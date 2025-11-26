package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // Sửa tên hàm cho đúng chuẩn JPA:
    // Tìm theo SessionId VÀ Student (đối tượng con) -> StudentCode (trường của con)
    boolean existsBySessionIdAndStudentStudentCode(Long sessionId, String studentCode);
}