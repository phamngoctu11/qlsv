package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    // Sửa student.studentCode -> student.studentCode (vẫn giống tên nhưng logic entity khác)
    boolean existsBySessionIdAndStudentStudentCode(Long sessionId, String studentCode);

    List<AttendanceRecord> findBySessionCourseIdAndStudentStudentCodeOrderByCheckInTimeDesc(Long courseId, String studentCode);

    List<AttendanceRecord> findBySessionIdOrderByCheckInTimeDesc(Long sessionId);
}