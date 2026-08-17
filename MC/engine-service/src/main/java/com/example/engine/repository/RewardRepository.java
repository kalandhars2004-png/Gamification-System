package com.example.engine.repository;

import com.example.engine.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByUserIdOrderByDateDesc(Long userId);

    long countByUserId(Long userId);
}