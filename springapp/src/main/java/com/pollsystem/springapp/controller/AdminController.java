package com.pollsystem.springapp.controller;

import com.pollsystem.springapp.dto.response.MessageResponse;
import com.pollsystem.springapp.dto.response.PollResponse;
import com.pollsystem.springapp.entity.Poll;
import com.pollsystem.springapp.entity.User;
import com.pollsystem.springapp.repository.PollRepository;
import com.pollsystem.springapp.repository.UserRepository;
import com.pollsystem.springapp.repository.VoteRepository;
import com.pollsystem.springapp.service.PollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private PollService pollService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Get statistics
        long totalUsers = userRepository.count();
        long totalPolls = pollRepository.count();
        long totalVotes = voteRepository.countTotalVotes();
        long activePolls = pollRepository.countByStatus(Poll.Status.OPEN);
        long closedPolls = pollRepository.countByStatus(Poll.Status.CLOSED);
        
        dashboard.put("totalUsers", totalUsers);
        dashboard.put("totalPolls", totalPolls);
        dashboard.put("totalVotes", totalVotes);
        dashboard.put("activePolls", activePolls);
        dashboard.put("closedPolls", closedPolls);
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/polls")
    public ResponseEntity<Page<Poll>> getAllPolls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Poll> polls = pollRepository.findAll(pageable);
        return ResponseEntity.ok(polls);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<MessageResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User.Role newRole = User.Role.valueOf(role.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("User role updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error updating user role: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            userRepository.delete(user);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error deleting user: " + e.getMessage()));
        }
    }

    @PutMapping("/polls/{id}/close")
    public ResponseEntity<MessageResponse> closePoll(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            pollService.closePoll(id, userEmail);
            return ResponseEntity.ok(new MessageResponse("Poll closed successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error closing poll: " + e.getMessage()));
        }
    }

    @DeleteMapping("/polls/{id}")
    public ResponseEntity<MessageResponse> deletePoll(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            pollService.deletePoll(id, userEmail);
            return ResponseEntity.ok(new MessageResponse("Poll deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error deleting poll: " + e.getMessage()));
        }
    }

    @GetMapping("/polls/{id}")
    public ResponseEntity<PollResponse> getPollDetails(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        return pollService.getPollById(id, userEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}