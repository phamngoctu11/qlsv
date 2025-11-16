package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.request.CreateCourseRequest;
import com.example.qlsv.application.dto.request.RegisterStudentRequest;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.application.dto.response.SimpleStudentResponse; // Đổi từ UserResponse
import com.example.qlsv.application.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse createdCourse = courseService.createCourse(request);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // --- Các nghiệp vụ chuyên sâu ---

    @PostMapping("/register-student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerStudentToCourse(@Valid @RequestBody RegisterStudentRequest request) {
        // Gọi service với 2 ID tách biệt
        courseService.registerStudent(request.getStudentId(), request.getCourseId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<List<SimpleStudentResponse>> getStudentsInCourse(@PathVariable Long id) {
        List<SimpleStudentResponse> students = courseService.getStudentsByCourse(id);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-lecturer/{lecturerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<List<CourseResponse>> getCoursesByLecturer(@PathVariable Long lecturerId) {
        // (Trong dự án thật, Giảng viên chỉ nên xem được của chính họ
        // bằng cách lấy ID từ Principal, thay vì PathVariable)
        List<CourseResponse> courses = courseService.getCoursesByLecturer(lecturerId);
        return ResponseEntity.ok(courses);
    }
}