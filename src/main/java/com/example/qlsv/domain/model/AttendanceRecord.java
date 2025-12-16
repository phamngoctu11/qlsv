package com.example.qlsv.domain.model;

import com.example.qlsv.domain.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private AttendanceSession session;

    // --- THAY ĐỔI: Trỏ tới User ---
    @ManyToOne
    @JoinColumn(name = "student_user_id")
    private User student;

    private LocalDateTime checkInTime;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
}