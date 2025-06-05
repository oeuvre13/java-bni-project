package com.bni.bni.controller;

import com.bni.bni.service.AuthService;
import com.bni.bni.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String message = authService.register(username, password);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String token = authService.login(username, password);

        Map<String, Object> response = new HashMap<>();
        if (token != null) {
            response.put("status", 200);
            response.put("token", token);
            response.put("message", "Login berhasil deh");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", 401);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", 400);
            response.put("message", "Authorization header missing or invalid");
            return ResponseEntity.status(400).body(response);
        }

        try {
            String token = authHeader.replace("Bearer ", "").trim();

            if (!jwtUtil.validateToken(token)) {
                response.put("status", 401);
                response.put("message", "Token tidak valid atau expired");
                return ResponseEntity.status(401).body(response);
            }

            Claims claims = jwtUtil.getAllClaimsFromToken(token);

            response.put("status", 200);
            response.put("username", claims.getSubject());
            response.put("role", claims.get("role"));
            response.put("issuedAt", claims.getIssuedAt());
            response.put("expiration", claims.getExpiration());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 401);
            response.put("message", "Token tidak valid");
            return ResponseEntity.status(401).body(response);

        }
    }
}
