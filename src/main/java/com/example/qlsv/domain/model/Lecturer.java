package com.example.qlsv.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lecturers")
public class Lecturer {

    @Id
    @Column(name = "lecturer_code", length = 20, nullable = false)
    private String lecturerCode; // KHÓA CHÍNH MỚI

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 100)
    private String department;

    // Quan hệ 1-1 với User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
}