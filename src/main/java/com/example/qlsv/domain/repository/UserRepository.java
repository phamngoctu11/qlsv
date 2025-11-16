package com.example.qlsv.domain.repository;

import com.example.qlsv.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm kiếm người dùng bằng username (dùng cho Spring Security).
     * Phải dùng Optional để xử lý trường hợp không tìm thấy.
     */
    Optional<User> findByUsername(String username);

    /**
     * (Tùy chọn) Kiểm tra xem email đã tồn tại chưa
     */
    Boolean existsByEmail(String email);

    /**
     * (Tùy chọn) Kiểm tra xem username đã tồn tại chưa
     */
    Boolean existsByUsername(String username);
}