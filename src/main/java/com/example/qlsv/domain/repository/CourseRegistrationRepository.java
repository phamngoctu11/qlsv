package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.CourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {

    // Sửa: Student ID giờ là String code
    boolean existsByStudentStudentCodeAndCourseId(String studentCode, Long courseId);

    boolean existsByStudentStudentCode(String studentCode);
    boolean existsByCourseId(Long courseId);
}