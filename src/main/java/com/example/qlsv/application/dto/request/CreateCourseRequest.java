package com.example.qlsv.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCourseRequest {
    @NotBlank
    private String courseCode;

    @NotNull
    private Long subjectId;

    @NotBlank // Input bây giờ là String Code
    private String lecturerCode;

    @NotNull
    private Long semesterId;

    @NotBlank
    private String dayOfWeek;
    @NotBlank
    private String startTime;
    @NotBlank
    private String endTime;
}