package com.pollsystem.springapp.controller;

import com.pollsystem.springapp.dto.request.CreatePollRequest;
import com.pollsystem.springapp.dto.request.VoteRequest;
import com.pollsystem.springapp.dto.response.MessageResponse;
import com.pollsystem.springapp.dto.response.PollResponse;
import com.pollsystem.springapp.service.PollService;
import com.pollsystem.springapp.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/polls")
public class PollController {

    @Autowired
    private PollService pollService;

    @Autowired
    private VoteService voteService;

    @PostMapping
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<PollResponse> createPoll(@Valid @RequestBody CreatePollRequest createPollRequest,
                                                  Authentication authentication) {
        String userEmail = authentication.getName();
        PollResponse poll = pollService.createPoll(createPollRequest, userEmail);
        return ResponseEntity.ok(poll);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<PollResponse>> getPublicPolls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        String userEmail = authentication != null ? authentication.getName() : null;
        Page<PollResponse> polls = pollService.getPublicPolls(pageable, userEmail);
        return ResponseEntity.ok(polls);
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<PollResponse>> searchPublicPolls(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        String userEmail = authentication != null ? authentication.getName() : null;
        Page<PollResponse> polls = pollService.searchPublicPolls(keyword, pageable, userEmail);
        return ResponseEntity.ok(polls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PollResponse> getPoll(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        Optional<PollResponse> poll = pollService.getPollById(id, userEmail);
        return poll.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-polls")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PollResponse>> getMyPolls(Authentication authentication) {
        String userEmail = authentication.getName();
        List<PollResponse> polls = pollService.getUserPolls(userEmail);
        return ResponseEntity.ok(polls);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<PollResponse> closePoll(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            PollResponse poll = pollService.closePoll(id, userEmail);
            return ResponseEntity.ok(poll);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePoll(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            pollService.deletePoll(id, userEmail);
            return ResponseEntity.ok(new MessageResponse("Poll deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('VOTER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> vote(@PathVariable Long id,
                                               @Valid @RequestBody VoteRequest voteRequest,
                                               Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            MessageResponse response = voteService.castVote(id, voteRequest, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/vote")
    @PreAuthorize("hasRole('VOTER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> removeVote(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            MessageResponse response = voteService.removeVote(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}