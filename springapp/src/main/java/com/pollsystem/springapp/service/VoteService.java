package com.pollsystem.springapp.service;

import com.pollsystem.springapp.dto.request.VoteRequest;
import com.pollsystem.springapp.dto.response.MessageResponse;
import com.pollsystem.springapp.entity.Option;
import com.pollsystem.springapp.entity.Poll;
import com.pollsystem.springapp.entity.User;
import com.pollsystem.springapp.entity.Vote;
import com.pollsystem.springapp.repository.OptionRepository;
import com.pollsystem.springapp.repository.PollRepository;
import com.pollsystem.springapp.repository.UserRepository;
import com.pollsystem.springapp.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private OptionRepository optionRepository;

    public MessageResponse castVote(Long pollId, VoteRequest voteRequest, String userEmail) {
        User voter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (poll.getStatus() == Poll.Status.CLOSED) {
            throw new RuntimeException("Poll is closed");
        }

        Option option = optionRepository.findById(voteRequest.getOptionId())
                .orElseThrow(() -> new RuntimeException("Option not found"));

        if (!option.getPoll().getId().equals(pollId)) {
            throw new RuntimeException("Option does not belong to this poll");
        }

        // Check if user has already voted
        Optional<Vote> existingVote = voteRepository.findByPollAndVoter(poll, voter);
        if (existingVote.isPresent()) {
            // Update existing vote
            Vote vote = existingVote.get();
            Option oldOption = vote.getOption();
            
            // Decrease count from old option
            oldOption.decrementVoteCount();
            optionRepository.save(oldOption);
            
            // Increase count for new option
            option.incrementVoteCount();
            optionRepository.save(option);
            
            // Update vote
            vote.setOption(option);
            voteRepository.save(vote);
            
            return new MessageResponse("Vote updated successfully!");
        } else {
            // Create new vote
            Vote vote = new Vote(poll, option, voter);
            voteRepository.save(vote);
            
            // Increment vote count for option
            option.incrementVoteCount();
            optionRepository.save(option);
            
            return new MessageResponse("Vote cast successfully!");
        }
    }

    public MessageResponse removeVote(Long pollId, String userEmail) {
        User voter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (poll.getStatus() == Poll.Status.CLOSED) {
            throw new RuntimeException("Poll is closed");
        }

        Optional<Vote> existingVote = voteRepository.findByPollAndVoter(poll, voter);
        if (existingVote.isEmpty()) {
            throw new RuntimeException("No vote found to remove");
        }

        Vote vote = existingVote.get();
        Option option = vote.getOption();
        
        // Decrease vote count
        option.decrementVoteCount();
        optionRepository.save(option);
        
        // Remove vote
        voteRepository.delete(vote);
        
        return new MessageResponse("Vote removed successfully!");
    }
}