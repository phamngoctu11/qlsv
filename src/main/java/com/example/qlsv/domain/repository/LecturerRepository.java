package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, String> { // ID là String (code)

    // Tìm giảng viên dựa trên User ID
    Optional<Lecturer> findByUserId(Long userId);
}