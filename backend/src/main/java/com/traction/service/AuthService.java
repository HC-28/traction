package com.traction.service;

import com.traction.dto.request.LoginRequest;
import com.traction.dto.request.RegisterRequest;
import com.traction.dto.response.AuthResponse;
import com.traction.entity.Role;
import com.traction.entity.User;
import com.traction.exception.DuplicateResourceException;
import com.traction.exception.UnauthorizedException;
import com.traction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
