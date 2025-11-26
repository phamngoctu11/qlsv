package com.example.qlsv.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String role; // STUDENT, LECTURER, ADMIN

    // Thông tin chung
    private String firstName;
    private String lastName;

    // --- BẮT BUỘC NẾU LÀ STUDENT ---
    private String studentCode;

    // --- BẮT BUỘC NẾU LÀ LECTURER ---
    private String lecturerCode;
    private String department;
}