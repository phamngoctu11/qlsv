package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Tìm sinh viên bằng mã sinh viên
    Optional<Student> findByStudentCode(String studentCode);

    // Tìm sinh viên bằng username (kế thừa từ User)
    Optional<Student> findByUsername(String username);
}