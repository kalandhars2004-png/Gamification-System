package com.example.payment.dto;

import java.util.List;

public class GoalProgressResponse {

    private Long userId;
    private long totalTransactions;
    private List<String> completedGoals;
    private String nextGoal;
    private double nextReward;
    private String progress;

    public GoalProgressResponse() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public List<String> getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(List<String> completedGoals) {
        this.completedGoals = completedGoals;
    }

    public String getNextGoal() {
        return nextGoal;
    }

    public void setNextGoal(String nextGoal) {
        this.nextGoal = nextGoal;
    }

    public double getNextReward() {
        return nextReward;
    }

    public void setNextReward(double nextReward) {
        this.nextReward = nextReward;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
}