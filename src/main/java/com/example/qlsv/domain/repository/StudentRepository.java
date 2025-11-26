package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> { // ID là String (code)

    // Tìm sinh viên dựa trên User ID (Dùng khi đăng nhập xong)
    Optional<Student> findByUserId(Long userId);
}