package com.example.engine.engine;

import com.example.engine.entity.GoalRule;
import com.example.engine.repository.GoalRuleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GoalEngine {

    private final GoalRuleRepository goalRuleRepository;

    public GoalEngine(GoalRuleRepository goalRuleRepository) {
        this.goalRuleRepository = goalRuleRepository;
    }

    public Optional<GoalRule> check(int transactionCount) {
        return goalRuleRepository.findByTransactionCount(transactionCount);
    }
}