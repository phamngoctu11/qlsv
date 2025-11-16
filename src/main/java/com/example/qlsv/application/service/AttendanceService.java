package com.example.qlsv.application.service;

import com.example.qlsv.application.dto.request.StartSessionRequest;
import com.example.qlsv.application.dto.request.StudentCheckInRequest; // <-- MỚI
import com.example.qlsv.application.dto.response.AttendanceSessionResponse;
import com.example.qlsv.application.dto.response.CheckInResponse; // <-- MỚI

public interface AttendanceService {

    // (Hàm cũ của Giảng viên)
    AttendanceSessionResponse startSession(StartSessionRequest request, Long lecturerId);

    // --- [MỚI] HÀM CỦA SINH VIÊN ---
    CheckInResponse studentCheckIn(StudentCheckInRequest request, Long studentId);
}