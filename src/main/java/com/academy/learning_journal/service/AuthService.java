package com.academy.learning_journal.service;

import com.academy.learning_journal.entity.User;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface AuthService {
    boolean isAdmin(Authentication authentication);
    boolean isPasswordMatch(String password, String confirmPassword);
    boolean isUsernameExists(String username);
    User registerUser(String name, String email, String password);
    boolean updateUserRole(UUID id, String role);
}