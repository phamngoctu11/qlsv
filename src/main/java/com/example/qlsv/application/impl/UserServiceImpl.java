package com.example.qlsv.application.impl;

import com.example.qlsv.application.dto.mapper.UserMapper;
import com.example.qlsv.application.dto.request.CreateUserRequest;
import com.example.qlsv.application.dto.request.UpdateUserRequest; // <-- MỚI
import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.application.service.UserService;
import com.example.qlsv.domain.exception.BusinessException;
import com.example.qlsv.domain.exception.ResourceNotFoundException; // <-- MỚI
import com.example.qlsv.domain.model.Admin;
import com.example.qlsv.domain.model.Lecturer;
import com.example.qlsv.domain.model.Student;
import com.example.qlsv.domain.model.User;
import com.example.qlsv.domain.model.enums.Role;
import com.example.qlsv.domain.repository.CourseRegistrationRepository; // <-- MỚI
import com.example.qlsv.domain.repository.CourseRepository; // <-- MỚI
import com.example.qlsv.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // <-- MỚI
import org.springframework.data.domain.Pageable; // <-- MỚI
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // [MỚI] Cần các repo này để kiểm tra ràng buộc khi xóa
    private final CourseRepository courseRepository;
    private final CourseRegistrationRepository registrationRepository;

    // (Hàm createUser giữ nguyên như cũ)
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Kiểm tra tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username đã tồn tại: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại: " + request.getEmail());
        }

        User newUser;
        Role role;

        try {
            role = Role.valueOf("ROLE_" + request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Vai trò không hợp lệ: " + request.getRole());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 2. Tạo đối tượng User cụ thể dựa trên vai trò
        switch (role) {
            case ROLE_STUDENT:
                Student student = new Student();
                student.setUsername(request.getUsername());
                student.setPassword(encodedPassword);
                student.setEmail(request.getEmail());
                student.setRole(Role.ROLE_STUDENT);
                student.setFirstName(request.getFirstName());
                student.setLastName(request.getLastName());
                student.setStudentCode(request.getStudentCode());
                newUser = student;
                break;

            case ROLE_LECTURER:
                Lecturer lecturer = new Lecturer();
                lecturer.setUsername(request.getUsername());
                lecturer.setPassword(encodedPassword);
                lecturer.setEmail(request.getEmail());
                lecturer.setRole(Role.ROLE_LECTURER);
                lecturer.setFirstName(request.getFirstName());
                lecturer.setLastName(request.getLastName());
                lecturer.setLecturerCode(request.getLecturerCode());
                lecturer.setDepartment(request.getDepartment());
                newUser = lecturer;
                break;

            case ROLE_ADMIN:
                Admin admin = new Admin(
                        request.getUsername(),
                        encodedPassword,
                        request.getEmail()
                );
                newUser = admin;
                break;

            default:
                throw new BusinessException("Vai trò không được hỗ trợ: " + role);
        }

        // 3. Lưu vào DB
        User savedUser = userRepository.save(newUser);

        // 4. Map sang DTO và trả về
        return userMapper.toResponse(savedUser);
    }


    // --- [MỚI] TRIỂN KHAI CÁC HÀM CRUD CÒN LẠI ---

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
        // Lấy dữ liệu phân trang từ repository
        Page<User> userPage = userRepository.findAll(pageable);

        // Chuyển đổi (map) Page<User> thành Page<UserResponse>
        return userPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        // 1. Tìm người dùng
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // 2. Kiểm tra nghiệp vụ (Email có bị trùng với người khác không)
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email đã tồn tại: " + request.getEmail());
            }
            existingUser.setEmail(request.getEmail());
        }

        // 3. Cập nhật thông tin dựa trên vai trò
        if (existingUser instanceof Student student) { // (Java 17+ pattern matching)
            if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
            if (request.getLastName() != null) student.setLastName(request.getLastName());
            if (request.getStudentCode() != null) student.setStudentCode(request.getStudentCode());
        }
        else if (existingUser instanceof Lecturer lecturer) {
            if (request.getFirstName() != null) lecturer.setFirstName(request.getFirstName());
            if (request.getLastName() != null) lecturer.setLastName(request.getLastName());
            if (request.getLecturerCode() != null) lecturer.setLecturerCode(request.getLecturerCode());
            if (request.getDepartment() != null) lecturer.setDepartment(request.getDepartment());
        }
        // (Admin hiện không có trường riêng để cập nhật)

        // 4. Lưu lại
        User updatedUser = userRepository.save(existingUser);

        // 5. Trả về
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // 1. Kiểm tra tồn tại
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // 2. Kiểm tra ràng buộc nghiệp vụ (Xóa an toàn)
        if (user.getRole() == Role.ROLE_LECTURER) {
            if (courseRepository.existsByLecturerId(id)) {
                throw new BusinessException("Không thể xóa Giảng viên đang phụ trách Lớp học phần.");
            }
        }
        else if (user.getRole() == Role.ROLE_STUDENT) {
            if (registrationRepository.existsByStudentId(id)) {
                throw new BusinessException("Không thể xóa Sinh viên đã đăng ký Lớp học phần.");
                // (Bạn cũng có thể kiểm tra AttendanceRecord tại đây)
            }
        }
        else if (user.getRole() == Role.ROLE_ADMIN && id == 1L) {
            // (Giả sử bạn muốn bảo vệ tài khoản admin gốc)
            // throw new BusinessException("Không thể xóa tài khoản admin gốc.");
        }

        // 3. Xóa
        userRepository.delete(user);
    }
}