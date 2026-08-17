package com.example.transaction.dto;

public class PaymentResponse {

    private Long transactionId;
    private String message;
    private double amount;
    private double cashback;
    private String rewardType;
    private String rewardValue;
    private double referralReward;
    private double goalReward;
    private String goal;
    private double balance;

    public PaymentResponse() {
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public double getReferralReward() {
        return referralReward;
    }

    public void setReferralReward(double referralReward) {
        this.referralReward = referralReward;
    }

    public double getGoalReward() {
        return goalReward;
    }

    public void setGoalReward(double goalReward) {
        this.goalReward = goalReward;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}