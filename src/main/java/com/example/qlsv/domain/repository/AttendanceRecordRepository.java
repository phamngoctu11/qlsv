package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * Kiểm tra xem sinh viên đã điểm danh cho phiên này chưa
     */
    boolean existsBySessionIdAndStudentId(Long sessionId, Long studentId);
}