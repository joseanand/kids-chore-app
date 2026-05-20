package com.example.kidsapp.controller;

import com.example.kidsapp.repository.ChoreRepository;
import com.example.kidsapp.repository.RewardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ChoreRepository choreRepository;
    private final RewardRepository rewardRepository;

    public HomeController(ChoreRepository choreRepository,
                          RewardRepository rewardRepository) {
        this.choreRepository = choreRepository;
        this.rewardRepository = rewardRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("chores", choreRepository.findAll());
        model.addAttribute("rewards", rewardRepository.findAll());
        model.addAttribute("totalPoints", 120);
        return "home";
    }
}