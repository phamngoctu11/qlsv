package com.example.qlsv.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi email cảnh báo cấm thi bất đồng bộ (Async)
     * để không làm chậm luồng chính của hệ thống.
     */
    @Async
    public void sendBanNotification(String toEmail, String studentName, String courseName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("CẢNH BÁO HỌC VỤ: Thông báo Cấm thi môn " + courseName);
            message.setText("Chào sinh viên " + studentName + ",\n\n" +
                    "Hệ thống ghi nhận bạn đã vắng quá 30% số buổi học của môn: " + courseName + ".\n" +
                    "Theo quy chế, bạn đã bị loại khỏi học phần này và cần đăng ký học lại vào học kỳ sau.\n\n" +
                    "Trân trọng,\n" +
                    "Phòng Đào tạo.");

            mailSender.send(message);
            System.out.println("--- EMAIL SERVICE: Đã gửi cảnh báo tới: " + toEmail + " ---");
        } catch (Exception e) {
            System.err.println("--- EMAIL SERVICE LỖI: Không thể gửi mail tới " + toEmail + ": " + e.getMessage());
        }
    }
}