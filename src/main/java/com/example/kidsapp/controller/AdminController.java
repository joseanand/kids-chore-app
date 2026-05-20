package com.example.kidsapp.controller;

import com.example.kidsapp.entity.Chore;
import com.example.kidsapp.entity.Reward;
import com.example.kidsapp.repository.ChoreRepository;
import com.example.kidsapp.repository.RewardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ChoreRepository choreRepository;
    private final RewardRepository rewardRepository;

    public AdminController(ChoreRepository choreRepository,
                           RewardRepository rewardRepository) {
        this.choreRepository = choreRepository;
        this.rewardRepository = rewardRepository;
    }

    @GetMapping
    public String adminPage() {
        return "admin";
    }

    @PostMapping("/chore")
    public String addChore(@RequestParam String name,
                           @RequestParam Integer points) {

        Chore chore = new Chore();
        chore.setName(name);
        chore.setPoints(points);

        choreRepository.save(chore);

        return "redirect:/admin";
    }

    @PostMapping("/reward")
    public String addReward(@RequestParam String name,
                            @RequestParam Integer pointsRequired) {

        Reward reward = new Reward();
        reward.setName(name);
        reward.setPointsRequired(pointsRequired);

        rewardRepository.save(reward);

        return "redirect:/admin";
    }
}