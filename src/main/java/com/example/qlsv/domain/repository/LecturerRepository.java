package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    // Tìm giảng viên bằng username (kế thừa từ User)
    Optional<Lecturer> findByUsername(String username);
}