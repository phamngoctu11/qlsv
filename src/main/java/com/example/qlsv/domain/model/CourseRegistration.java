package com.example.qlsv.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "course_registrations")
public class CourseRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- THAY ĐỔI Ở ĐÂY ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_code", nullable = false)
    private Student student;
    // ---------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private LocalDateTime registrationDate;

    public CourseRegistration(Student student, Course course) {
        this.student = student;
        this.course = course;
        this.registrationDate = LocalDateTime.now();
    }
}