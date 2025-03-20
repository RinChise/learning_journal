package com.academy.learning_journal.service.impl;

import com.academy.learning_journal.entity.JournalEntry;
import com.academy.learning_journal.entity.Topic;
import com.academy.learning_journal.entity.User;
import com.academy.learning_journal.repository.EntryRepository;
import com.academy.learning_journal.repository.TopicRepository;
import com.academy.learning_journal.repository.UserRepository;
import com.academy.learning_journal.service.EntryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EntryServiceImpl implements EntryService {

    private final EntryRepository entryRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    public EntryServiceImpl(EntryRepository entryRepository, UserRepository userRepository, TopicRepository topicRepository) {
        this.entryRepository = entryRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public List<JournalEntry> findEntries(UUID authorId, UUID topicId) {
        List<JournalEntry> entries;

        // Apply filters if provided
        if (authorId != null && topicId != null) {
            // Filter by both author and topic
            entries = entryRepository.findByAuthorIdAndTopicId(authorId, topicId);
        } else if (authorId != null) {
            // Filter by author only
            entries = entryRepository.findByAuthorId(authorId);
        } else if (topicId != null) {
            // Filter by topic only
            entries = entryRepository.findByTopicId(topicId);
        } else {
            // No filters, get all entries
            entries = entryRepository.findAll();
        }

        // Sort entries by createdAt in descending order (newest first)
        entries.sort(Comparator.comparing(JournalEntry::getCreatedAt).reversed());
        return entries;
    }

    @Override
    public JournalEntry findById(UUID id) {
        return entryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Topic> findAllTopicsSorted() {
        List<Topic> topics = topicRepository.findAll();
        topics.sort(Comparator.comparing(Topic::getType));
        return topics;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean canModifyEntry(JournalEntry entry, Authentication authentication) {
        if (entry == null || authentication == null) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = entry.getAuthor().getName().equals(authentication.getName());

        return isAdmin || isOwner;
    }

    @Override
    public JournalEntry createEntry(String content, UUID topicId, String username) {
        return userRepository.findByName(username).map(author -> {
            JournalEntry entry = new JournalEntry();
            entry.setId(UUID.randomUUID());
            entry.setAuthor(author);
            entry.setContent(content);
            entry.setCreatedAt(LocalDateTime.now());

            // Set topic if selected
            if (topicId != null) {
                topicRepository.findById(topicId).ifPresent(entry::setTopic);
            }

            return entryRepository.save(entry);
        }).orElse(null);
    }

    @Override
    public JournalEntry updateEntry(UUID id, String content, UUID topicId, Authentication authentication) {
        Optional<JournalEntry> optionalEntry = entryRepository.findById(id);

        if (optionalEntry.isEmpty()) {
            return null;
        }

        JournalEntry entry = optionalEntry.get();

        // Check if user is admin or the author of the entry
        if (!canModifyEntry(entry, authentication)) {
            return null;
        }

        entry.setContent(content);
        entry.setUpdatedAt(LocalDateTime.now());

        // Update topic
        if (topicId != null) {
            topicRepository.findById(topicId).ifPresent(entry::setTopic);
        } else {
            entry.setTopic(null);
        }

        return entryRepository.save(entry);
    }

    @Override
    public boolean deleteEntry(UUID id, Authentication authentication) {
        Optional<JournalEntry> optionalEntry = entryRepository.findById(id);

        if (optionalEntry.isEmpty()) {
            return false;
        }

        JournalEntry entry = optionalEntry.get();

        // Check if user is admin or the author of the entry
        if (!canModifyEntry(entry, authentication)) {
            return false;
        }

        entryRepository.deleteById(id);
        return true;
    }
}