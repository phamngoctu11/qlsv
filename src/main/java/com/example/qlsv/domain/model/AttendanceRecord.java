package com.example.qlsv.domain.model;

import com.example.qlsv.domain.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    // --- THAY ĐỔI ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_code", nullable = false)
    private Student student;
    // ----------------

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    private LocalDateTime checkInTime;

    public AttendanceRecord(AttendanceSession session, Student student, AttendanceStatus status) {
        this.session = session;
        this.student = student;
        this.status = status;
        this.checkInTime = LocalDateTime.now();
    }
}