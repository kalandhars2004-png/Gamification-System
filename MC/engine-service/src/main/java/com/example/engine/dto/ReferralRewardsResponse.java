package com.example.engine.dto;

public class ReferralRewardsResponse {

    private double newUserReward;
    private double referrerReward;

    public ReferralRewardsResponse() {
    }

    public ReferralRewardsResponse(double newUserReward, double referrerReward) {
        this.newUserReward = newUserReward;
        this.referrerReward = referrerReward;
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