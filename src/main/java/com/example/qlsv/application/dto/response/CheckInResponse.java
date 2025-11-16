package com.example.qlsv.application.dto.response;

import com.example.qlsv.domain.model.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private String message;
    private AttendanceStatus status;
    private LocalDateTime checkInTime;
}