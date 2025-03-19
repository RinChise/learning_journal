package com.academy.learning_journal.service;

import com.academy.learning_journal.model.entity.User;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> findAllSortedByCurrentUser(String currentUsername);
    User findById(UUID id);
    boolean isAdmin(Authentication authentication);
    boolean canAccessUser(UUID userId, Authentication authentication);
    User createUser(String name, String email, String password, String role);
    User updateUser(UUID id, String name, String email, String password, String role, Authentication authentication);
    boolean deleteUser(UUID id);
    boolean isEmailAlreadyUsed(String email, UUID excludeUserId);
}