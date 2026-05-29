package com.routeplanner.route_planner.controller;

import com.routeplanner.route_planner.model.User;
import com.routeplanner.route_planner.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // SIGNUP
    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody User user) {

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return Map.of("message", "Username already exists");
        }

        userRepository.save(user);

        return Map.of("message", "Signup successful");
    }

    // LOGIN
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {

        User existingUser = userRepository
                .findByUsername(user.getUsername())
                .orElse(null);

        if (existingUser == null) {
            return Map.of("message", "User not found");
        }

        if (!existingUser.getPassword().equals(user.getPassword())) {
            return Map.of("message", "Invalid password");
        }

        return Map.of("message", "Login successful");
    }
}