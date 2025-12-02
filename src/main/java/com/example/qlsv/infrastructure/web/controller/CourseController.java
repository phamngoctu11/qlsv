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
    // Thư ký và Admin được tạo lớp học phần
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return new ResponseEntity<>(courseService.createCourse(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'LECTURER', 'STUDENT')")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Xóa lớp vẫn nên để Admin
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register-student")
    // Thư ký và Admin được ghi danh
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<Void> registerStudentToCourse(@Valid @RequestBody RegisterStudentRequest request) {
        courseService.registerStudent(request.getStudentCode(), request.getCourseId());
        return ResponseEntity.ok().build();
    }

    // Các API khác giữ nguyên, chỉ cần update @PreAuthorize nếu cần SECRETARY xem
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'LECTURER')")
    public ResponseEntity<List<SimpleStudentResponse>> getStudentsInCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getStudentsByCourse(id));
    }

    @GetMapping("/by-lecturer/{lecturerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'LECTURER')")
    public ResponseEntity<List<CourseResponse>> getCoursesByLecturer(@PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.getCoursesByLecturer(lecturerId));
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'LECTURER')")
    public ResponseEntity<List<StudentAttendanceStat>> getCourseStats(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseStatistics(id));
    }

    @PostMapping("/{id}/send-ban-notifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'LECTURER')")
    public ResponseEntity<String> sendBanNotifications(@PathVariable Long id) {
        courseService.sendBanNotifications(id);
        return ResponseEntity.ok("Đã gửi lệnh gửi email.");
    }
}