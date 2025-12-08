package com.example.qlsv.application.service;

import com.example.qlsv.application.dto.request.StartSessionRequest;
import com.example.qlsv.application.dto.request.StudentCheckInRequest; // <-- MỚI
import com.example.qlsv.application.dto.response.AttendanceSessionResponse;
import com.example.qlsv.application.dto.response.CheckInResponse; // <-- MỚI

import java.util.List;

public interface AttendanceService {

    // (Hàm cũ của Giảng viên)
    AttendanceSessionResponse startSession(StartSessionRequest request, Long lecturerId);

    // --- [MỚI] HÀM CỦA SINH VIÊN ---
    AttendanceSessionResponse closeSession(Long courseId, Long userId);
    CheckInResponse studentCheckIn(StudentCheckInRequest request, Long studentId);
    List<AttendanceSessionResponse> getSessionsByCourse(Long courseId);
}