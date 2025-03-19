package com.academy.learning_journal.service.impl;

import com.academy.learning_journal.model.entity.User;
import com.academy.learning_journal.repository.UserRepository;
import com.academy.learning_journal.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAllSortedByCurrentUser(String currentUsername) {
        // Get all users
        List<User> allUsers = userRepository.findAll();

        // Rearrange the list to put the current user first
        return allUsers.stream()
                .sorted((u1, u2) -> {
                    if (u1.getName().equals(currentUsername)) return -1;
                    if (u2.getName().equals(currentUsername)) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public boolean canAccessUser(UUID userId, Authentication authentication) {
        // Admins can access any user
        if (isAdmin(authentication)) {
            return true;
        }

        // Regular users can only access their own details
        String username = authentication.getName();
        User currentUser = userRepository.findByName(username).orElse(null);

        return currentUser != null && currentUser.getId().equals(userId);
    }

    @Override
    public User createUser(String name, String email, String password, String role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        // Set default role if not provided
        user.setRole(role != null && !role.isEmpty() ? role : "USER");
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateUser(UUID id, String name, String email, String password, String role, Authentication authentication) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User existingUser = optionalUser.get();

        // Check if email is already used by another user
        if (isEmailAlreadyUsed(email, id)) {
            return null;
        }

        existingUser.setName(name);
        existingUser.setEmail(email);

        // Only update password if provided
        if (password != null && !password.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }

        // Only allow role changes if the current user is an admin
        if (isAdmin(authentication) && role != null && !role.isEmpty()) {
            existingUser.setRole(role);
        }

        return userRepository.save(existingUser);
    }

    @Override
    public boolean deleteUser(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmailAlreadyUsed(String email, UUID excludeUserId) {
        List<User> usersWithEmail = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email) && !u.getId().equals(excludeUserId))
                .toList();

        return !usersWithEmail.isEmpty();
    }
}