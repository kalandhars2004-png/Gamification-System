package com.example.payment.engine;

import com.example.payment.dto.RewardOutcome;
import com.example.payment.entity.CashbackRule;
import com.example.payment.entity.ServiceRule;
import com.example.payment.repository.CashbackRuleRepository;
import com.example.payment.repository.ServiceRuleRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CashbackEngine {

    private final CashbackRuleRepository cashbackRuleRepository;
    private final ServiceRuleRepository serviceRuleRepository;

    public CashbackEngine(CashbackRuleRepository cashbackRuleRepository,
                          ServiceRuleRepository serviceRuleRepository) {
        this.cashbackRuleRepository = cashbackRuleRepository;
        this.serviceRuleRepository = serviceRuleRepository;
    }

    public boolean isServiceType(String type) {
        if ("TRANSFER".equalsIgnoreCase(type)) {
            return false;
        }
        return serviceRuleRepository.findByServiceTypeIgnoreCase(type).isPresent();
    }

    public RewardOutcome apply(String type, double amount) {
        if (!"TRANSFER".equalsIgnoreCase(type)) {
            ServiceRule serviceRule = serviceRuleRepository.findByServiceTypeIgnoreCase(type).orElse(null);
            if (serviceRule != null) {
                double cashback = Math.min(amount * serviceRule.getPercentage() / 100, serviceRule.getMaxCap());
                return new RewardOutcome(cashback, serviceRule.getRewardType(), serviceRule.getRewardValue());
            }
        }

        List<CashbackRule> rules = cashbackRuleRepository.findAllByOrderByMinAmountAsc();
        for (CashbackRule rule : rules) {
            boolean withinMin = amount >= rule.getMinAmount();
            boolean withinMax = rule.getMaxAmount() == null || amount <= rule.getMaxAmount();
            if (withinMin && withinMax) {
                return new RewardOutcome(rule.getCashbackAmount(), rule.getRewardType(), rule.getRewardValue());
            }
        }

        return new RewardOutcome(0, "CASHBACK", null);
    }
}
