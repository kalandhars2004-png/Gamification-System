package com.example.transaction.service;

import com.example.transaction.dto.GoalRuleDto;
import com.example.transaction.dto.PaymentRequest;
import com.example.transaction.dto.PaymentResponse;
import com.example.transaction.dto.ReferralRewardsResponse;
import com.example.transaction.dto.RewardOutcome;
import com.example.transaction.dto.UserDto;
import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${engine.service.url}")
    private String engineServiceUrl;

    public TransactionService(TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new RuntimeException("Transaction type is required");
        }

        UserDto payer = getUser(request.getUserId());

        UserDto recipient = null;
        if ("TRANSFER".equalsIgnoreCase(request.getType())) {
            if (request.getToUserId() == null) {
                throw new RuntimeException("toUserId is required for TRANSFER");
            }
            recipient = getUser(request.getToUserId());
        }

        post(userServiceUrl + "/api/internal/users/" + payer.getId() + "/debit?amount=" + request.getAmount());

        if (recipient != null) {
            post(userServiceUrl + "/api/internal/users/" + recipient.getId() + "/credit?amount=" + request.getAmount());
        }

        RewardOutcome outcome = postForObject(engineServiceUrl + "/api/engine/cashback",
                Map.of("type", request.getType(), "amount", request.getAmount()), RewardOutcome.class);
        if (outcome == null) {
            outcome = new RewardOutcome();
        }

        double credited = 0;
        if ("CASHBACK".equalsIgnoreCase(outcome.getRewardType()) && outcome.getCashback() > 0) {
            post(userServiceUrl + "/api/internal/users/" + payer.getId() + "/credit?amount=" + outcome.getCashback());
            credited = outcome.getCashback();
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(payer.getId());
        transaction.setToUserId(recipient != null ? recipient.getId() : null);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType().toUpperCase());
        transaction.setCashback(credited);
        transaction.setDate(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        Long transactionId = transaction.getId();

        String cashbackSource = "TRANSACTION_CASHBACK";
        if (isServiceType(request.getType())) {
            cashbackSource = "SERVICE_CASHBACK";
        }
        grantReward(payer.getId(), cashbackSource, outcome.getRewardType(), outcome.getRewardValue(),
                credited, transactionId);

        long count = transactionRepository.countByUserId(payer.getId());
        double referralReward = 0;
        if (count == 1 && payer.getReferredBy() != null && !payer.getReferredBy().isBlank()) {
            ReferralRewardsResponse referralRewards = postForObject(
                    engineServiceUrl + "/api/engine/referral-rewards", null, ReferralRewardsResponse.class);

            double newUserReward = referralRewards != null ? referralRewards.getNewUserReward() : 250;
            post(userServiceUrl + "/api/internal/users/" + payer.getId() + "/credit?amount=" + newUserReward);
            grantReward(payer.getId(), "REFERRAL_NEW_USER", "CASHBACK", null, newUserReward, transactionId);

            try {
                UserDto referrer = restTemplate.getForObject(
                        userServiceUrl + "/api/internal/users/referral/" + payer.getReferredBy(), UserDto.class);
                if (referrer != null) {
                    double referrerReward = referralRewards != null ? referralRewards.getReferrerReward() : 500;
                    post(userServiceUrl + "/api/internal/users/" + referrer.getId() + "/credit?amount=" + referrerReward);
                    grantReward(referrer.getId(), "REFERRAL_REFERRER", "CASHBACK", null, referrerReward, transactionId);
                }
            } catch (HttpClientErrorException ignored) {
            }
            referralReward = newUserReward;
        }

        GoalRuleDto goalRule = checkGoal((int) count);
        double goalReward = 0;
        String goalMessage = null;
        if (goalRule != null) {
            goalReward = goalRule.getReward();
            if ("CASHBACK".equalsIgnoreCase(goalRule.getRewardType())) {
                post(userServiceUrl + "/api/internal/users/" + payer.getId() + "/credit?amount=" + goalReward);
            }
            grantReward(payer.getId(), "GOAL_REWARD", goalRule.getRewardType(),
                    goalRule.getRewardValue(), goalReward, transactionId);
            goalMessage = count + " transactions completed";
        }

        UserDto updatedPayer = getUser(payer.getId());

        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transactionId);
        response.setMessage("Payment successful");
        response.setAmount(request.getAmount());
        response.setCashback(credited);
        response.setRewardType(outcome.getRewardType());
        response.setRewardValue(outcome.getRewardValue());
        response.setReferralReward(referralReward);
        response.setGoalReward(goalReward);
        response.setGoal(goalMessage);
        response.setBalance(updatedPayer.getBalance());
        return response;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> transactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public long countByUser(Long userId) {
        return transactionRepository.countByUserId(userId);
    }

    private boolean isServiceType(String type) {
        if ("TRANSFER".equalsIgnoreCase(type)) {
            return false;
        }
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    engineServiceUrl + "/api/rules/services", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            return false;
        }
    }

    private GoalRuleDto checkGoal(int count) {
        try {
            return restTemplate.postForObject(engineServiceUrl + "/api/engine/goal",
                    Map.of("transactionCount", count), GoalRuleDto.class);
        } catch (HttpClientErrorException ex) {
            return null;
        }
    }

    private void grantReward(Long userId, String source, String rewardType, String rewardValue,
                             double amount, Long transactionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("source", source);
        body.put("rewardType", rewardType);
        body.put("rewardValue", rewardValue);
        body.put("amount", amount);
        body.put("transactionId", transactionId);
        postForObject(engineServiceUrl + "/api/engine/rewards/grant", body, Map.class);
    }

    private UserDto getUser(Long id) {
        UserDto user = restTemplate.getForObject(userServiceUrl + "/api/internal/users/" + id, UserDto.class);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user;
    }

    private void post(String url) {
        postForObject(url, null, Map.class);
    }

    private <T> T postForObject(String url, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType).getBody();
    }
}