package com.example.payment.engine;

import com.example.payment.repository.AppConfigRepository;
import org.springframework.stereotype.Component;

@Component
public class ReferralEngine {

    private final AppConfigRepository appConfigRepository;

    public ReferralEngine(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    public double rewardForNewUser() {
        return getConfigDouble("referral.reward.newUser", 250);
    }

    public double rewardForReferrer() {
        return getConfigDouble("referral.reward.referrer", 500);
    }

    private double getConfigDouble(String key, double defaultValue) {
        return appConfigRepository.findByConfigKey(key)
                .map(config -> Double.parseDouble(config.getConfigValue()))
                .orElse(defaultValue);
    }
}
