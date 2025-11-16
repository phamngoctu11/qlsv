package com.example.qlsv.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true) // Quan trọng khi kế thừa
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student extends User {

    @Column(unique = true, nullable = false, length = 20)
    private String studentCode; // Mã sinh viên

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    // Một sinh viên có thể đăng ký nhiều lớp học
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseRegistration> registrations = new HashSet<>();

    // Một sinh viên có nhiều bản ghi điểm danh
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttendanceRecord> attendanceRecords = new HashSet<>();
}