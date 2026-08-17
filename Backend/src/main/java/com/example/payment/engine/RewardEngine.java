package com.example.payment.engine;

import com.example.payment.entity.Reward;
import com.example.payment.repository.RewardRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RewardEngine {

    private final RewardRepository rewardRepository;

    public RewardEngine(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    public Reward grant(Long userId, String source, String rewardType, String rewardValue,
                        double amount, Long transactionId) {
        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setSource(source);
        reward.setRewardType(rewardType);
        reward.setRewardValue(rewardValue);
        reward.setAmount(amount);
        reward.setTransactionId(transactionId);
        reward.setDate(LocalDateTime.now());
        return rewardRepository.save(reward);
    }
}
