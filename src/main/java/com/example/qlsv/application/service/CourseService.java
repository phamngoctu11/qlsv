package com.example.qlsv.application.service;

import com.example.qlsv.application.dto.request.CreateCourseRequest;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.application.dto.response.SimpleStudentResponse;
import java.util.List;

public interface CourseService {

    // --- CRUD ---
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse getCourseById(Long id);
    List<CourseResponse> getAllCourses();
    void deleteCourse(Long id);

    // --- NGHIỆP VỤ ---
    void registerStudent(Long studentId, Long courseId);
    List<SimpleStudentResponse> getStudentsByCourse(Long courseId);
    List<CourseResponse> getCoursesByLecturer(Long lecturerId);
}