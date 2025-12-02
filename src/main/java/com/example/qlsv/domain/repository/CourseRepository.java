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

    // 1. Các hàm cơ bản
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsBySubjectId(Long subjectId);
    boolean existsBySemesterId(Long semesterId);

    // 2. Các hàm hỗ trợ cấu trúc mới (String Code)
    boolean existsByLecturerLecturerCode(String lecturerCode);
    List<Course> findByLecturerLecturerCode(String lecturerCode);
    Optional<Course> findByIdAndLecturerLecturerCode(Long id, String lecturerCode);

    // 3. Hàm lấy danh sách sinh viên
    @Query("SELECT cr.student FROM CourseRegistration cr WHERE cr.course.id = :courseId")
    List<Student> findStudentsByCourseId(Long courseId);

    // 4. Validate Lịch học SINH VIÊN
    @Query("SELECT cr.course FROM CourseRegistration cr " +
            "WHERE cr.student.studentCode = :studentCode " +
            "AND cr.course.semester.id = :semesterId")
    List<Course> findCoursesByStudentCodeAndSemesterId(String studentCode, Long semesterId);

    // 5. [MỚI] Validate Lịch dạy GIẢNG VIÊN (Lấy các lớp GV dạy trong học kỳ này)
    List<Course> findByLecturerLecturerCodeAndSemesterId(String lecturerCode, Long semesterId);
}