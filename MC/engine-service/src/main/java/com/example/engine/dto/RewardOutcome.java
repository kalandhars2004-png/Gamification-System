package com.example.engine.dto;

public class RewardOutcome {

    private double cashback;
    private String rewardType;
    private String rewardValue;

    public RewardOutcome() {
    }

    public RewardOutcome(double cashback, String rewardType, String rewardValue) {
        this.cashback = cashback;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
    }

    public double getCashback() {
        return cashback;
    }

    public void setCashback(double cashback) {
        this.cashback = cashback;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getRewardValue() {
        return rewardValue;
    }

    public void setRewardValue(String rewardValue) {
        this.rewardValue = rewardValue;
    }
}