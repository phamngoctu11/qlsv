package com.example.qlsv.infrastructure.security;

import com.example.qlsv.domain.model.User;
import com.example.qlsv.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm User trong DB bằng username (như đã định nghĩa trong UserRepository)
        // Model User của chúng ta đã implement UserDetails rồi,
        // nên chúng ta có thể trả về nó trực tiếp.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        return user;
    }
}