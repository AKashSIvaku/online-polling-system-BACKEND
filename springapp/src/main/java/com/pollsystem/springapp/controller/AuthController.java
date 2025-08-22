package com.pollsystem.springapp.controller;

import com.pollsystem.springapp.dto.request.LoginRequest;
import com.pollsystem.springapp.dto.request.SignupRequest;
import com.pollsystem.springapp.dto.response.JwtResponse;
import com.pollsystem.springapp.dto.response.MessageResponse;
import com.pollsystem.springapp.entity.User;
import com.pollsystem.springapp.repository.UserRepository;
import com.pollsystem.springapp.security.jwt.JwtUtils;
import com.pollsystem.springapp.security.services.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        String strRole = signUpRequest.getRole();

        if (strRole == null || strRole.isEmpty()) {
            user.setRole(User.Role.VOTER);
        } else {
            switch (strRole.toUpperCase()) {
                case "ADMIN":
                    user.setRole(User.Role.ADMIN);
                    break;
                case "CREATOR":
                    user.setRole(User.Role.CREATOR);
                    break;
                default:
                    user.setRole(User.Role.VOTER);
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}