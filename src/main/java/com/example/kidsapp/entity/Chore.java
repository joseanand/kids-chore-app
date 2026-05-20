package com.example.kidsapp.entity;

import jakarta.persistence.*;

@Entity
public class Chore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer points;

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}