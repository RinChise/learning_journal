package com.academy.learning_journal.controller;

import com.academy.learning_journal.entity.JournalEntry;
import com.academy.learning_journal.service.EntryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/entries")
public class EntryController {

    private final EntryService entryService;

    public EntryController(EntryService entryService) {
        this.entryService = entryService;
    }

    // READ: Alle Einträge anzeigen
    @GetMapping
    public String listEntries(
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) UUID topicId,
            Model model) {

        model.addAttribute("entries", entryService.findEntries(authorId, topicId));
        model.addAttribute("users", entryService.findAllUsers());
        model.addAttribute("topics", entryService.findAllTopicsSorted());
        model.addAttribute("selectedAuthorId", authorId);
        model.addAttribute("selectedTopicId", topicId);

        return "entries/list";
    }

    // Eintrag anzeigen
    @GetMapping("/{id}")
    public String viewEntry(@PathVariable UUID id, Model model, Authentication authentication) {
        JournalEntry entry = entryService.findById(id);

        if (entry != null) {
            model.addAttribute("entry", entry);
            model.addAttribute("canModify", entryService.canModifyEntry(entry, authentication));
        }

        return "entries/view";
    }

    // Formular für neuen Eintrag
    @GetMapping("/new")
    public String newEntryForm(Model model) {
        model.addAttribute("entry", new JournalEntry());
        model.addAttribute("topics", entryService.findAllTopicsSorted());
        return "entries/form";
    }

    // Eintrag erstellen
    @PostMapping
    public String createEntry(
            @RequestParam String content,
            @RequestParam(required = false) UUID topicId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        JournalEntry entry = entryService.createEntry(content, topicId, authentication.getName());

        if (entry != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Eintrag erfolgreich erstellt.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Erstellen des Eintrags.");
        }

        return "redirect:/entries";
    }

    // Formular zum Bearbeiten
    @GetMapping("/{id}/edit")
    public String editEntryForm(@PathVariable UUID id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        JournalEntry entry = entryService.findById(id);

        if (entry == null) {
            return "redirect:/entries";
        }

        if (!entryService.canModifyEntry(entry, authentication)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sie haben keine Berechtigung, diesen Eintrag zu bearbeiten.");
            return "redirect:/entries/" + id;
        }

        model.addAttribute("entry", entry);
        model.addAttribute("topics", entryService.findAllTopicsSorted());
        return "entries/form";
    }

    // Eintrag aktualisieren
    @PostMapping("/{id}")
    public String updateEntry(
            @PathVariable UUID id,
            @RequestParam String content,
            @RequestParam(required = false) UUID topicId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        JournalEntry updated = entryService.updateEntry(id, content, topicId, authentication);

        if (updated != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Eintrag erfolgreich aktualisiert.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Sie haben keine Berechtigung, diesen Eintrag zu aktualisieren, oder der Eintrag existiert nicht.");
        }

        return "redirect:/entries/" + id;
    }

    // Eintrag löschen
    @GetMapping("/{id}/delete")
    public String deleteEntry(@PathVariable UUID id, Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean deleted = entryService.deleteEntry(id, authentication);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Eintrag erfolgreich gelöscht.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Sie haben keine Berechtigung, diesen Eintrag zu löschen, oder der Eintrag existiert nicht.");
        }

        return "redirect:/entries";
    }
}