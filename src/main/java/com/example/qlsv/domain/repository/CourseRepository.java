package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Course;
import com.example.qlsv.domain.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsBySubjectId(Long subjectId);
    boolean existsBySemesterId(Long semesterId);

    // Sửa: Lecturer ID giờ là String code
    boolean existsByLecturerLecturerCode(String lecturerCode);

    // Sửa: find by Lecturer Code
    List<Course> findByLecturerLecturerCode(String lecturerCode);

    // Sửa: Tìm giảng viên phụ trách (dùng Code)
    Optional<Course> findByIdAndLecturerLecturerCode(Long id, String lecturerCode);

    // Query lấy sinh viên
    @Query("SELECT cr.student FROM CourseRegistration cr WHERE cr.course.id = :courseId")
    List<Student> findStudentsByCourseId(Long courseId);
}