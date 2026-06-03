package com.example.kidsapp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ChoreCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long choreId;
    private LocalDate completionDate;

    private boolean approved;

    // Getters, Setters, and Constructors
    public ChoreCompletion() {}

    public ChoreCompletion(Long choreId, LocalDate completionDate) {
        this.choreId = choreId;
        this.completionDate = completionDate;
    }

    public Long getId() { return id; }
    public Long getChoreId() { return choreId; }
    public LocalDate getCompletionDate() { return completionDate; }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChoreId(Long choreId) {
        this.choreId = choreId;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
