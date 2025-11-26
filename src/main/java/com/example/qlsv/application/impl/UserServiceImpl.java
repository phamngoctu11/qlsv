package com.example.qlsv.application.impl;

import com.example.qlsv.application.dto.mapper.UserMapper;
import com.example.qlsv.application.dto.request.CreateUserRequest;
import com.example.qlsv.application.dto.request.UpdateUserRequest;
import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.application.service.UserService;
import com.example.qlsv.domain.exception.BusinessException;
import com.example.qlsv.domain.exception.ResourceNotFoundException;
import com.example.qlsv.domain.model.Lecturer;
import com.example.qlsv.domain.model.Student;
import com.example.qlsv.domain.model.User;
import com.example.qlsv.domain.model.enums.Role;
import com.example.qlsv.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // Dùng để kiểm tra ràng buộc khi xóa
    private final CourseRepository courseRepository;
    private final CourseRegistrationRepository registrationRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Validate User
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username đã tồn tại: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại: " + request.getEmail());
        }

        Role role;
        try {
            role = Role.valueOf("ROLE_" + request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Vai trò không hợp lệ: " + request.getRole());
        }

        // 2. Tạo User (Auth info)
        User newUser = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                role
        );
        User savedUser = userRepository.save(newUser);

        // 3. Tạo Response ngay lập tức
        UserResponse response = userMapper.toResponse(savedUser);

        // 4. Tạo Hồ sơ chi tiết và Cập nhật vào Response
        if (role == Role.ROLE_STUDENT) {
            if (request.getStudentCode() == null || request.getStudentCode().isBlank()) {
                throw new BusinessException("Mã sinh viên không được để trống");
            }
            if (studentRepository.existsById(request.getStudentCode())) {
                throw new BusinessException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
            }

            Student student = new Student();
            student.setStudentCode(request.getStudentCode()); // PK
            student.setFirstName(request.getFirstName());
            student.setLastName(request.getLastName());
            student.setUser(savedUser); // Link FK

            studentRepository.save(student);

            // [SỬA LỖI Ở ĐÂY]: Set dữ liệu vào DTO trả về, KHÔNG set ngược vào Entity User
            response.setFirstName(student.getFirstName());
            response.setLastName(student.getLastName());

        } else if (role == Role.ROLE_LECTURER) {
            if (request.getLecturerCode() == null || request.getLecturerCode().isBlank()) {
                throw new BusinessException("Mã giảng viên không được để trống");
            }
            if (lecturerRepository.existsById(request.getLecturerCode())) {
                throw new BusinessException("Mã giảng viên đã tồn tại: " + request.getLecturerCode());
            }

            Lecturer lecturer = new Lecturer();
            lecturer.setLecturerCode(request.getLecturerCode()); // PK
            lecturer.setFirstName(request.getFirstName());
            lecturer.setLastName(request.getLastName());
            lecturer.setDepartment(request.getDepartment());
            lecturer.setUser(savedUser); // Link FK

            lecturerRepository.save(lecturer);

            // [SỬA LỖI Ở ĐÂY]: Set dữ liệu vào DTO trả về
            response.setFirstName(lecturer.getFirstName());
            response.setLastName(lecturer.getLastName());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật thông tin chi tiết (Tên, Khoa...)
        if (user.getStudent() != null) {
            Student s = user.getStudent();
            if (request.getFirstName() != null) s.setFirstName(request.getFirstName());
            if (request.getLastName() != null) s.setLastName(request.getLastName());
            // Không cho sửa StudentCode vì là PK
            studentRepository.save(s);
        } else if (user.getLecturer() != null) {
            Lecturer l = user.getLecturer();
            if (request.getFirstName() != null) l.setFirstName(request.getFirstName());
            if (request.getLastName() != null) l.setLastName(request.getLastName());
            if (request.getDepartment() != null) l.setDepartment(request.getDepartment());
            // Không cho sửa LecturerCode vì là PK
            lecturerRepository.save(l);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Xóa an toàn: Tìm hồ sơ liên quan và xóa trước
        studentRepository.findByUserId(id).ifPresent(s -> {
            if (registrationRepository.existsByStudentStudentCode(s.getStudentCode())) {
                throw new BusinessException("Sinh viên đã đăng ký học, không thể xóa.");
            }
            studentRepository.delete(s);
        });

        lecturerRepository.findByUserId(id).ifPresent(l -> {
            if (courseRepository.existsByLecturerLecturerCode(l.getLecturerCode())) {
                throw new BusinessException("Giảng viên đang dạy, không thể xóa.");
            }
            lecturerRepository.delete(l);
        });

        userRepository.deleteById(id);
    }
}