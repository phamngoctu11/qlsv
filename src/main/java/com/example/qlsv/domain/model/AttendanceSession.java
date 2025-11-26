package com.example.qlsv.domain.model;

import com.example.qlsv.domain.model.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // --- THAY ĐỔI ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_code", nullable = false)
    private Lecturer lecturer;
    // ----------------

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(nullable = false, unique = true)
    private String qrCodeData;

    public AttendanceSession(Course course, Lecturer lecturer) {
        this.course = course;
        this.lecturer = lecturer;
        this.startTime = LocalDateTime.now();
        this.status = SessionStatus.OPEN;
        this.qrCodeData = UUID.randomUUID().toString();
    }
}