package com.example.engine.controller;

import com.example.engine.entity.Reward;
import com.example.engine.repository.RewardRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardRepository rewardRepository;

    public RewardController(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @GetMapping
    public List<Reward> allRewards() {
        return rewardRepository.findAll();
    }

    @GetMapping("/{id}")
    public Reward rewardById(@PathVariable Long id) {
        return rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reward not found with id: " + id));
    }

    @GetMapping("/user/{userId}")
    public List<Reward> rewardsByUser(@PathVariable Long userId) {
        return rewardRepository.findByUserIdOrderByDateDesc(userId);
    }

    @GetMapping("/user/{userId}/count")
    public Map<String, Long> countByUser(@PathVariable Long userId) {
        return Map.of("count", rewardRepository.countByUserId(userId));
    }
}