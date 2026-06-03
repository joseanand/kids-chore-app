package com.example.kidsapp.repository;

import com.example.kidsapp.entity.ChoreCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ChoreCompletionRepository extends JpaRepository<ChoreCompletion, Long> {
    List<ChoreCompletion> findByChoreIdAndCompletionDateBetween(Long choreId, LocalDate start, LocalDate end);
}
