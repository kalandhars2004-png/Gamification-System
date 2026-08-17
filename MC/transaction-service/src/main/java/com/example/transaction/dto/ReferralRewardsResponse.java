package com.example.transaction.dto;

public class ReferralRewardsResponse {

    private double newUserReward;
    private double referrerReward;

    public ReferralRewardsResponse() {
    }

    public double getNewUserReward() {
        return newUserReward;
    }

    public void setNewUserReward(double newUserReward) {
        this.newUserReward = newUserReward;
    }

    public double getReferrerReward() {
        return referrerReward;
    }

    public void setReferrerReward(double referrerReward) {
        this.referrerReward = referrerReward;
    }
}