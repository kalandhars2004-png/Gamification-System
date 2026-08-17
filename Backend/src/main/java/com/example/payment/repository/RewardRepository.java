package com.example.payment.repository;

import com.example.payment.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByUserIdOrderByDateDesc(Long userId);

    long countByUserId(Long userId);
}
