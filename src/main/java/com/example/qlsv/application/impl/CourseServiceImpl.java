package com.example.qlsv.application.impl;

import com.example.qlsv.application.dto.mapper.CourseMapper;
import com.example.qlsv.application.dto.mapper.UserMapper;
import com.example.qlsv.application.dto.request.CreateCourseRequest;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.application.dto.response.SimpleStudentResponse;
import com.example.qlsv.application.service.CourseService;
import com.example.qlsv.domain.exception.BusinessException;
import com.example.qlsv.domain.exception.ResourceNotFoundException;
import com.example.qlsv.domain.model.*;
import com.example.qlsv.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    // Cần TẤT CẢ các repository và mapper liên quan
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final CourseRegistrationRepository registrationRepository;

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        // 1. Kiểm tra nghiệp vụ: Mã lớp học phần có bị trùng không?
        courseRepository.findByCourseCode(request.getCourseCode())
                .ifPresent(c -> {
                    throw new BusinessException("Mã lớp học phần đã tồn tại: " + c.getCourseCode());
                });

        // 2. Tìm các thực thể liên quan (nếu không tìm thấy sẽ ném 404)
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        Lecturer lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", request.getLecturerId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", request.getSemesterId()));

        // 3. Tạo Entity mới
        DayOfWeek dayOfWeek;
        LocalTime startTime;
        LocalTime endTime;
        try {
            // Chuyển String từ Request sang Enum và LocalTime
            dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new BusinessException("Định dạng ngày giờ không hợp lệ.");
        }

        // Tạo Entity mới
        Course newCourse = new Course();
        newCourse.setCourseCode(request.getCourseCode());
        newCourse.setSubject(subject);
        newCourse.setLecturer(lecturer);
        newCourse.setSemester(semester);

        // --- QUAN TRỌNG: PHẢI GÁN GIÁ TRỊ VÀO ENTITY ---
        newCourse.setDayOfWeek(dayOfWeek);  // <--- Bạn đang thiếu dòng này
        newCourse.setStartTime(startTime);  // <--- Bạn đang thiếu dòng này
        newCourse.setEndTime(endTime);      // <--- Bạn đang thiếu dòng này

        // Lưu vào DB
        Course savedCourse = courseRepository.save(newCourse);

        return courseMapper.toResponse(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", "id", id);
        }

        // Kiểm tra ràng buộc: Lớp này đã có sinh viên đăng ký chưa?
        if (registrationRepository.existsByCourseId(id)) {
            throw new BusinessException("Không thể xóa Lớp học phần đã có sinh viên đăng ký.");
        }

        // (Bạn cũng có thể kiểm tra xem lớp đã có AttendanceSession chưa)

        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void registerStudent(Long studentId, Long courseId) {
        // 1. Kiểm tra xem sinh viên đã đăng ký lớp này chưa
        registrationRepository.findByStudentIdAndCourseId(studentId, courseId)
                .ifPresent(r -> {
                    throw new BusinessException("Sinh viên đã đăng ký lớp học này.");
                });

        // 2. Tìm thực thể
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // 3. Tạo bản ghi đăng ký mới
        CourseRegistration registration = new CourseRegistration(student, course);

        // 4. Lưu
        registrationRepository.save(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimpleStudentResponse> getStudentsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        List<Student> students = courseRepository.findStudentsByCourseId(courseId);

        return students.stream()
                .map(userMapper::studentToSimpleStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByLecturer(Long lecturerId) {
        if (!lecturerRepository.existsById(lecturerId)) {
            throw new ResourceNotFoundException("Lecturer", "id", lecturerId);
        }

        List<Course> courses = courseRepository.findByLecturerId(lecturerId);

        return courses.stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

}