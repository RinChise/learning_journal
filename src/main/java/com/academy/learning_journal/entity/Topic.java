package com.academy.learning_journal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "topic")
public class Topic {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="type")
    private String type;
}