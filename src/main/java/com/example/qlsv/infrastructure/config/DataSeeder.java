package com.example.qlsv.infrastructure.config; // (Hoặc com.example.qlsv...)

import com.example.qlsv.domain.model.Admin;
import com.example.qlsv.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem admin đã tồn tại chưa
        if (userRepository.findByUsername("admin").isEmpty()) {

            // Tạo đối tượng Admin mới
            Admin adminUser = new Admin(
                    "admin",
                    // Mã hóa mật khẩu bạn yêu cầu
                    passwordEncoder.encode("123456"),
                    "admin@yourcompany.com" // Email mặc định
            );

            userRepository.save(adminUser);

            System.out.println("=============================================");
            System.out.println("=== Đã tạo tài khoản ADMIN mặc định ===");
            System.out.println("   Username: admin");
            System.out.println("   Password: 123456");
            System.out.println("=============================================");
        }
    }
}