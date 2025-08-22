package com.pollsystem.springapp.repository;

import com.pollsystem.springapp.entity.Vote;
import com.pollsystem.springapp.entity.Poll;
import com.pollsystem.springapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPollAndVoter(Poll poll, User voter);
    
    boolean existsByPollAndVoter(Poll poll, User voter);
    
    long countByPoll(Poll poll);
    
    @Query("SELECT COUNT(v) FROM Vote v")
    long countTotalVotes();
}