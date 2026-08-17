package com.example.payment.engine;

import com.example.payment.entity.GoalRule;
import com.example.payment.repository.GoalRuleRepository;
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
