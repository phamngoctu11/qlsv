package com.example.qlsv.application.impl;

import com.example.qlsv.application.dto.mapper.CourseMapper;
import com.example.qlsv.application.dto.mapper.UserMapper;
import com.example.qlsv.application.dto.request.CreateCourseRequest;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.application.dto.response.SimpleStudentResponse;
import com.example.qlsv.application.dto.response.StudentAttendanceStat;
import com.example.qlsv.application.service.CourseService;
import com.example.qlsv.domain.exception.BusinessException;
import com.example.qlsv.domain.exception.ResourceNotFoundException;
import com.example.qlsv.domain.model.*;
import com.example.qlsv.domain.model.enums.AttendanceStatus;
import com.example.qlsv.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AttendanceRecordRepository recordRepository; // Dùng để thống kê

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        // 1. Kiểm tra trùng mã lớp
        courseRepository.findByCourseCode(request.getCourseCode())
                .ifPresent(c -> {
                    throw new BusinessException("Mã lớp học phần đã tồn tại: " + c.getCourseCode());
                });

        // 2. Tìm các bảng liên quan
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        Lecturer lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", request.getLecturerId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", request.getSemesterId()));

        // 3. Xử lý Lịch học (Fix lỗi NULL)
        DayOfWeek dayOfWeek;
        LocalTime startTime;
        LocalTime endTime;
        try {
            dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new BusinessException("Lịch học không hợp lệ. Kiểm tra lại Ngày (VD: MONDAY) hoặc Giờ (VD: 09:00:00)");
        }

        // 4. Tạo Entity
        Course newCourse = new Course();
        newCourse.setCourseCode(request.getCourseCode());
        newCourse.setSubject(subject);
        newCourse.setLecturer(lecturer);
        newCourse.setSemester(semester);
        newCourse.setDayOfWeek(dayOfWeek);
        newCourse.setStartTime(startTime);
        newCourse.setEndTime(endTime);

        // 5. Lưu
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
        if (registrationRepository.existsByCourseId(id)) {
            throw new BusinessException("Không thể xóa Lớp học phần đã có sinh viên đăng ký.");
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void registerStudent(Long studentId, Long courseId) {
        if (registrationRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessException("Sinh viên đã đăng ký lớp học này.");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        CourseRegistration registration = new CourseRegistration(student, course);
        registrationRepository.save(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimpleStudentResponse> getStudentsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        return courseRepository.findStudentsByCourseId(courseId).stream()
                .map(userMapper::studentToSimpleStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByLecturer(Long lecturerId) {
        if (!lecturerRepository.existsById(lecturerId)) {
            throw new ResourceNotFoundException("Lecturer", "id", lecturerId);
        }
        return courseRepository.findByLecturerId(lecturerId).stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- LOGIC THỐNG KÊ ---
    @Override
    @Transactional(readOnly = true)
    public List<StudentAttendanceStat> getCourseStatistics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // 1. Tính tổng số buổi học dự kiến
        int totalSessions = calculateTotalSessions(
                course.getSemester().getStartDate(),
                course.getSemester().getEndDate(),
                course.getDayOfWeek()
        );
        if (totalSessions == 0) totalSessions = 1;

        // 2. Tính số buổi đã trôi qua (tính đến hôm nay)
        int passedSessions = calculateTotalSessions(
                course.getSemester().getStartDate(),
                LocalDate.now(),
                course.getDayOfWeek()
        );
        if (passedSessions > totalSessions) passedSessions = totalSessions;

        // 3. Lấy dữ liệu
        List<Student> students = courseRepository.findStudentsByCourseId(courseId);
        List<AttendanceRecord> allRecords = recordRepository.findAll();

        int finalTotalSessions = totalSessions;
        int finalPassedSessions = passedSessions;

        return students.stream().map(student -> {
            // Đếm số lần đi học (Có mặt hoặc Trễ)
            long attendedCount = allRecords.stream()
                    .filter(r -> r.getSession().getCourse().getId().equals(courseId)
                            && r.getStudent().getId().equals(student.getId())
                            && (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE))
                    .count();

            // Tính số buổi vắng thực tế (Dựa trên số buổi đã trôi qua)
            int realAbsentCount = finalPassedSessions - (int) attendedCount;
            if (realAbsentCount < 0) realAbsentCount = 0;

            // Tính % vắng (Dựa trên tổng số buổi cả kỳ)
            double absentPercentage = ((double) realAbsentCount / finalTotalSessions) * 100;
            boolean isBanned = absentPercentage > 30.0;

            return StudentAttendanceStat.builder()
                    .studentId(student.getId())
                    .studentName(student.getLastName() + " " + student.getFirstName())
                    .studentCode(student.getStudentCode())
                    .totalSessions(finalTotalSessions)
                    .attendedSessions((int) attendedCount)
                    .absentSessions(realAbsentCount)
                    .absentPercentage(Math.round(absentPercentage * 10.0) / 10.0)
                    .isBanned(isBanned)
                    .build();
        }).collect(Collectors.toList());
    }

    private int calculateTotalSessions(LocalDate start, LocalDate end, DayOfWeek classDay) {
        if (start.isAfter(end)) return 0;
        LocalDate firstClassDate = start;
        while (firstClassDate.getDayOfWeek() != classDay) {
            firstClassDate = firstClassDate.plusDays(1);
        }
        if (firstClassDate.isAfter(end)) return 0;
        long weeks = ChronoUnit.WEEKS.between(firstClassDate, end);
        return (int) weeks + 1;
    }
}