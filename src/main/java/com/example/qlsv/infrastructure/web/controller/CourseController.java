package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.request.CreateCourseRequest;
import com.example.qlsv.application.dto.request.RegisterStudentRequest;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.application.dto.response.SimpleStudentResponse;
import com.example.qlsv.application.dto.response.StudentAttendanceStat;
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

    // --- [SỬA LỖI Ở ĐÂY] ---
    @PostMapping("/register-student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerStudentToCourse(@Valid @RequestBody RegisterStudentRequest request) {
        // Sửa getStudentId() thành getStudentCode()
        courseService.registerStudent(request.getStudentCode(), request.getCourseId());
        return ResponseEntity.ok().build();
    }
    // -----------------------

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<List<SimpleStudentResponse>> getStudentsInCourse(@PathVariable Long id) {
        List<SimpleStudentResponse> students = courseService.getStudentsByCourse(id);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-lecturer/{lecturerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<List<CourseResponse>> getCoursesByLecturer(@PathVariable Long lecturerId) {
        // Lưu ý: lecturerId ở đây là User ID (Long)
        List<CourseResponse> courses = courseService.getCoursesByLecturer(lecturerId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<List<StudentAttendanceStat>> getCourseStats(@PathVariable Long id) {
        List<StudentAttendanceStat> stats = courseService.getCourseStatistics(id);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{id}/send-ban-notifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<String> sendBanNotifications(@PathVariable Long id) {
        courseService.sendBanNotifications(id);
        return ResponseEntity.ok("Đã gửi lệnh gửi email cảnh báo cho các sinh viên bị cấm thi.");
    }
}