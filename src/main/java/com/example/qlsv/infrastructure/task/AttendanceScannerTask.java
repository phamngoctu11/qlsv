package com.example.qlsv.infrastructure.task;

import com.example.qlsv.domain.model.Course;
import com.example.qlsv.domain.model.Student;
import com.example.qlsv.domain.model.enums.AttendanceStatus;
import com.example.qlsv.domain.repository.AttendanceRecordRepository;
import com.example.qlsv.domain.repository.CourseRepository;
import com.example.qlsv.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceScannerTask {

    private final CourseRepository courseRepository;
    private final AttendanceRecordRepository recordRepository;
    private final EmailService emailService;

    // Chạy mỗi 30 phút
    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void scanForAbsencesAndBan() {
        System.out.println("--- [TASK] Đang quét các lớp vừa kết thúc để kiểm tra vắng mặt... ---");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        // 1. Lấy tất cả lớp học của HÔM NAY
        List<Course> coursesToday = courseRepository.findAll().stream()
                .filter(c -> c.getDayOfWeek() == currentDay)
                .toList();

        for (Course course : coursesToday) {
            // 2. Chỉ xử lý các lớp ĐÃ KẾT THÚC trong vòng 1 tiếng qua
            if (now.isAfter(course.getEndTime()) && now.isBefore(course.getEndTime().plusHours(1))) {
                System.out.println("--- [TASK] Đang xử lý lớp: " + course.getCourseCode());
                processAbsentStudents(course);
            }
        }
    }

    private void processAbsentStudents(Course course) {
        // Lấy danh sách sinh viên trong lớp
        List<Student> students = courseRepository.findStudentsByCourseId(course.getId());

        int totalSessions = calculateTotalSessions(course.getSemester().getStartDate(), course.getSemester().getEndDate(), course.getDayOfWeek());
        if (totalSessions == 0) totalSessions = 1;

        int passedSessions = calculateTotalSessions(course.getSemester().getStartDate(), LocalDate.now(), course.getDayOfWeek());

        // Lấy toàn bộ record để tối ưu (tránh query trong loop)
        var allRecords = recordRepository.findAll();

        for (Student student : students) {
            // Đếm số lần đi học
            long attendedCount = allRecords.stream()
                    .filter(r -> r.getSession().getCourse().getId().equals(course.getId())
                            && r.getStudent().getId().equals(student.getId())
                            && (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE))
                    .count();

            int absentCount = passedSessions - (int) attendedCount;
            if (absentCount < 0) absentCount = 0;

            double absentPercentage = ((double) absentCount / totalSessions) * 100;

            // KIỂM TRA ĐIỀU KIỆN GỬI MAIL
            // Điều kiện: Vắng > 30% VÀ buổi vắng hôm nay là buổi quyết định
            if (absentPercentage > 30.0) {
                // Tính thử % của lần trước (tức là nếu đi học hôm nay thì sao?)
                // Nếu (vắng - 1) vẫn > 30% nghĩa là đã bị cấm từ trước rồi -> Không gửi nữa
                // Nếu (vắng - 1) <= 30% nghĩa là hôm nay mới chính thức bị cấm -> GỬI

                double prevPercentage = ((double) (absentCount - 1) / totalSessions) * 100;

                if (prevPercentage <= 30.0) {
                    System.out.println("--- [TASK] Phát hiện sinh viên " + student.getStudentCode() + " bị cấm thi. Đang gửi mail...");
                    emailService.sendBanNotification(
                            student.getEmail(),
                            student.getLastName() + " " + student.getFirstName(),
                            course.getSubject().getName() + " (" + course.getCourseCode() + ")"
                    );
                }
            }
        }
    }

    private int calculateTotalSessions(LocalDate start, LocalDate end, DayOfWeek classDay) {
        if (start.isAfter(end)) return 0;
        LocalDate firstClassDate = start;
        while (firstClassDate.getDayOfWeek() != classDay) {
            firstClassDate = firstClassDate.plusDays(1);
        }
        if (firstClassDate.isAfter(end)) return 0;
        long weeks = ChronoUnit.WEEKS.between(firstClassDate, end);
        return (int) weeks + 1;
    }
}