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

    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void scanForAbsencesAndBan() {
        System.out.println("--- [TASK] Đang quét điểm danh... ---");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        List<Course> coursesToday = courseRepository.findAll().stream()
                .filter(c -> c.getDayOfWeek() == currentDay)
                .toList();

        for (Course course : coursesToday) {
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

        // Lấy tất cả record
        var allRecords = recordRepository.findAll();

        for (Student student : students) {
            long attended = allRecords.stream()
                    .filter(r -> r.getSession().getCourse().getId().equals(course.getId())
                            // --- SỬA: SO SÁNH STUDENT CODE ---
                            && r.getStudent().getStudentCode().equals(student.getStudentCode())
                            && (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE))
                    .count();

            int absent = passed - (int) attended;
            if (absent < 0) absent = 0;
            double pct = ((double) absent / total) * 100;

            if (pct > 30.0) {
                double prevPct = ((double) (absent - 1) / total) * 100;
                if (prevPct <= 30.0) {
                    // Lấy email từ bảng User thông qua Student
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