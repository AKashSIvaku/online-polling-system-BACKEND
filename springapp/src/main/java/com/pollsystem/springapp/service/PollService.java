package com.pollsystem.springapp.service;

import com.pollsystem.springapp.dto.request.CreatePollRequest;
import com.pollsystem.springapp.dto.response.OptionResponse;
import com.pollsystem.springapp.dto.response.PollResponse;
import com.pollsystem.springapp.entity.Option;
import com.pollsystem.springapp.entity.Poll;
import com.pollsystem.springapp.entity.User;
import com.pollsystem.springapp.entity.Vote;
import com.pollsystem.springapp.repository.OptionRepository;
import com.pollsystem.springapp.repository.PollRepository;
import com.pollsystem.springapp.repository.UserRepository;
import com.pollsystem.springapp.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PollService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    public PollResponse createPoll(CreatePollRequest request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Poll poll = new Poll(creator, request.getQuestion());
        poll.setPrivacy(Poll.Privacy.valueOf(request.getPrivacy().toUpperCase()));

        Poll savedPoll = pollRepository.save(poll);

        // Create options
        for (String optionText : request.getOptions()) {
            Option option = new Option(savedPoll, optionText);
            optionRepository.save(option);
        }

        return convertToPollResponse(savedPoll, creator);
    }

    public Page<PollResponse> getPublicPolls(Pageable pageable, String userEmail) {
        User currentUser = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
        }

        Page<Poll> polls = pollRepository.findByPrivacyOrderByCreatedAtDesc(Poll.Privacy.PUBLIC, pageable);
        final User finalCurrentUser = currentUser;

        return polls.map(poll -> convertToPollResponse(poll, finalCurrentUser));
    }

    public Page<PollResponse> searchPublicPolls(String keyword, Pageable pageable, String userEmail) {
        User currentUser = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
        }

        Page<Poll> polls = pollRepository.findPublicPollsByKeyword(keyword, pageable);
        final User finalCurrentUser = currentUser;

        return polls.map(poll -> convertToPollResponse(poll, finalCurrentUser));
    }

    public Optional<PollResponse> getPollById(Long pollId, String userEmail) {
        Optional<Poll> pollOpt = pollRepository.findById(pollId);
        if (pollOpt.isEmpty()) {
            return Optional.empty();
        }

        Poll poll = pollOpt.get();
        User currentUser = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
        }

        return Optional.of(convertToPollResponse(poll, currentUser));
    }

    public List<PollResponse> getUserPolls(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Poll> polls = pollRepository.findByCreator(user);
        return polls.stream()
                .map(poll -> convertToPollResponse(poll, user))
                .collect(Collectors.toList());
    }

    public PollResponse closePoll(Long pollId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (!poll.getCreator().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized to close this poll");
        }

        poll.setStatus(Poll.Status.CLOSED);
        Poll savedPoll = pollRepository.save(poll);

        return convertToPollResponse(savedPoll, user);
    }

    public void deletePoll(Long pollId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (!poll.getCreator().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized to delete this poll");
        }

        pollRepository.delete(poll);
    }

    private PollResponse convertToPollResponse(Poll poll, User currentUser) {
        PollResponse response = new PollResponse();
        response.setId(poll.getId());
        response.setQuestion(poll.getQuestion());
        response.setPrivacy(poll.getPrivacy().name());
        response.setStatus(poll.getStatus().name());
        response.setCreatorName(poll.getCreator().getName());
        response.setCreatedAt(poll.getCreatedAt());

        // Get options with vote counts
        List<Option> options = optionRepository.findByPollOrderById(poll);
        long totalVotes = voteRepository.countByPoll(poll);
        
        List<OptionResponse> optionResponses = options.stream().map(option -> {
            double percentage = totalVotes > 0 ? (double) option.getVoteCount() / totalVotes * 100 : 0.0;
            return new OptionResponse(option.getId(), option.getText(), option.getVoteCount(), percentage);
        }).collect(Collectors.toList());

        response.setOptions(optionResponses);
        response.setTotalVotes(totalVotes);

        // Check if current user has voted
        if (currentUser != null) {
            boolean hasVoted = voteRepository.existsByPollAndVoter(poll, currentUser);
            response.setHasVoted(hasVoted);
        }

        return response;
    }
}