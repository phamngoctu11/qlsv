package com.example.qlsv.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "Mã lớp học phần không được để trống")
    private String courseCode;

    @NotNull(message = "ID Môn học không được để trống")
    private Long subjectId;

    @NotNull(message = "ID Giảng viên không được để trống")
    private Long lecturerId;

    @NotNull(message = "ID Học kỳ không được để trống")
    private Long semesterId;

    // --- [MỚI] THÊM LỊCH HỌC ---
    @NotBlank(message = "Ngày trong tuần không được để trống (VD: MONDAY, TUESDAY...)")
    private String dayOfWeek;

    @NotBlank(message = "Giờ bắt đầu không được để trống (VD: 09:00:00)")
    private String startTime; // Sẽ là "09:00:00"

    @NotBlank(message = "Giờ kết thúc không được để trống (VD: 12:00:00)")
    private String endTime;   // Sẽ là "12:00:00"
}