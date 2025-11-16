package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.request.LoginRequest;
import com.example.qlsv.application.dto.response.JwtAuthResponse;
import com.example.qlsv.domain.model.User;
import com.example.qlsv.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Xác thực username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Nếu xác thực thành công, đặt vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Lấy thông tin User từ đối tượng Authentication
        User userDetails = (User) authentication.getPrincipal();

        // Trả về token và thông tin người dùng
        return ResponseEntity.ok(new JwtAuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole().name()
        ));
    }

    // (Bạn có thể thêm API /register tại đây nếu cần)
}