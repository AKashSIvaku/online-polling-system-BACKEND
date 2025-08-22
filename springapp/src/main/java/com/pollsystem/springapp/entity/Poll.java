package com.pollsystem.springapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "polls")
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Privacy privacy = Privacy.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Option> options = new HashSet<>();

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Vote> votes = new HashSet<>();

    public Poll() {
        this.createdAt = LocalDateTime.now();
    }

    public Poll(User creator, String question) {
        this();
        this.creator = creator;
        this.question = question;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Option> getOptions() {
        return options;
    }

    public void setOptions(Set<Option> options) {
        this.options = options;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public enum Privacy {
        PUBLIC, PRIVATE
    }

    public enum Status {
        OPEN, CLOSED
    }
}