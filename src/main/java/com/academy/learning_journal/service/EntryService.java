package com.academy.learning_journal.service;

import com.academy.learning_journal.model.entity.JournalEntry;
import com.academy.learning_journal.model.entity.Topic;
import com.academy.learning_journal.model.entity.User;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface EntryService {
    List<JournalEntry> findEntries(UUID authorId, UUID topicId);
    JournalEntry findById(UUID id);
    List<Topic> findAllTopicsSorted();
    List<User> findAllUsers();
    boolean canModifyEntry(JournalEntry entry, Authentication authentication);
    JournalEntry createEntry(String content, UUID topicId, String username);
    JournalEntry updateEntry(UUID id, String content, UUID topicId, Authentication authentication);
    boolean deleteEntry(UUID id, Authentication authentication);
}