package com.example.qlsv.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    // Thông tin chung
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    // Phải là: "STUDENT", "LECTURER", hoặc "ADMIN" (Không có tiền tố ROLE_)
    private String role;

    // Thông tin riêng
    private String firstName;
    private String lastName;

    // Dành cho STUDENT
    private String studentCode;

    // Dành cho LECTURER
    private String lecturerCode;
    private String department;
}