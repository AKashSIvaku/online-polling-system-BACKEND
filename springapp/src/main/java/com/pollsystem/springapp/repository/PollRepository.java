package com.pollsystem.springapp.repository;

import com.pollsystem.springapp.entity.Poll;
import com.pollsystem.springapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByCreator(User creator);
    
    Page<Poll> findByPrivacyOrderByCreatedAtDesc(Poll.Privacy privacy, Pageable pageable);
    
    @Query("SELECT p FROM Poll p WHERE p.privacy = 'PUBLIC' AND " +
           "(LOWER(p.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.creator.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Poll> findPublicPollsByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    long countByStatus(Poll.Status status);
}
