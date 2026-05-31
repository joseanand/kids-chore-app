package com.example.kidsapp.repository;

import com.example.kidsapp.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoreRepository extends JpaRepository<Chore, Long> {
    List<Chore> findByAssignedToUsername(String loggedInUsername);
}