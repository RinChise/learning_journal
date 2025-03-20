package com.academy.learning_journal.controller;

import com.academy.learning_journal.entity.User;
import com.academy.learning_journal.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }

    @PostMapping("/{id}/role")
    public String updateUserRole(@PathVariable UUID id, @RequestParam String role) {
        // Check if the current user has ADMIN role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!authService.isAdmin(auth)) {
            return "redirect:/access-denied";
        }

        if (authService.updateUserRole(id, role)) {
            // Role updated successfully
        }

        return "redirect:/users/" + id;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {

        // Save input values for repopulating the form in case of validation errors
        redirectAttributes.addAttribute("prevName", name);
        redirectAttributes.addAttribute("prevEmail", email);

        // Validate input
        if (!authService.isPasswordMatch(password, confirmPassword)) {
            redirectAttributes.addAttribute("regError", true);
            redirectAttributes.addAttribute("passwordMismatch", true);
            return "redirect:/login?regError";
        }

        // Check if username already exists
        if (authService.isUsernameExists(name)) {
            redirectAttributes.addAttribute("regError", true);
            redirectAttributes.addAttribute("userExists", true);
            return "redirect:/login?regError";
        }

        // Create new user
        User user = authService.registerUser(name, email, password);

        // Redirect to login page with success message
        redirectAttributes.addAttribute("registered", true);
        return "redirect:/login?registered";
    }
}