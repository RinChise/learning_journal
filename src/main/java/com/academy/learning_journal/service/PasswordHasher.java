package com.academy.learning_journal.service;

import com.academy.learning_journal.model.entity.User;
import com.academy.learning_journal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordHasher {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordHasher(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Run this when application starts
    @EventListener(ApplicationReadyEvent.class)
    public void hashExistingPasswords() {
        System.out.println("Starting to hash user passwords...");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // Check if the password is not already a BCrypt hash
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                String plainPassword = user.getPassword();
                String hashedPassword = passwordEncoder.encode(plainPassword);
                user.setPassword(hashedPassword);
                userRepository.save(user);
                System.out.println("Hashed password for user: " + user.getName());
            }
        }
        System.out.println("Finished hashing passwords.");
    }
}