package com.pollsystem.springapp.controller;

import com.pollsystem.springapp.dto.response.MessageResponse;
import com.pollsystem.springapp.entity.User;
import com.pollsystem.springapp.entity.Vote;
import com.pollsystem.springapp.repository.UserRepository;
import com.pollsystem.springapp.repository.VoteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole().name());
        profile.put("pollsCreated", user.getCreatedPolls().size());
        profile.put("votesCount", user.getVotes().size());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<MessageResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                user.setName(request.getName().trim());
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // Check if email is already in use by another user
                if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Email is already in use!"));
                }
                user.setEmail(request.getEmail().trim());
            }

            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error updating profile: " + e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify current password
            if (!encoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Current password is incorrect!"));
            }

            // Update password
            user.setPasswordHash(encoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error changing password: " + e.getMessage()));
        }
    }

    @GetMapping("/voting-history")
    public ResponseEntity<List<Vote>> getVotingHistory(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Vote> votes = voteRepository.findAll().stream()
                .filter(vote -> vote.getVoter().getId().equals(user.getId()))
                .toList();

        return ResponseEntity.ok(votes);
    }

    // Inner classes for request DTOs
    public static class UpdateProfileRequest {
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}