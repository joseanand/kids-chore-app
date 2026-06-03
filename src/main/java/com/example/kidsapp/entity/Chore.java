package com.example.kidsapp.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Chore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskName;
    private String dayOfWeek; // e.g., "Monday"
    private int points;
    @Transient
    private boolean completed;
    private boolean rewardItem; // true if this represents a redemption reward

    // Links this chore/reward setup to a specific kid's profile
    private String assignedToUsername;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(boolean rewardItem) {
        this.rewardItem = rewardItem;
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }
}