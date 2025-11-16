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
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor // Tự động @Autowired qua constructor
public class AttendanceServiceImpl implements AttendanceService {

    // Chỉ phụ thuộc vào các Interfaces của Domain và Mapper
    private final AttendanceSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository; // <-- MỚI
    private final AttendanceRecordRepository recordRepository; // <-- MỚI
    private final UserRepository userRepository; // Giả định đã có
    private final AttendanceSessionMapper sessionMapper;
    private final CourseRegistrationRepository registrationRepository;
    @Override
    @Transactional // Đảm bảo tính toàn vẹn
    public AttendanceSessionResponse startSession(StartSessionRequest request, Long lecturerId) {

        // --- QUY TẮC NGHIỆP VỤ (BUSINESS RULES) ---

        // 1. Kiểm tra xem giảng viên có tồn tại không
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên với ID: " + lecturerId));

        // 2. Kiểm tra xem giảng viên có thực sự dạy lớp này không
        Course course = courseRepository.findByIdAndLecturerId(request.getCourseId(), lecturerId)
                .orElseThrow(() -> new BusinessException("Giảng viên không phụ trách lớp này hoặc lớp không tồn tại."));

        // 3. Kiểm tra xem lớp này đã có phiên nào đang MỞ chưa
        sessionRepository.findByCourseIdAndStatus(request.getCourseId(), SessionStatus.OPEN)
                .ifPresent(session -> {
                    throw new BusinessException("Lớp " + course.getSubject().getName() + " đã có một phiên điểm danh đang mở.");
                });

        // --- HẾT QUY TẮC NGHIỆP VỤ ---

        // Nếu mọi thứ hợp lệ, tạo phiên mới
        AttendanceSession newSession = new AttendanceSession(course, lecturer);

        // Lưu vào DB
        AttendanceSession savedSession = sessionRepository.save(newSession);

        // Map sang DTO để trả về cho client
        return sessionMapper.toResponse(savedSession);
    }




    private static final int LATE_THRESHOLD_MINUTES = 15;

    @Override
    @Transactional
    public CheckInResponse studentCheckIn(StudentCheckInRequest request, Long studentId) {

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek currentDay = now.getDayOfWeek();

        // 1. Tìm Sinh viên
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // 2. Tìm Lớp học phần
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));
        boolean isRegistered = registrationRepository.existsByStudentIdAndCourseId(studentId, request.getCourseId());

        if (!isRegistered) {
            throw new BusinessException("Bạn chưa đăng ký tham gia lớp học phần này, không thể điểm danh.");
        }
        // --- QUY TẮC NGHIỆP VỤ VỀ THỜI GIAN ---

        // 3. [NGHIỆP VỤ 1] Kiểm tra xem có đúng ngày học không
        if (course.getDayOfWeek() != currentDay) {
            throw new BusinessException("Hôm nay không phải ngày học của môn này (" + course.getDayOfWeek() + ").");
        }

        // 4. [NGHIỆP VỤ 2] Kiểm tra xem có đúng khung giờ học không
        // (Ví dụ: Lớp 9:00-12:00, sinh viên check-in lúc 8:50 hoặc 12:01 là thất bại)
        if (currentTime.isBefore(course.getStartTime()) || currentTime.isAfter(course.getEndTime())) {
            throw new BusinessException("Không thể điểm danh ngoài khung giờ học ( "
                    + course.getStartTime() + " - " + course.getEndTime() + " ).");
        }

        // 5. [NGHIỆP VỤ 3] Kiểm tra xem Giảng viên đã mở phiên điểm danh chưa
        AttendanceSession session = sessionRepository
                .findByCourseIdAndStatus(request.getCourseId(), SessionStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Giảng viên chưa mở điểm danh cho buổi học này."));

        // 6. [NGHIỆP VỤ 4] Kiểm tra xem sinh viên đã điểm danh chưa
        boolean alreadyCheckedIn = recordRepository.existsBySessionIdAndStudentId(session.getId(), studentId);
        if (alreadyCheckedIn) {
            throw new BusinessException("Bạn đã điểm danh cho buổi học này rồi.");
        }

        // --- HẾT QUY TẮC NGHIỆP VỤ ---

        // 7. Xác định trạng thái (Trễ hay Đúng giờ)
        // Giờ G: là giờ Giảng viên bấm nút "Start Session" + 15 phút
        LocalDateTime lateThresholdTime = session.getStartTime().plusMinutes(LATE_THRESHOLD_MINUTES);

        AttendanceStatus status;
        if (now.isAfter(lateThresholdTime)) {
            status = AttendanceStatus.LATE; // Đi trễ
        } else {
            status = AttendanceStatus.PRESENT; // Có mặt
        }

        // 8. Tạo và lưu bản ghi
        AttendanceRecord record = new AttendanceRecord(session, student, status);
        recordRepository.save(record);

        return new CheckInResponse(
                "Điểm danh thành công!",
                status,
                record.getCheckInTime()
        );
    }
}