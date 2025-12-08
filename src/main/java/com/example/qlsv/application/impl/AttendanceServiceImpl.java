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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository recordRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AttendanceSessionMapper sessionMapper;

    private static final int LATE_THRESHOLD_MINUTES = 15;

    @Override
    @Transactional
    public AttendanceSessionResponse startSession(StartSessionRequest request, Long userId) {
        // 1. Tìm Lecturer từ UserID
        Lecturer lecturer = lecturerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ Giảng viên."));

        // 2. Tìm Course bằng Lecturer Code
        Course course = courseRepository.findByIdAndLecturerLecturerCode(request.getCourseId(), lecturer.getLecturerCode())
                .orElseThrow(() -> new BusinessException("Giảng viên không phụ trách lớp này hoặc lớp không tồn tại."));

        // 3. Validate Time
        LocalDateTime now = LocalDateTime.now();
        if (course.getDayOfWeek() != now.getDayOfWeek()) {
            throw new BusinessException("Hôm nay không phải ngày học (" + course.getDayOfWeek() + ").");
        }
        if (now.toLocalTime().isBefore(course.getStartTime()) || now.toLocalTime().isAfter(course.getEndTime())) {
            throw new BusinessException("Ngoài khung giờ học.");
        }

        // 4. Check Session
        sessionRepository.findByCourseIdAndStatus(request.getCourseId(), SessionStatus.OPEN).ifPresent(s -> {
            throw new BusinessException("Đã có phiên điểm danh đang mở.");
        });

        AttendanceSession session = new AttendanceSession(course, lecturer);
        return sessionMapper.toResponse(sessionRepository.save(session));
    }
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> getSessionsByCourse(Long courseId) {
        // Kiểm tra lớp học có tồn tại không
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        // Lấy danh sách và Map sang DTO
        return sessionRepository.findByCourseIdOrderByStartTimeDesc(courseId).stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CheckInResponse studentCheckIn(StudentCheckInRequest request, Long userId) {
        // 1. Tìm Student từ UserID
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ Sinh viên."));

        String studentCode = student.getStudentCode(); // Lấy mã SV

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        // 2. Check Đăng ký (Dùng studentCode)
        if (!registrationRepository.existsByStudentStudentCodeAndCourseId(studentCode, request.getCourseId())) {
            throw new BusinessException("Bạn chưa đăng ký lớp học phần này.");
        }

        // 3. Check Time & Session
        LocalDateTime now = LocalDateTime.now();
        if (course.getDayOfWeek() != now.getDayOfWeek()) throw new BusinessException("Sai ngày học.");
        if (now.toLocalTime().isBefore(course.getStartTime()) || now.toLocalTime().isAfter(course.getEndTime()))
            throw new BusinessException("Sai giờ học.");

        AttendanceSession session = sessionRepository.findByCourseIdAndStatus(request.getCourseId(), SessionStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Giảng viên chưa mở điểm danh."));

        // 4. [SỬA LẠI] Check Trùng (Dùng hàm đúng tên trong Repo)
        if (recordRepository.existsBySessionIdAndStudentStudentCode(session.getId(), studentCode)) {
            throw new BusinessException("Bạn đã điểm danh rồi.");
        }

        // 5. Save Record
        AttendanceStatus status = now.isAfter(session.getStartTime().plusMinutes(LATE_THRESHOLD_MINUTES))
                ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        AttendanceRecord record = new AttendanceRecord(session, student, status);
        recordRepository.save(record);

        return new CheckInResponse("Điểm danh thành công!", status, record.getCheckInTime());
    }
    @Override
    @Transactional
    public AttendanceSessionResponse closeSession(Long courseId, Long userId) {
        // 1. Lấy thông tin Giảng viên từ User ID
        Lecturer lecturer = lecturerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin giảng viên."));

        // 2. Tìm phiên đang MỞ của lớp học này
        AttendanceSession session = sessionRepository.findByCourseIdAndStatus(courseId, SessionStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Lớp học này hiện không có phiên điểm danh nào đang mở."));

        // 3. Kiểm tra quyền sở hữu (Giảng viên này có phải người dạy lớp này không)
        // So sánh lecturerCode trong session và lecturerCode của người đang login
        if (!session.getLecturer().getLecturerCode().equals(lecturer.getLecturerCode())) {
            throw new BusinessException("Bạn không có quyền đóng phiên điểm danh của giảng viên khác.");
        }

        // 4. Đóng phiên
        session.setStatus(SessionStatus.CLOSED);

        // (Tuỳ chọn) Nếu muốn kích hoạt quét vắng mặt ngay lập tức thì gọi logic đó ở đây
        // Nhưng để đơn giản, cứ để Task tự động quét sau.

        return sessionMapper.toResponse(sessionRepository.save(session));
    }
}