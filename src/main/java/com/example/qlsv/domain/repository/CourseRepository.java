package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.Course;
import com.example.qlsv.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Tìm lớp theo ID của User (Giảng viên)
    List<Course> findByLecturerId(Long lecturerUserId);

    // Validate trùng lịch dạy (Lecturer là User)
    @Query("SELECT c FROM Course c WHERE c.lecturer.id = :lecturerUserId " +
            "AND c.semester.id = :semesterId " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND ((:startTime BETWEEN c.startTime AND c.endTime) OR (:endTime BETWEEN c.startTime AND c.endTime))")
    List<Course> findConflictingCoursesForLecturer(Long lecturerUserId, Long semesterId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    // Validate trùng lịch học (Student là User)
    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentUserId " +
            "AND c.semester.id = :semesterId " +
            "AND c.dayOfWeek = :dayOfWeek " +
            "AND ((:startTime BETWEEN c.startTime AND c.endTime) OR (:endTime BETWEEN c.startTime AND c.endTime))")
    List<Course> findConflictingCoursesForStudent(Long studentUserId, Long semesterId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    // Lấy danh sách SV (User) trong lớp
    @Query("SELECT s FROM Course c JOIN c.students s WHERE c.id = :courseId")
    List<User> findStudentsByCourseId(@Param("courseId") Long courseId);
}