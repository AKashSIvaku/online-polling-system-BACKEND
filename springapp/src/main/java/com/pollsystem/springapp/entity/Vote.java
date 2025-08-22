package com.pollsystem.springapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"poll_id", "voter_id"})
       })
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    private LocalDateTime timestamp;

    public Vote() {
        this.timestamp = LocalDateTime.now();
    }

    public Vote(Poll poll, Option option, User voter) {
        this();
        this.poll = poll;
        this.option = option;
        this.voter = voter;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public User getVoter() {
        return voter;
    }

    public void setVoter(User voter) {
        this.voter = voter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}