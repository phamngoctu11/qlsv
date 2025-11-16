package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.CourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {

    // Kiểm tra xem sinh viên đã đăng ký lớp này chưa
    Optional<CourseRegistration> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Kiểm tra (trước khi xóa)
    boolean existsByStudentId(Long studentId);
    boolean existsByCourseId(Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}