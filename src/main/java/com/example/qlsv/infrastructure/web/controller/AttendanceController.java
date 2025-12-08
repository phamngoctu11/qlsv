package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.request.StartSessionRequest;
import com.example.qlsv.application.dto.request.StudentCheckInRequest; // <-- MỚI
import com.example.qlsv.application.dto.response.AttendanceSessionResponse;
import com.example.qlsv.application.dto.response.CheckInResponse; // <-- MỚI
import com.example.qlsv.application.service.AttendanceService;
import com.example.qlsv.domain.model.User; // <-- MỚI
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Endpoint cho Giảng viên bắt đầu một phiên điểm danh.
     */
    @PostMapping("/start-session")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')") // Admin hoặc Giảng viên
    public ResponseEntity<AttendanceSessionResponse> startSession(
            @Valid @RequestBody StartSessionRequest request,
            @AuthenticationPrincipal User currentUser // Lấy user đang đăng nhập
    ) {
        // Lấy ID của giảng viên từ user đang đăng nhập
        Long lecturerId = currentUser.getId();

        AttendanceSessionResponse response = attendanceService.startSession(request, lecturerId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/close-session")
    @PreAuthorize("hasRole('LECTURER')") // Chỉ Giảng viên được đóng
    public ResponseEntity<AttendanceSessionResponse> closeSession(
            @Valid @RequestBody StartSessionRequest request, // Dùng lại DTO này vì nó có chứa courseId
            @AuthenticationPrincipal User currentUser
    ) {
        AttendanceSessionResponse response = attendanceService.closeSession(request.getCourseId(), currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * [MỚI] Endpoint cho Sinh viên check-in.
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STUDENT')") // Chỉ Sinh viên
    public ResponseEntity<CheckInResponse> studentCheckIn(
            @Valid @RequestBody StudentCheckInRequest request,
            @AuthenticationPrincipal User currentUser // Lấy user (sinh viên) đang đăng nhập
    ) {
        // Lấy ID của sinh viên từ user đang đăng nhập
        Long studentId = currentUser.getId();

        CheckInResponse response = attendanceService.studentCheckIn(request, studentId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'LECTURER', 'STUDENT')")
    public ResponseEntity<List<AttendanceSessionResponse>> getSessionsByCourse(@PathVariable Long courseId) {
        List<AttendanceSessionResponse> sessions = attendanceService.getSessionsByCourse(courseId);
        return ResponseEntity.ok(sessions);
    }
}