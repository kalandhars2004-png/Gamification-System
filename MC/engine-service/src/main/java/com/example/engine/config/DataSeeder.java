package com.example.engine.config;

import com.example.engine.entity.AppConfig;
import com.example.engine.entity.CashbackRule;
import com.example.engine.entity.GoalRule;
import com.example.engine.entity.ServiceRule;
import com.example.engine.repository.AppConfigRepository;
import com.example.engine.repository.CashbackRuleRepository;
import com.example.engine.repository.GoalRuleRepository;
import com.example.engine.repository.ServiceRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CashbackRuleRepository cashbackRuleRepository;
    private final ServiceRuleRepository serviceRuleRepository;
    private final GoalRuleRepository goalRuleRepository;
    private final AppConfigRepository appConfigRepository;

    public DataSeeder(CashbackRuleRepository cashbackRuleRepository,
                      ServiceRuleRepository serviceRuleRepository,
                      GoalRuleRepository goalRuleRepository,
                      AppConfigRepository appConfigRepository) {
        this.cashbackRuleRepository = cashbackRuleRepository;
        this.serviceRuleRepository = serviceRuleRepository;
        this.goalRuleRepository = goalRuleRepository;
        this.appConfigRepository = appConfigRepository;
    }

    @Override
    public void run(String... args) {
        if (cashbackRuleRepository.count() == 0) {
            cashbackRuleRepository.save(rule(0, 9.99, 0, "COUPON", "COUPON-FREESHIP"));
            cashbackRuleRepository.save(rule(100, 199.99, 15, "CASHBACK", null));
            cashbackRuleRepository.save(rule(200, 499.99, 20, "CASHBACK", null));
            cashbackRuleRepository.save(rule(500, 999.99, 35, "CASHBACK", null));
            cashbackRuleRepository.save(rule(1000, 4999.99, 50, "CASHBACK", null));
            cashbackRuleRepository.save(rule(5000, null, 100, "CASHBACK", null));
        }

        if (serviceRuleRepository.count() == 0) {
            serviceRuleRepository.save(service("ELECTRICITY", 10, 75));
            serviceRuleRepository.save(service("MOBILE", 5, 75));
            serviceRuleRepository.save(service("DTH", 7, 75));
            serviceRuleRepository.save(service("WATER", 8, 75));
            serviceRuleRepository.save(service("BILL", 8, 75));
        }

        if (goalRuleRepository.count() == 0) {
            goalRuleRepository.save(goal(5, 50));
            goalRuleRepository.save(goal(10, 150));
            goalRuleRepository.save(goal(20, 300));
        }

        if (appConfigRepository.count() == 0) {
            appConfigRepository.save(config("referral.reward.newUser", "250"));
            appConfigRepository.save(config("referral.reward.referrer", "500"));
        }
    }

    private CashbackRule rule(double min, Double max, double cashback, String rewardType, String rewardValue) {
        CashbackRule rule = new CashbackRule();
        rule.setMinAmount(min);
        rule.setMaxAmount(max);
        rule.setCashbackAmount(cashback);
        rule.setRewardType(rewardType);
        rule.setRewardValue(rewardValue);
        return rule;
    }

    private ServiceRule service(String type, double percentage, double maxCap) {
        ServiceRule rule = new ServiceRule();
        rule.setServiceType(type);
        rule.setPercentage(percentage);
        rule.setMaxCap(maxCap);
        rule.setRewardType("CASHBACK");
        return rule;
    }

    private GoalRule goal(int count, double reward) {
        GoalRule rule = new GoalRule();
        rule.setTransactionCount(count);
        rule.setReward(reward);
        rule.setRewardType("CASHBACK");
        return rule;
    }

    private AppConfig config(String key, String value) {
        AppConfig config = new AppConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}