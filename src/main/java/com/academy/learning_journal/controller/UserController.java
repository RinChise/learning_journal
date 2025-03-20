package com.academy.learning_journal.controller;

import com.academy.learning_journal.entity.User;
import com.academy.learning_journal.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // READ - Show all users to everyone, but regular users can only interact with themselves
    @GetMapping
    public String listUsers(Model model, Authentication authentication) {
        // Get the current user's name
        String currentUsername = authentication.getName();

        model.addAttribute("users", userService.findAllSortedByCurrentUser(currentUsername));
        model.addAttribute("isAdmin", userService.isAdmin(authentication));

        return "users/list";
    }

    // Create form - Admin only
    @GetMapping("/new")
    public String newUserForm(Model model, Authentication authentication) {
        if (!userService.isAdmin(authentication)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("user", new User());
        return "users/form";
    }

    // Edit form - Admin can edit any user, regular users can only edit themselves
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable UUID id, Model model, Authentication authentication) {
        if (!userService.canAccessUser(id, authentication)) {
            return "redirect:/access-denied";
        }

        User user = userService.findById(id);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("isAdmin", userService.isAdmin(authentication));
        }
        return "users/form";
    }

    // Delete - Admin only
    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable UUID id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!userService.isAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
        }

        return "redirect:/users";
    }

    // READ - Specific user by ID
    @GetMapping("/{id}")
    public String viewUser(@PathVariable UUID id, Model model, Authentication authentication) {
        if (!userService.canAccessUser(id, authentication)) {
            return "redirect:/access-denied";
        }

        User user = userService.findById(id);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("isAdmin", userService.isAdmin(authentication));
        }

        return "users/view";
    }

    // CREATE - Add new user (admin only)
    @PostMapping
    public String createUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam(required = false) String role,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (!userService.isAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        if (userService.isEmailAlreadyUsed(email, null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Diese E-Mail-Adresse wird bereits verwendet.");
            return "redirect:/users/new?error";
        }

        User user = userService.createUser(name, email, password, role);

        if (user != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich erstellt.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Erstellen des Benutzers.");
        }

        return "redirect:/users";
    }

    // UPDATE - Update existing user
    @PostMapping("/{id}")
    public String updateUser(@PathVariable UUID id,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) String role,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        if (!userService.canAccessUser(id, authentication)) {
            return "redirect:/access-denied";
        }

        // Check if email is already used by another user
        if (userService.isEmailAlreadyUsed(email, id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Diese E-Mail-Adresse wird bereits verwendet.");
            return "redirect:/users/" + id + "/edit?error";
        }

        User updatedUser = userService.updateUser(id, name, email, password, role, authentication);

        if (updatedUser != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich aktualisiert.");
            return "redirect:/users/" + id;
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Aktualisieren des Benutzers.");
            return "redirect:/users/" + id + "/edit?error";
        }
    }
}