package com.example.qlsv.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentCheckInRequest {

    @NotNull(message = "ID Lớp học phần không được để trống")
    private Long courseId;
}