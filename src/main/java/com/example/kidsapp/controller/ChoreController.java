package com.example.kidsapp.controller;

import com.example.kidsapp.entity.Chore;
import com.example.kidsapp.repository.ChoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChoreController {

    @Autowired
    private ChoreRepository choreRepository;

    // --- KID & PARENT ENDPOINTS ---
    @GetMapping("/kid/chores")
    public List<Chore> getAllChores() {
        return choreRepository.findAll();
    }

    @PostMapping("/kid/complete/{id}")
    public Chore completeChore(@PathVariable Long id) {
        Chore chore = choreRepository.findById(id).orElseThrow();
        chore.setCompleted(true);
        return choreRepository.save(chore);
    }

    // --- PARENT CHORE ENDPOINTS ---
    @PostMapping("/parent/chore")
    public Chore createChore(@RequestBody Chore chore) {
        chore.setRewardItem(false);
        return choreRepository.save(chore);
    }

    @PostMapping("/parent/reward")
    public Chore createReward(@RequestBody Chore chore) {
        chore.setRewardItem(true);
        return choreRepository.save(chore);
    }
}
