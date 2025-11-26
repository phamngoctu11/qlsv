package com.example.qlsv.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "student_code", length = 20, nullable = false)
    private String studentCode; // KHÓA CHÍNH MỚI

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    // Quan hệ 1-1 với User (User chứa login info)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
}