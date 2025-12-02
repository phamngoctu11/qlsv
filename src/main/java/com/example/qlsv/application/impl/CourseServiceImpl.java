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
import com.example.qlsv.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final AttendanceRecordRepository recordRepository;
    private final EmailService emailService;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        courseRepository.findByCourseCode(request.getCourseCode())
                .ifPresent(c -> { throw new BusinessException("Mã lớp tồn tại: " + c.getCourseCode()); });

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        Lecturer lecturer = lecturerRepository.findById(request.getLecturerCode())
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "code", request.getLecturerCode()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", request.getSemesterId()));

        DayOfWeek dayOfWeek;
        LocalTime startTime;
        LocalTime endTime;
        try {
            dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
            startTime = LocalTime.parse(request.getStartTime());
            endTime = LocalTime.parse(request.getEndTime());
        } catch (Exception e) {
            throw new BusinessException("Lịch học không hợp lệ.");
        }

        // --- [MỚI] KIỂM TRA LỊCH GIẢNG VIÊN ---
        checkLecturerScheduleConflict(lecturer.getLecturerCode(), semester.getId(), dayOfWeek, startTime, endTime);
        // -------------------------------------

        Course newCourse = new Course();
        newCourse.setCourseCode(request.getCourseCode());
        newCourse.setSubject(subject);
        newCourse.setLecturer(lecturer);
        newCourse.setSemester(semester);
        newCourse.setDayOfWeek(dayOfWeek);
        newCourse.setStartTime(startTime);
        newCourse.setEndTime(endTime);

        return courseMapper.toResponse(courseRepository.save(newCourse));
    }

    // --- HÀM VALIDATE LỊCH GIẢNG VIÊN ---
    private void checkLecturerScheduleConflict(String lecturerCode, Long semesterId, DayOfWeek newDay, LocalTime newStart, LocalTime newEnd) {
        List<Course> existingCourses = courseRepository.findByLecturerLecturerCodeAndSemesterId(lecturerCode, semesterId);

        for (Course c : existingCourses) {
            if (c.getDayOfWeek() == newDay) {
                boolean isOverlap = newStart.isBefore(c.getEndTime()) && c.getStartTime().isBefore(newEnd);
                if (isOverlap) {
                    throw new BusinessException("Giảng viên bị trùng lịch dạy với lớp: " + c.getCourseCode()
                            + " (" + c.getStartTime() + " - " + c.getEndTime() + ")");
                }
            }
        }
    }

    @Override
    @Transactional
    public void registerStudent(String studentCode, Long courseId) {
        Student student = studentRepository.findById(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "code", studentCode));

        Course courseToRegister = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (registrationRepository.existsByStudentStudentCodeAndCourseId(studentCode, courseId)) {
            throw new BusinessException("Sinh viên đã đăng ký lớp học này.");
        }

        checkStudentScheduleConflict(studentCode, courseToRegister);

        CourseRegistration registration = new CourseRegistration(student, courseToRegister);
        registrationRepository.save(registration);
    }

    // Hàm check lịch sinh viên (đổi tên cho rõ ràng)
    private void checkStudentScheduleConflict(String studentCode, Course newCourse) {
        List<Course> registeredCourses = courseRepository.findCoursesByStudentCodeAndSemesterId(
                studentCode, newCourse.getSemester().getId());

        for (Course existingCourse : registeredCourses) {
            if (existingCourse.getDayOfWeek() == newCourse.getDayOfWeek()) {
                boolean isOverlap = newCourse.getStartTime().isBefore(existingCourse.getEndTime())
                        && existingCourse.getStartTime().isBefore(newCourse.getEndTime());

                if (isOverlap) {
                    throw new BusinessException("Sinh viên bị trùng lịch học với lớp: " + existingCourse.getCourseCode());
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) throw new ResourceNotFoundException("Course", "id", id);
        if (registrationRepository.existsByCourseId(id)) throw new BusinessException("Không thể xóa Lớp học phần đã có sinh viên đăng ký.");
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimpleStudentResponse> getStudentsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) throw new ResourceNotFoundException("Course", "id", courseId);
        return courseRepository.findStudentsByCourseId(courseId).stream()
                .map(userMapper::studentToSimpleStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByLecturer(Long lecturerId) {
        Lecturer lecturer = lecturerRepository.findByUserId(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "userId", lecturerId));
        return courseRepository.findByLecturerLecturerCode(lecturer.getLecturerCode()).stream()
                .map(courseMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAttendanceStat> getCourseStatistics(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        int totalSessions = calculateTotalSessions(course.getSemester().getStartDate(), course.getSemester().getEndDate(), course.getDayOfWeek());
        if (totalSessions == 0) totalSessions = 1;
        int passedSessions = calculateTotalSessions(course.getSemester().getStartDate(), LocalDate.now(), course.getDayOfWeek());
        if (passedSessions > totalSessions) passedSessions = totalSessions;

        List<Student> students = courseRepository.findStudentsByCourseId(courseId);
        List<AttendanceRecord> allRecords = recordRepository.findAll();

        int finalTotal = totalSessions;
        int finalPassed = passedSessions;

        return students.stream().map(student -> {
            long attended = allRecords.stream().filter(r ->
                    r.getSession().getCourse().getId().equals(courseId) &&
                            r.getStudent().getStudentCode().equals(student.getStudentCode()) &&
                            (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE)
            ).count();

            int absent = finalPassed - (int) attended;
            if (absent < 0) absent = 0;
            double pct = ((double) absent / finalTotal) * 100;

            return StudentAttendanceStat.builder()
                    .studentId(student.getUser().getId())
                    .studentCode(student.getStudentCode())
                    .studentName(student.getLastName() + " " + student.getFirstName())
                    .totalSessions(finalTotal)
                    .attendedSessions((int)attended)
                    .absentSessions(absent)
                    .absentPercentage(Math.round(pct * 10.0) / 10.0)
                    .isBanned(pct > 30.0)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendBanNotifications(Long courseId) {
        List<StudentAttendanceStat> stats = getCourseStatistics(courseId);
        for (StudentAttendanceStat stat : stats) {
            if (stat.isBanned()) {
                Student student = studentRepository.findById(stat.getStudentCode()).orElse(null);
                if (student != null && student.getEmail() != null) {
                    emailService.sendBanNotification(student.getEmail(), student.getLastName(), "Môn học");
                }
            }
        }
    }

    private int calculateTotalSessions(LocalDate start, LocalDate end, DayOfWeek day) {
        if (start.isAfter(end)) return 0;
        LocalDate date = start;
        while (date.getDayOfWeek() != day) date = date.plusDays(1);
        if (date.isAfter(end)) return 0;
        return (int) ChronoUnit.WEEKS.between(date, end) + 1;
    }
}