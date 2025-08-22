package com.pollsystem.springapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email")
       })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Email
    private String email;

    @NotBlank
    @Size(max = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role = Role.VOTER;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Poll> createdPolls = new HashSet<>();

    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Vote> votes = new HashSet<>();

    public User() {}

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Poll> getCreatedPolls() {
        return createdPolls;
    }

    public void setCreatedPolls(Set<Poll> createdPolls) {
        this.createdPolls = createdPolls;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public enum Role {
        ADMIN, CREATOR, VOTER
    }
}