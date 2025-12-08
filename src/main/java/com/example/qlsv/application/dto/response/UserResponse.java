package com.example.qlsv.application.dto.response;

import com.example.qlsv.domain.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role; // Hoặc String role tùy cấu hình của bạn
    private String firstName;
    private String lastName;

    // --- [THÊM 2 TRƯỜNG MỚI] ---
    private String studentCode;
    private String lecturerCode;
}