package com.example.qlsv.infrastructure.task;

import com.example.qlsv.domain.model.AttendanceSession;
import com.example.qlsv.domain.model.Course;
import com.example.qlsv.domain.model.Student;
import com.example.qlsv.domain.model.enums.AttendanceStatus;
import com.example.qlsv.domain.model.enums.SessionStatus;
import com.example.qlsv.domain.repository.AttendanceRecordRepository;
import com.example.qlsv.domain.repository.AttendanceSessionRepository;
import com.example.qlsv.domain.repository.CourseRepository;
import com.example.qlsv.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceScannerTask {

    private final CourseRepository courseRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository; // [MỚI]
    private final EmailService emailService;

    // Chạy mỗi 30 phút (00:00, 00:30, 01:00...)
    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void runScheduledTasks() {
        System.out.println("--- [TASK] Bắt đầu chạy tác vụ định kỳ... ---");

        // 1. Tự động đóng các phiên đã hết giờ
        autoCloseExpiredSessions();

        // 2. Quét vắng mặt và gửi mail cấm thi
        scanForAbsencesAndBan();

        System.out.println("--- [TASK] Hoàn tất tác vụ. ---");
    }

    /**
     * [MỚI] Tự động đóng các session đang OPEN mà giờ hiện tại đã vượt quá giờ kết thúc của lớp học
     */
    private void autoCloseExpiredSessions() {
        // Lấy tất cả các phiên đang MỞ
        List<AttendanceSession> openSessions = sessionRepository.findAllByStatus(SessionStatus.OPEN);
        LocalDateTime now = LocalDateTime.now();

        int closedCount = 0;

        for (AttendanceSession session : openSessions) {
            // Tính thời gian kết thúc của phiên này
            // Logic: Ngày bắt đầu phiên + Giờ kết thúc của Môn học
            LocalDate sessionDate = session.getStartTime().toLocalDate();
            LocalTime courseEndTime = session.getCourse().getEndTime();

            // Thời điểm session nên đóng
            LocalDateTime sessionEndTime = sessionDate.atTime(courseEndTime);

            // Nếu bây giờ đã qua giờ kết thúc -> ĐÓNG
            if (now.isAfter(sessionEndTime)) {
                session.setStatus(SessionStatus.CLOSED);
                sessionRepository.save(session);
                closedCount++;
                System.out.println("-> Đã tự động đóng phiên điểm danh ID: " + session.getId()
                        + " (Lớp: " + session.getCourse().getCourseCode() + ")");
            }
        }

        if (closedCount > 0) {
            System.out.println("--- [TASK] Đã đóng " + closedCount + " phiên điểm danh quá hạn.");
        }
    }

    private void scanForAbsencesAndBan() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        // Lấy các lớp học CÓ lịch hôm nay
        List<Course> coursesToday = courseRepository.findAll().stream()
                .filter(c -> c.getDayOfWeek() == currentDay)
                .toList();

        for (Course course : coursesToday) {
            // Chỉ xử lý các lớp VỪA KẾT THÚC trong vòng 1 tiếng qua (để tránh gửi mail lại)
            if (now.isAfter(course.getEndTime()) && now.isBefore(course.getEndTime().plusHours(1))) {
                processAbsentStudents(course);
            }
        }
    }

    private void processAbsentStudents(Course course) {
        List<Student> students = courseRepository.findStudentsByCourseId(course.getId());

        int total = calculateSessions(course.getSemester().getStartDate(), course.getSemester().getEndDate(), course.getDayOfWeek());
        if (total == 0) total = 1;
        int passed = calculateSessions(course.getSemester().getStartDate(), LocalDate.now(), course.getDayOfWeek());

        var allRecords = recordRepository.findAll();

        for (Student student : students) {
            long attended = allRecords.stream()
                    .filter(r -> r.getSession().getCourse().getId().equals(course.getId())
                            && r.getStudent().getStudentCode().equals(student.getStudentCode())
                            && (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE))
                    .count();

            int absent = passed - (int) attended;
            if (absent < 0) absent = 0;
            double pct = ((double) absent / total) * 100;

            if (pct > 30.0) {
                double prevPct = ((double) (absent - 1) / total) * 100;
                if (prevPct <= 30.0) {
                    String email = student.getUser().getEmail();
                    emailService.sendBanNotification(
                            email,
                            student.getLastName() + " " + student.getFirstName(),
                            course.getCourseCode()
                    );
                }
            }
        }
    }

    private int calculateSessions(LocalDate start, LocalDate end, DayOfWeek day) {
        if (start.isAfter(end)) return 0;
        LocalDate d = start;
        while (d.getDayOfWeek() != day) d = d.plusDays(1);
        if (d.isAfter(end)) return 0;
        return (int) ChronoUnit.WEEKS.between(d, end) + 1;
    }
}