package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.request.CreateUserRequest;
import com.example.qlsv.application.dto.request.UpdateUserRequest;
import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
// MẶC ĐỊNH: Cho phép cả Admin và Thư ký truy cập (để Xem danh sách)
@PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
public class UserController {

    private final UserService userService;

    /**
     * [CẬP NHẬT] Chỉ ADMIN mới được phép TẠO người dùng mới.
     * Thư ký bị chặn ở đây.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Xem chi tiết: Cả Admin và Thư ký đều được (kế thừa từ class)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Xem danh sách: Cả Admin và Thư ký đều được (kế thừa từ class)
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Cập nhật: Tạm thời để cả Admin và Thư ký (để sửa lỗi chính tả tên SV chẳng hạn).
     * Nếu bạn muốn chặn Thư ký sửa, hãy thêm @PreAuthorize("hasRole('ADMIN')") vào đây.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Xóa: Chỉ ADMIN mới được phép.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}