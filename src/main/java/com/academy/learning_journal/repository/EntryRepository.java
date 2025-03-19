package com.academy.learning_journal.repository;

import com.academy.learning_journal.model.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

//JPA-Repositories
@Repository
public interface EntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findByAuthorId(UUID authorId);
    List<JournalEntry> findByTopicId(UUID topicId);
    List<JournalEntry> findByAuthorIdAndTopicId(UUID authorId, UUID topicId);
}
