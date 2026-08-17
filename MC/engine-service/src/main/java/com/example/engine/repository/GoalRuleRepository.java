package com.example.engine.repository;

import com.example.engine.entity.GoalRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRuleRepository extends JpaRepository<GoalRule, Long> {

    Optional<GoalRule> findByTransactionCount(int transactionCount);

    List<GoalRule> findAllByOrderByTransactionCountAsc();
}