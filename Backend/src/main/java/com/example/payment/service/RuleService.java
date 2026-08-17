package com.example.payment.service;

import com.example.payment.entity.AppConfig;
import com.example.payment.entity.CashbackRule;
import com.example.payment.entity.GoalRule;
import com.example.payment.entity.ServiceRule;
import com.example.payment.repository.AppConfigRepository;
import com.example.payment.repository.CashbackRuleRepository;
import com.example.payment.repository.GoalRuleRepository;
import com.example.payment.repository.ServiceRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleService {

    private final CashbackRuleRepository cashbackRuleRepository;
    private final ServiceRuleRepository serviceRuleRepository;
    private final GoalRuleRepository goalRuleRepository;
    private final AppConfigRepository appConfigRepository;

    public RuleService(CashbackRuleRepository cashbackRuleRepository,
                       ServiceRuleRepository serviceRuleRepository,
                       GoalRuleRepository goalRuleRepository,
                       AppConfigRepository appConfigRepository) {
        this.cashbackRuleRepository = cashbackRuleRepository;
        this.serviceRuleRepository = serviceRuleRepository;
        this.goalRuleRepository = goalRuleRepository;
        this.appConfigRepository = appConfigRepository;
    }

    public List<CashbackRule> getCashbackRules() {
        return cashbackRuleRepository.findAllByOrderByMinAmountAsc();
    }

    public CashbackRule addCashbackRule(CashbackRule rule) {
        return cashbackRuleRepository.save(rule);
    }

    public CashbackRule updateCashbackRule(Long id, CashbackRule rule) {
        CashbackRule existing = cashbackRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cashback rule not found with id: " + id));
        existing.setMinAmount(rule.getMinAmount());
        existing.setMaxAmount(rule.getMaxAmount());
        existing.setCashbackAmount(rule.getCashbackAmount());
        existing.setRewardType(rule.getRewardType());
        existing.setRewardValue(rule.getRewardValue());
        return cashbackRuleRepository.save(existing);
    }

    public void deleteCashbackRule(Long id) {
        cashbackRuleRepository.deleteById(id);
    }

    public List<ServiceRule> getServiceRules() {
        return serviceRuleRepository.findAll();
    }

    public ServiceRule addServiceRule(ServiceRule rule) {
        if (serviceRuleRepository.findByServiceTypeIgnoreCase(rule.getServiceType()).isPresent()) {
            throw new RuntimeException("Service rule already exists for type: " + rule.getServiceType());
        }
        return serviceRuleRepository.save(rule);
    }

    public ServiceRule updateServiceRule(Long id, ServiceRule rule) {
        ServiceRule existing = serviceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service rule not found with id: " + id));
        existing.setServiceType(rule.getServiceType());
        existing.setPercentage(rule.getPercentage());
        existing.setMaxCap(rule.getMaxCap());
        existing.setRewardType(rule.getRewardType());
        existing.setRewardValue(rule.getRewardValue());
        return serviceRuleRepository.save(existing);
    }

    public void deleteServiceRule(Long id) {
        serviceRuleRepository.deleteById(id);
    }

    public List<GoalRule> getGoalRules() {
        return goalRuleRepository.findAllByOrderByTransactionCountAsc();
    }

    public GoalRule addGoalRule(GoalRule rule) {
        return goalRuleRepository.save(rule);
    }

    public GoalRule updateGoalRule(Long id, GoalRule rule) {
        GoalRule existing = goalRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal rule not found with id: " + id));
        existing.setTransactionCount(rule.getTransactionCount());
        existing.setReward(rule.getReward());
        existing.setRewardType(rule.getRewardType());
        existing.setRewardValue(rule.getRewardValue());
        return goalRuleRepository.save(existing);
    }

    public void deleteGoalRule(Long id) {
        goalRuleRepository.deleteById(id);
    }

    public List<AppConfig> getConfigs() {
        return appConfigRepository.findAll();
    }

    public AppConfig updateConfig(String key, String value) {
        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found with key: " + key));
        config.setConfigValue(value);
        return appConfigRepository.save(config);
    }
}
