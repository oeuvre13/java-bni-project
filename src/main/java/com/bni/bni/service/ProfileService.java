package com.bni.bni.service;

import com.bni.bni.entity.Profile;
import com.bni.bni.entity.User;
import com.bni.bni.repository.ProfileRepository;
import com.bni.bni.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private ProfileRepository repo;

    // view profile
    public Profile getProfile(Long id) {
        Optional<Profile> profile = repo.findByUserId(id);
        return profile.get();
    }


    // update profile
    public String updateProfile(Long userId, String firstName, String lastName, String placeOfBirth, LocalDate dateOfBirth) {
        try {
            if (!repo.existsByUserId(userId)) {
                Profile profile = new Profile();
                profile.setUserId(userId);
                profile.setFirstName(firstName);
                profile.setLastName(lastName);
                profile.setPlaceOfBirth(placeOfBirth);
                profile.setDateOfBirth(dateOfBirth);
                profile.setCreatedAt(OffsetDateTime.now());
                profile.setUpdatedAt(OffsetDateTime.now());
                repo.save(profile);
            } else {
                Profile profile = repo.findByUserId(userId).get();
                profile.setFirstName(firstName);
                profile.setLastName(lastName);
                profile.setPlaceOfBirth(placeOfBirth);
                profile.setDateOfBirth(dateOfBirth);
                profile.setUpdatedAt(OffsetDateTime.now());
                repo.save(profile);
            }
            return "Updated successfully";
        } catch (Exception e) {
            return "Error: " + e;
        }
    }
}
