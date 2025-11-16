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

    // Lớp học phần mà phiên này thuộc về
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Giảng viên tạo phiên này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    // Dữ liệu duy nhất để tạo mã QR, sinh viên sẽ gửi cái này lên
    @Column(nullable = false, unique = true)
    private String qrCodeData;

    public AttendanceSession(Course course, User lecturer) {
        this.course = course;
        this.lecturer = lecturer;
        this.startTime = LocalDateTime.now();
        this.status = SessionStatus.OPEN;
        // Tạo một mã định danh duy nhất cho phiên QR
        this.qrCodeData = UUID.randomUUID().toString();
    }

    // Business logic: Phương thức để đóng phiên
    public void closeSession() {
        if (this.status == SessionStatus.OPEN) {
            this.status = SessionStatus.CLOSED;
            this.endTime = LocalDateTime.now();
        }
    }
}