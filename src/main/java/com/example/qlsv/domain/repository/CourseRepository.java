package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // Tìm lớp học dựa trên ID và ID giảng viên
    Optional<Course> findByIdAndLecturerId(Long courseId, Long lecturerId);
    Optional<Course> findByCourseCode(String courseCode);

    // 2. Kiểm tra nghiệp vụ (trước khi xóa)
    // (Đây là các phương thức tôi đã "nhá hàng" ở các file service trước)
    boolean existsBySubjectId(Long subjectId);
    boolean existsBySemesterId(Long semesterId);
    boolean existsByLecturerId(Long lecturerId);

    // 3. Lấy lớp học phần theo giảng viên
    List<Course> findByLecturerId(Long lecturerId);

    // 4. (Nâng cao) Lấy danh sách sinh viên trong lớp
    @Query("SELECT cr.student FROM CourseRegistration cr WHERE cr.course.id = :courseId")
    List<com.example.qlsv.domain.model.Student> findStudentsByCourseId(Long courseId);
}