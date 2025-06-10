package com.bni.bni.service;

import com.bni.bni.entity.User;
import com.bni.bni.repository.UserRepository;
import com.bni.bni.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(String email, String username, String password) {
        if (repo.existsByUsername(username) || repo.existsByEmail(email)) {
            return "User already exists";
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setPasswordHash(encoder.encode(password));
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        repo.save(user);

        return "Registered successfully";
    }

    public String login(String username, String password) {
        Optional<User> user = repo.findByUsername(username);
        if (user.isPresent() && encoder.matches(password, user.get().getPasswordHash())) {
            return jwtUtil.generateToken(username, user.get().getId(), user.get().getRole());
        }

        return null;
    }
}
