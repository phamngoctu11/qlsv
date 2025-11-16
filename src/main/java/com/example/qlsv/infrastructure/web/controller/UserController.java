package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.request.CreateUserRequest;
import com.example.qlsv.application.dto.request.UpdateUserRequest; // <-- DTO MỚI
import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // <-- Dùng để phân trang
import org.springframework.data.domain.Pageable; // <-- Dùng để phân trang
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Mặc định tất cả API đều yêu cầu ADMIN
public class UserController {

    private final UserService userService;

    /**
     * (Đã có) Tạo một người dùng mới (Student, Lecturer, Admin).
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * [MỚI] Lấy thông tin chi tiết của một người dùng bằng ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * [MỚI] Lấy danh sách tất cả người dùng (có phân trang).
     * Ví dụ: /api/v1/users?page=0&size=10&sort=username,asc
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * [MỚI] Cập nhật thông tin người dùng.
     * (Chỉ cập nhật các trường cơ bản như firstName, lastName, email...)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * [MỚI] Xóa một người dùng.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}