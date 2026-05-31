package com.example.kidsapp.controller;

import com.example.kidsapp.config.FamilyProperties;
import com.example.kidsapp.entity.Chore;
import com.example.kidsapp.entity.User;
import com.example.kidsapp.repository.ChoreRepository;
import com.example.kidsapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChoreController {

    @Autowired private ChoreRepository choreRepository;
    @Autowired private UserRepository userRepository;
    // Injecting the configurations supplied by your gitignored properties file
    @Autowired private FamilyProperties familyProperties;

    // Helper to extract whoever is currently playing the game
    private String getLoggedInUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // --- SEED SYSTEM (Creates standard family configuration if database is empty) ---
    @PostConstruct
    public String initDefaultFamily() {
        if (userRepository.count() == 0) {
            List<User> initialUsers = new ArrayList<>();

            // Seed Parents dynamically
            for (FamilyProperties.UserProfile p : familyProperties.getParents()) {
                User parent = new User();
                parent.setUsername(p.getUsername());
                parent.setPassword(p.getPassword());
                parent.setRole("ROLE_PARENT");
                initialUsers.add(parent);
            }

            // Seed Kids dynamically
            for (FamilyProperties.UserProfile k : familyProperties.getKids()) {
                User kid = new User();
                kid.setUsername(k.getUsername());
                kid.setPassword(k.getPassword());
                kid.setRole("ROLE_KID");
                initialUsers.add(kid);
            }

            userRepository.saveAll(initialUsers);
            return "Family Database Bootstrapped cleanly from properties configuration!";
        }
        return "Database already contains family members.";
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