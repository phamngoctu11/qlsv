package com.example.qlsv.infrastructure.config;

import com.example.qlsv.domain.model.User;
import com.example.qlsv.domain.model.enums.Role;
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
        // Tạo Admin (Admin không cần bảng phụ, chỉ cần User)
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("123456"),
                    "admin@test.com",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            System.out.println("--- SEEDER: Đã tạo Admin (admin/123456) ---");
        }
        else if (userRepository.findByUsername("secretary").isEmpty()) {
            User secretary = new User(
                    "secretary",
                    passwordEncoder.encode("123456"),
                    "secretary@test.com",
                    Role.ROLE_SECRETARY
            );
            userRepository.save(secretary);
            System.out.println("--- SEEDER: Đã tạo Thư ký (secretary/123456) ---");
        }

        // (Bạn có thể thêm code seed Student/Lecturer ở đây nếu muốn,
        // nhưng dùng API createUser sẽ an toàn hơn vì nó xử lý cả 2 bảng)
    }
}