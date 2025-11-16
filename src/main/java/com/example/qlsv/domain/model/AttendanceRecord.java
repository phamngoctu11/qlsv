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

    // Nhiều bản ghi thuộc MỘT phiên điểm danh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    // Nhiều bản ghi thuộc MỘT sinh viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    private LocalDateTime checkInTime; // Thời điểm sinh viên check-in

    public AttendanceRecord(AttendanceSession session, Student student, AttendanceStatus status) {
        this.session = session;
        this.student = student;
        this.status = status;
        if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE) {
            this.checkInTime = LocalDateTime.now();
        }
    }
}