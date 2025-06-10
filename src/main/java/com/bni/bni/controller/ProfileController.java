package com.bni.bni.controller;

import com.bni.bni.entity.Profile;
import com.bni.bni.service.ProfileService;
import com.bni.bni.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> viewProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", 400);
            response.put("message", "Authorization header missing or invalid");
            return ResponseEntity.status(400).body(response);
        }

        String token = authHeader.replace("Bearer ", "").trim();

        if (!jwtUtil.validateToken(token)) {
            response.put("status", 401);
            response.put("message", "Token tidak valid atau expired");
            return ResponseEntity.status(401).body(response);
        }

        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        Long userId = ((Integer) claims.get("user_id")).longValue();

        Profile userProfile = profileService.getProfile(userId);

        if (userProfile == null) {
            response.put("status", 401);
            response.put("message", "User tidak ditemukan");
            return ResponseEntity.status(401).body(response);
        }

        response.put("status", 200);
        response.put("username", claims.getSubject());
        response.put("first_name", userProfile.getFirstName());
        response.put("last_name", userProfile.getLastName());
        response.put("place_of_birth", userProfile.getPlaceOfBirth());
        response.put("date_of_birth", userProfile.getDateOfBirth());
        response.put("created_at", userProfile.getCreatedAt());
        response.put("updated_at", userProfile.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestHeader(value = "Authorization", required = false) String authHeader, @RequestBody Map<String,String> body) {
        Map<String, Object> response = new HashMap<>();
        String firstName = body.get("first_name");
        String lastName = body.get("last_name");
        String placeOfBirth = body.get("place_of_birth");
        LocalDate dateOfBirth = LocalDate.parse(body.get("date_of_birth"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", 400);
            response.put("message", "Authorization header missing or invalid");
            return ResponseEntity.status(400).body(response);
        }

        String token = authHeader.replace("Bearer ", "").trim();

        if (!jwtUtil.validateToken(token)) {
            response.put("status", 401);
            response.put("message", "Token tidak valid atau expired");
            return ResponseEntity.status(401).body(response);
        }

        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        Long userId = ((Integer) claims.get("user_id")).longValue();

        String message = profileService.updateProfile(userId, firstName, lastName, placeOfBirth, dateOfBirth);

        response.put("status", 200);
        response.put("message", message);
        return ResponseEntity.ok().body(response);
    }
}
