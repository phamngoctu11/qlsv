package com.example.qlsv.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentAttendanceStat {
    private Long studentId;
    private String studentName;
    private String studentCode;

    private int totalSessions;      // Tổng số buổi dự kiến của cả kỳ
    private int attendedSessions;   // Số buổi đã đi học
    private int absentSessions;     // Số buổi vắng (tính đến hiện tại)

    private double absentPercentage; // Tỷ lệ vắng (%)
    private boolean isBanned;        // true nếu vắng > 30% - CẤM THI
}