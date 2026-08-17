package com.example.engine.controller;

import com.example.engine.dto.CashbackRequest;
import com.example.engine.dto.GoalCheckRequest;
import com.example.engine.dto.GrantRewardRequest;
import com.example.engine.dto.ReferralRewardsResponse;
import com.example.engine.dto.RewardOutcome;
import com.example.engine.engine.CashbackEngine;
import com.example.engine.engine.GoalEngine;
import com.example.engine.engine.ReferralEngine;
import com.example.engine.engine.RewardEngine;
import com.example.engine.entity.GoalRule;
import com.example.engine.entity.Reward;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engine")
public class EngineApiController {

    private final CashbackEngine cashbackEngine;
    private final ReferralEngine referralEngine;
    private final GoalEngine goalEngine;
    private final RewardEngine rewardEngine;

    public EngineApiController(CashbackEngine cashbackEngine,
                               ReferralEngine referralEngine,
                               GoalEngine goalEngine,
                               RewardEngine rewardEngine) {
        this.cashbackEngine = cashbackEngine;
        this.referralEngine = referralEngine;
        this.goalEngine = goalEngine;
        this.rewardEngine = rewardEngine;
    }

    @PostMapping("/cashback")
    public RewardOutcome calculateCashback(@RequestBody CashbackRequest request) {
        return cashbackEngine.apply(request.getType(), request.getAmount());
    }

    @GetMapping("/referral-rewards")
    public ReferralRewardsResponse referralRewards() {
        return new ReferralRewardsResponse(
                referralEngine.rewardForNewUser(),
                referralEngine.rewardForReferrer());
    }

    @PostMapping("/goal")
    public ResponseEntity<GoalRule> checkGoal(@RequestBody GoalCheckRequest request) {
        return goalEngine.check(request.getTransactionCount())
                .map(goal -> ResponseEntity.ok(goal))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/rewards/grant")
    public Reward grantReward(@RequestBody GrantRewardRequest request) {
        return rewardEngine.grant(request.getUserId(), request.getSource(), request.getRewardType(),
                request.getRewardValue(), request.getAmount(), request.getTransactionId());
    }
}