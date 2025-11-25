package com.example.qlsv.application.impl;
import com.example.qlsv.application.dto.mapper.AttendanceSessionMapper;
import com.example.qlsv.application.dto.request.StartSessionRequest;
import com.example.qlsv.application.dto.request.StudentCheckInRequest;
import com.example.qlsv.application.dto.response.AttendanceSessionResponse;
import com.example.qlsv.application.dto.response.CheckInResponse;
import com.example.qlsv.application.service.AttendanceService;
import com.example.qlsv.domain.exception.BusinessException;
import com.example.qlsv.domain.exception.ResourceNotFoundException;
import com.example.qlsv.domain.model.*;
import com.example.qlsv.domain.model.enums.AttendanceStatus;
import com.example.qlsv.domain.model.enums.SessionStatus;
import com.example.qlsv.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository recordRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AttendanceSessionMapper sessionMapper;

    // Ngưỡng đi trễ (phút)
    private static final int LATE_THRESHOLD_MINUTES = 15;

    /**
     * CHỨC NĂNG 1: GIẢNG VIÊN MỞ PHIÊN ĐIỂM DANH
     */
    @Override
    @Transactional
    public AttendanceSessionResponse startSession(StartSessionRequest request, Long lecturerId) {

        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên với ID: " + lecturerId));

        Course course = courseRepository.findByIdAndLecturerId(request.getCourseId(), lecturerId)
                .orElseThrow(() -> new BusinessException("Giảng viên không phụ trách lớp này hoặc lớp không tồn tại."));

        // --- KIỂM TRA THỜI GIAN ---
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        if (course.getDayOfWeek() != currentDay) {
            throw new BusinessException("Hôm nay không phải ngày học của lớp này (Lịch: " + course.getDayOfWeek() + ").");
        }

        if (currentTime.isBefore(course.getStartTime()) || currentTime.isAfter(course.getEndTime())) {
            throw new BusinessException("Chưa đến giờ hoặc đã hết giờ học (Lịch: "
                    + course.getStartTime() + " - " + course.getEndTime() + ").");
        }

        // Kiểm tra session đang mở
        sessionRepository.findByCourseIdAndStatus(request.getCourseId(), SessionStatus.OPEN)
                .ifPresent(session -> {
                    throw new BusinessException("Lớp " + course.getSubject().getName() + " đã có một phiên điểm danh đang mở.");
                });

        AttendanceSession newSession = new AttendanceSession(course, lecturer);
        AttendanceSession savedSession = sessionRepository.save(newSession);

        return sessionMapper.toResponse(savedSession);
    }

    /**
     * CHỨC NĂNG 2: SINH VIÊN CHECK-IN (ĐIỂM DANH)
     */
    @Override
    @Transactional
    public CheckInResponse studentCheckIn(StudentCheckInRequest request, Long studentId) {

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek currentDay = now.getDayOfWeek();

        // 1. Tìm Student & Course
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        // 2. KIỂM TRA ĐĂNG KÝ KHOÁ HỌC
        boolean isRegistered = registrationRepository.existsByStudentIdAndCourseId(studentId, request.getCourseId());
        if (!isRegistered) {
            throw new BusinessException("Bạn chưa đăng ký tham gia lớp học phần này, không thể điểm danh.");
        }

        // 3. --- [QUAN TRỌNG] KIỂM TRA CẤM THI ---
        if (isStudentBanned(course, studentId)) {
            throw new BusinessException("BẠN ĐÃ BỊ CẤM THI MÔN NÀY (Vắng quá 30%). Không thể điểm danh.");
        }
        // ----------------------------------------

        // 4. Kiểm tra Lịch học
        if (course.getDayOfWeek() != currentDay) {
            throw new BusinessException("Hôm nay không phải ngày học của môn này (" + course.getDayOfWeek() + ").");
        }
        if (currentTime.isBefore(course.getStartTime()) || currentTime.isAfter(course.getEndTime())) {
            throw new BusinessException("Không thể điểm danh ngoài khung giờ học ("
                    + course.getStartTime() + " - " + course.getEndTime() + ").");
        }

        // 5. Kiểm tra Session
        AttendanceSession session = sessionRepository
                .findByCourseIdAndStatus(request.getCourseId(), SessionStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Giảng viên chưa mở điểm danh cho buổi học này."));

        // 6. Kiểm tra check-in trùng
        boolean alreadyCheckedIn = recordRepository.existsBySessionIdAndStudentId(session.getId(), studentId);
        if (alreadyCheckedIn) {
            throw new BusinessException("Bạn đã điểm danh cho buổi học này rồi.");
        }

        // 7. Xác định trạng thái
        LocalDateTime lateThresholdTime = session.getStartTime().plusMinutes(LATE_THRESHOLD_MINUTES);
        AttendanceStatus status = now.isAfter(lateThresholdTime) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        // 8. Lưu
        AttendanceRecord record = new AttendanceRecord(session, student, status);
        recordRepository.save(record);

        return new CheckInResponse("Điểm danh thành công!", status, record.getCheckInTime());
    }

    // --- CÁC HÀM PHỤ TRỢ (HELPER METHODS) ---

    private boolean isStudentBanned(Course course, Long studentId) {
        int totalSessions = calculateTotalSessions(
                course.getSemester().getStartDate(),
                course.getSemester().getEndDate(),
                course.getDayOfWeek()
        );
        if (totalSessions == 0) totalSessions = 1;

        // Tính số buổi ĐÃ TRÔI QUA (đến hôm nay)
        int passedSessions = calculateTotalSessions(
                course.getSemester().getStartDate(),
                LocalDate.now(),
                course.getDayOfWeek()
        );
        if (passedSessions > totalSessions) passedSessions = totalSessions;

        // Đếm số lần đi học
        long attendedCount = recordRepository.findAll().stream()
                .filter(r -> r.getSession().getCourse().getId().equals(course.getId())
                        && r.getStudent().getId().equals(studentId)
                        && (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE))
                .count();

        // Số buổi vắng = (Đã trôi qua) - (Đã đi học)
        int absentCount = passedSessions - (int) attendedCount;
        if (absentCount < 0) absentCount = 0;

        double absentPercentage = ((double) absentCount / totalSessions) * 100;
        return absentPercentage > 30.0;
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