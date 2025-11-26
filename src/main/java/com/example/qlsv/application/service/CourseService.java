package com.example.qlsv.application.service;

import com.example.qlsv.application.dto.request.CreateCourseRequest;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.application.dto.response.SimpleStudentResponse;
import com.example.qlsv.application.dto.response.StudentAttendanceStat;

import java.util.List;

public interface CourseService {

    // --- CRUD ---
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse getCourseById(Long id);
    List<CourseResponse> getAllCourses();
    void deleteCourse(Long id);

    // --- NGHIỆP VỤ ---

    // [ĐÃ SỬA]: Tham số đầu tiên đổi từ Long studentId -> String studentCode
    void registerStudent(String studentCode, Long courseId);

    List<SimpleStudentResponse> getStudentsByCourse(Long courseId);
    List<CourseResponse> getCoursesByLecturer(Long lecturerId);

    // --- THỐNG KÊ ---
    List<StudentAttendanceStat> getCourseStatistics(Long courseId);
    void sendBanNotifications(Long courseId);
}