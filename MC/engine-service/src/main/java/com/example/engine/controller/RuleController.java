package com.example.engine.controller;

import com.example.engine.entity.AppConfig;
import com.example.engine.entity.CashbackRule;
import com.example.engine.entity.GoalRule;
import com.example.engine.entity.ServiceRule;
import com.example.engine.repository.AppConfigRepository;
import com.example.engine.repository.CashbackRuleRepository;
import com.example.engine.repository.GoalRuleRepository;
import com.example.engine.repository.ServiceRuleRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final CashbackRuleRepository cashbackRuleRepository;
    private final ServiceRuleRepository serviceRuleRepository;
    private final GoalRuleRepository goalRuleRepository;
    private final AppConfigRepository appConfigRepository;

    public RuleController(CashbackRuleRepository cashbackRuleRepository,
                          ServiceRuleRepository serviceRuleRepository,
                          GoalRuleRepository goalRuleRepository,
                          AppConfigRepository appConfigRepository) {
        this.cashbackRuleRepository = cashbackRuleRepository;
        this.serviceRuleRepository = serviceRuleRepository;
        this.goalRuleRepository = goalRuleRepository;
        this.appConfigRepository = appConfigRepository;
    }

    @GetMapping("/cashback")
    public List<CashbackRule> getCashbackRules() {
        return cashbackRuleRepository.findAllByOrderByMinAmountAsc();
    }

    @PostMapping("/cashback")
    public CashbackRule addCashbackRule(@RequestBody CashbackRule rule) {
        return cashbackRuleRepository.save(rule);
    }

    @PutMapping("/cashback/{id}")
    public CashbackRule updateCashbackRule(@PathVariable Long id, @RequestBody CashbackRule rule) {
        CashbackRule existing = cashbackRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cashback rule not found with id: " + id));
        existing.setMinAmount(rule.getMinAmount());
        existing.setMaxAmount(rule.getMaxAmount());
        existing.setCashbackAmount(rule.getCashbackAmount());
        existing.setRewardType(rule.getRewardType());
        existing.setRewardValue(rule.getRewardValue());
        return cashbackRuleRepository.save(existing);
    }

    @DeleteMapping("/cashback/{id}")
    public void deleteCashbackRule(@PathVariable Long id) {
        cashbackRuleRepository.deleteById(id);
    }

    @GetMapping("/services")
    public List<ServiceRule> getServiceRules() {
        return serviceRuleRepository.findAll();
    }

    @PostMapping("/services")
    public ServiceRule addServiceRule(@RequestBody ServiceRule rule) {
        if (serviceRuleRepository.findByServiceTypeIgnoreCase(rule.getServiceType()).isPresent()) {
            throw new RuntimeException("Service rule already exists for type: " + rule.getServiceType());
        }
        return serviceRuleRepository.save(rule);
    }

    @PutMapping("/services/{id}")
    public ServiceRule updateServiceRule(@PathVariable Long id, @RequestBody ServiceRule rule) {
        ServiceRule existing = serviceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service rule not found with id: " + id));
        existing.setServiceType(rule.getServiceType());
        existing.setPercentage(rule.getPercentage());
        existing.setMaxCap(rule.getMaxCap());
        existing.setRewardType(rule.getRewardType());
        existing.setRewardValue(rule.getRewardValue());
        return serviceRuleRepository.save(existing);
    }

    @DeleteMapping("/services/{id}")
    public void deleteServiceRule(@PathVariable Long id) {
        serviceRuleRepository.deleteById(id);
    }

    @GetMapping("/goals")
    public List<GoalRule> getGoalRules() {
        return goalRuleRepository.findAllByOrderByTransactionCountAsc();
    }

    @PostMapping("/goals")
    public GoalRule addGoalRule(@RequestBody GoalRule rule) {
        return goalRuleRepository.save(rule);
    }

    @PutMapping("/goals/{id}")
    public GoalRule updateGoalRule(@PathVariable Long id, @RequestBody GoalRule rule) {
        GoalRule existing = goalRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal rule not found with id: " + id));
        existing.setTransactionCount(rule.getTransactionCount());
        existing.setReward(rule.getReward());
        existing.setRewardType(rule.getRewardType());
        existing.setRewardValue(rule.getRewardValue());
        return goalRuleRepository.save(existing);
    }

    @DeleteMapping("/goals/{id}")
    public void deleteGoalRule(@PathVariable Long id) {
        goalRuleRepository.deleteById(id);
    }

    @GetMapping("/config")
    public List<AppConfig> getConfigs() {
        return appConfigRepository.findAll();
    }

    @PutMapping("/config/{key}")
    public AppConfig updateConfig(@PathVariable String key, @RequestBody AppConfig request) {
        AppConfig config = appConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found with key: " + key));
        config.setConfigValue(request.getConfigValue());
        return appConfigRepository.save(config);
    }
}