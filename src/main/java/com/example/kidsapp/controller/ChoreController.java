package com.example.kidsapp.controller;

import com.example.kidsapp.config.FamilyProperties;
import com.example.kidsapp.entity.Chore;
import com.example.kidsapp.entity.ChoreCompletion;
import com.example.kidsapp.entity.User;
import com.example.kidsapp.repository.ChoreCompletionRepository;
import com.example.kidsapp.repository.ChoreRepository;
import com.example.kidsapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChoreController {

    @Autowired private ChoreRepository choreRepository;
    @Autowired private UserRepository userRepository;
    // Injecting the configurations supplied by your gitignored properties file
    @Autowired private FamilyProperties familyProperties;
    @Autowired
    private ChoreCompletionRepository choreCompletionRepository;

    @Autowired
    private DataSource dataSource;

    // Helper to extract whoever is currently playing the game
    private String getLoggedInUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // --- SEED SYSTEM (Creates standard family configuration if database is empty) ---
    @EventListener(ApplicationReadyEvent.class)
    public String initDefaultFamily() {
        System.out.println("🚀 System fully ready. Checking if user database needs seeding...");
        System.out.println("Init User setup start");
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
        var auth = SecurityContextHolder.getContext().getAuthentication();
        List<Chore> baseChores = choreRepository.findByAssignedToUsername(auth.getName());

        // Calculate current week boundaries
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        for (Chore chore : baseChores) {
            // If it's a reward, it doesn't care about calendar tracking loops
            if (chore.isRewardItem()) {
                chore.setCompleted(false);
                continue;
            }

            // Look for any completion logs stamped inside THIS specific week
            List<ChoreCompletion> completionsThisWeek = choreCompletionRepository
                    .findByChoreIdAndCompletionDateBetween(chore.getId(), startOfWeek, endOfWeek);

            chore.setCompleted(!completionsThisWeek.isEmpty());
        }

        return baseChores;
    }

    @PostMapping("/kid/complete/{id}")
    public String completeChore(@PathVariable Long id) {
        // 1. Log the completion request, but flag approved as FALSE
        ChoreCompletion completion = new ChoreCompletion(id, LocalDate.now());
        completion.setApproved(false);
        choreCompletionRepository.save(completion);

        return "Quest submitted! Awaiting Parent approval... ⏳";
    }

    // --- PARENT ENDPOINTS ---

    @PostMapping("/parent/chore")
    public String createChore(@RequestBody Chore chore) {
        chore.setRewardItem(false);

        // Check if the parent wants to target every kid
        if ("ALL_KIDS".equals(chore.getAssignedToUsername())) {
            List<User> kids = userRepository.findAll().stream()
                    .filter(u -> "ROLE_KID".equals(u.getRole())).toList();

            for (User kid : kids) {
                Chore kidChore = new Chore();
                kidChore.setTaskName(chore.getTaskName());
                kidChore.setDayOfWeek(chore.getDayOfWeek());
                kidChore.setPoints(chore.getPoints());
                kidChore.setCompleted(false);
                kidChore.setRewardItem(false);
                kidChore.setAssignedToUsername(kid.getUsername()); // Assign individually
                choreRepository.save(kidChore);
            }
            return "Chore deployed to all kids successfully! 🚀";
        }

        choreRepository.save(chore);
        return "Chore deployed successfully! 🚀";
    }

    @PostMapping("/parent/reward")
    public String createReward(@RequestBody Chore chore) {
        chore.setRewardItem(true);

        if ("ALL_KIDS".equals(chore.getAssignedToUsername())) {
            List<User> kids = userRepository.findAll().stream()
                    .filter(u -> "ROLE_KID".equals(u.getRole())).toList();

            for (User kid : kids) {
                Chore kidReward = new Chore();
                kidReward.setTaskName(chore.getTaskName());
                kidReward.setDayOfWeek("");
                kidReward.setPoints(chore.getPoints());
                kidReward.setCompleted(false);
                kidReward.setRewardItem(true);
                kidReward.setAssignedToUsername(kid.getUsername());
                choreRepository.save(kidReward);
            }
            return "Redemption prize deployed to all kids successfully! 🎁";
        }

        choreRepository.save(chore);
        return "Redemption prize deployed successfully! 🎁";
    }


    // Add these endpoints inside your ChoreController class
    @GetMapping("/user/me")
    public Map<String, Object> getLoggedInUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        // Find the full user profile from the database to read their live balance
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("username", user.getUsername());
        userDetails.put("role", user.getRole().replace("ROLE_", ""));
        userDetails.put("balancePoints", user.getBalancePoints()); // <-- Add this line!

        return userDetails;
    }

    @GetMapping("/parent/kids")
    public List<Map<String, Object>> getAllKidsWithPoints() {
        // Finds all registered kid accounts and builds a scoreboard breakdown for the parent
        return userRepository.findAll().stream()
                .filter(u -> "ROLE_KID".equals(u.getRole()))
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    map.put("balancePoints", u.getBalancePoints());
                    return map;
                }).toList();
    }

    // Add this endpoint inside your ChoreController class

    @PostMapping("/parent/bonus")
    public String giveBonusPoints(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        int bonusAmount = Integer.parseInt(payload.get("points").toString());

        User kid = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kid not found: " + username));

        // Apply the bonus directly to their saved profile
        kid.setBalancePoints(kid.getBalancePoints() + bonusAmount);
        userRepository.save(kid);

        return "Successfully awarded " + bonusAmount + " bonus points to " + username + "! 🎉";
    }

    @PostMapping("/parent/backup")
    public String triggerManualBackup() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // FIX: Standard syntax supported across all modern versions of H2
            statement.execute("SCRIPT TO 'backup.sql'");
            return "Database snapshot successfully saved to backup.sql! 💾";
        } catch (Exception e) {
            throw new RuntimeException("Manual backup failed: " + e.getMessage());
        }
    }

    @GetMapping("/parent/chores")
    public List<Chore> getChoresByChild(@RequestParam String username) {
        List<Chore> chores = choreRepository.findByAssignedToUsername(username);

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        for (Chore chore : chores) {
            if (!chore.isRewardItem()) {
                List<ChoreCompletion> completions = choreCompletionRepository
                        .findByChoreIdAndCompletionDateBetween(chore.getId(), startOfWeek, endOfWeek);
                chore.setCompleted(!completions.isEmpty());
            }
        }
        return chores;
    }

    @PostMapping("/parent/approve/{completionId}")
    public String approveChore(
            @PathVariable Long completionId,
            @RequestBody Map<String, Object> payload
    ) {
        ChoreCompletion completion = choreCompletionRepository.findById(completionId)
                .orElseThrow(() -> new RuntimeException("Completion record not found"));
        Chore chore = choreRepository.findById(completion.getChoreId())
                .orElseThrow(() -> new RuntimeException("Associated chore not found"));

        // Check if the parent provided a custom point override value
        int finalPoints = chore.getPoints();
        if (payload.containsKey("overridePoints")) {
            finalPoints = Integer.parseInt(payload.get("overridePoints").toString());
        }

        // 1. Finalize the approval log status
        completion.setApproved(true);
        choreCompletionRepository.save(completion);

        // 2. Disburse the adjusted point total to the kid's ledger balance
        User kid = userRepository.findByUsername(chore.getAssignedToUsername()).orElseThrow();
        kid.setBalancePoints(kid.getBalancePoints() + finalPoints);

        userRepository.save(kid);
        return "Quest verified! Awarded " + finalPoints + " points to " + kid.getUsername() + " 💎";
    }

    // Add a quick endpoint for parents to see what is waiting for them
    @GetMapping("/parent/pending-approvals")
    public List<Map<String, Object>> getPendingApprovals() {
        return choreCompletionRepository.findAll().stream()
                .filter(c -> !c.isApproved())
                .map(c -> {
                    Chore chore = choreRepository.findById(c.getChoreId()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("completionId", c.getId());
                    map.put("taskName", chore != null ? chore.getTaskName() : "Unknown Task");
                    map.put("kid", chore != null ? chore.getAssignedToUsername() : "Unknown");
                    map.put("points", chore != null ? chore.getPoints() : 0);
                    return map;
                }).toList();
    }
}