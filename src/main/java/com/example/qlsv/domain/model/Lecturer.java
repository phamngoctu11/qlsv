package com.example.qlsv.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "lecturers")
public class Lecturer extends User {

    @Column(unique = true, nullable = false, length = 20)
    private String lecturerCode; // Mã giảng viên

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 100)
    private String department; // Khoa

    // Một giảng viên dạy nhiều lớp học phần
    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.PERSIST)
    private Set<Course> courses = new HashSet<>();
}