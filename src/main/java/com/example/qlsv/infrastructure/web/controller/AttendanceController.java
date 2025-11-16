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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}