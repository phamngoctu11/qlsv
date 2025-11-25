package com.example.qlsv.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterStudentRequest {

    @NotNull(message = "ID Sinh viên không được để trống")
    private Long studentId;

    @NotNull(message = "ID Lớp học không được để trống")
    private Long courseId;
    private String hehe;
}