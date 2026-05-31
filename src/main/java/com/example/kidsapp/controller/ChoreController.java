package com.example.kidsapp.controller;

import com.example.kidsapp.entity.Chore;
import com.example.kidsapp.entity.User;
import com.example.kidsapp.repository.ChoreRepository;
import com.example.kidsapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChoreController {

    @Autowired private ChoreRepository choreRepository;
    @Autowired private UserRepository userRepository;

    // Helper to extract whoever is currently playing the game
    private String getLoggedInUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // --- SEED SYSTEM (Creates standard family configuration if database is empty) ---
    @PostConstruct
    public String initDefaultFamily() {
        if(userRepository.count() == 0) {
            User mom = new User(); mom.setUsername("steffi"); mom.setPassword("superboss"); mom.setRole("ROLE_PARENT");
            User dad = new User(); dad.setUsername("jose"); dad.setPassword("superboss"); dad.setRole("ROLE_PARENT");
            User kid1 = new User(); kid1.setUsername("erin"); kid1.setPassword("erin16"); kid1.setRole("ROLE_KID");
            User kid2 = new User(); kid2.setUsername("odin"); kid2.setPassword("odin19"); kid2.setRole("ROLE_KID");
            userRepository.saveAll(List.of(mom, dad, kid1, kid2));
            return "Family Database Bootstrapped! Try logging in as 'alex' or 'mom'.";
        }
        return "Database already has members.";
    }

    // --- KID ENDPOINTS ---
    @GetMapping("/kid/chores")
    public List<Chore> getMyChores() {
        // Multi-kid support: Kids only see tasks explicitly assigned to them!
        return choreRepository.findByAssignedToUsername(getLoggedInUsername());
    }

    @PostMapping("/kid/complete/{id}")
    public void completeChore(@PathVariable Long id) {
        Chore chore = choreRepository.findById(id).orElseThrow();
        if(!chore.isCompleted()) {
            chore.setCompleted(true);
            choreRepository.save(chore);

            // Add points directly to this specific kid's bank profile account balance
            User kid = userRepository.findByUsername(getLoggedInUsername()).orElseThrow();
            kid.setBalancePoints(kid.getBalancePoints() + chore.getPoints());
            userRepository.save(kid);
        }
    }

    // --- PARENT ENDPOINTS ---
    @GetMapping("/parent/kids")
    public List<User> getAllKids() {
        return userRepository.findAll().stream().filter(u -> u.getRole().equals("ROLE_KID")).toList();
    }

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