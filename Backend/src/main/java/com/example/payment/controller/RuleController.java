package com.example.payment.controller;

import com.example.payment.dto.ConfigRequest;
import com.example.payment.entity.AppConfig;
import com.example.payment.entity.CashbackRule;
import com.example.payment.entity.GoalRule;
import com.example.payment.entity.ServiceRule;
import com.example.payment.service.RuleService;
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

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping("/cashback")
    public List<CashbackRule> getCashbackRules() {
        return ruleService.getCashbackRules();
    }

    @PostMapping("/cashback")
    public CashbackRule addCashbackRule(@RequestBody CashbackRule rule) {
        return ruleService.addCashbackRule(rule);
    }

    @PutMapping("/cashback/{id}")
    public CashbackRule updateCashbackRule(@PathVariable Long id, @RequestBody CashbackRule rule) {
        return ruleService.updateCashbackRule(id, rule);
    }

    @DeleteMapping("/cashback/{id}")
    public void deleteCashbackRule(@PathVariable Long id) {
        ruleService.deleteCashbackRule(id);
    }

    @GetMapping("/services")
    public List<ServiceRule> getServiceRules() {
        return ruleService.getServiceRules();
    }

    @PostMapping("/services")
    public ServiceRule addServiceRule(@RequestBody ServiceRule rule) {
        return ruleService.addServiceRule(rule);
    }

    @PutMapping("/services/{id}")
    public ServiceRule updateServiceRule(@PathVariable Long id, @RequestBody ServiceRule rule) {
        return ruleService.updateServiceRule(id, rule);
    }

    @DeleteMapping("/services/{id}")
    public void deleteServiceRule(@PathVariable Long id) {
        ruleService.deleteServiceRule(id);
    }

    @GetMapping("/goals")
    public List<GoalRule> getGoalRules() {
        return ruleService.getGoalRules();
    }

    @PostMapping("/goals")
    public GoalRule addGoalRule(@RequestBody GoalRule rule) {
        return ruleService.addGoalRule(rule);
    }

    @PutMapping("/goals/{id}")
    public GoalRule updateGoalRule(@PathVariable Long id, @RequestBody GoalRule rule) {
        return ruleService.updateGoalRule(id, rule);
    }

    @DeleteMapping("/goals/{id}")
    public void deleteGoalRule(@PathVariable Long id) {
        ruleService.deleteGoalRule(id);
    }

    @GetMapping("/config")
    public List<AppConfig> getConfigs() {
        return ruleService.getConfigs();
    }

    @PutMapping("/config/{key}")
    public AppConfig updateConfig(@PathVariable String key, @RequestBody ConfigRequest request) {
        return ruleService.updateConfig(key, request.getConfigValue());
    }
}
