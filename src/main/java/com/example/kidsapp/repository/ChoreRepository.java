package com.example.kidsapp.repository;

import com.example.kidsapp.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoreRepository extends JpaRepository<Chore, Long> {
}