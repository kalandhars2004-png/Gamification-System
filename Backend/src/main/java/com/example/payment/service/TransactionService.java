package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.RewardOutcome;
import com.example.payment.engine.CashbackEngine;
import com.example.payment.engine.GoalEngine;
import com.example.payment.engine.ReferralEngine;
import com.example.payment.engine.RewardEngine;
import com.example.payment.entity.GoalRule;
import com.example.payment.entity.Transaction;
import com.example.payment.entity.User;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CashbackEngine cashbackEngine;
    private final ReferralEngine referralEngine;
    private final GoalEngine goalEngine;
    private final RewardEngine rewardEngine;
    private final UserService userService;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository,
                              CashbackEngine cashbackEngine,
                              ReferralEngine referralEngine,
                              GoalEngine goalEngine,
                              RewardEngine rewardEngine,
                              UserService userService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.cashbackEngine = cashbackEngine;
        this.referralEngine = referralEngine;
        this.goalEngine = goalEngine;
        this.rewardEngine = rewardEngine;
        this.userService = userService;
    }

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new RuntimeException("Transaction type is required");
        }

        User payer = userService.getUser(request.getUserId());

        User recipient = null;
        if ("TRANSFER".equalsIgnoreCase(request.getType())) {
            if (request.getToUserId() == null) {
                throw new RuntimeException("toUserId is required for TRANSFER");
            }
            recipient = userService.getUser(request.getToUserId());
        }

        if (request.getAmount() > payer.getBalance()) {
            throw new RuntimeException("Insufficient balance");
        }

        payer.setBalance(payer.getBalance() - request.getAmount());

        if (recipient != null) {
            recipient.setBalance(recipient.getBalance() + request.getAmount());
            userRepository.save(recipient);
        }

        RewardOutcome outcome = cashbackEngine.apply(request.getType(), request.getAmount());
        if ("CASHBACK".equalsIgnoreCase(outcome.getRewardType())) {
            payer.setBalance(payer.getBalance() + outcome.getCashback());
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(payer.getId());
        transaction.setToUserId(recipient != null ? recipient.getId() : null);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType().toUpperCase());
        transaction.setCashback(outcome.getCashback());
        transaction.setDate(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        Long transactionId = transaction.getId();

        String cashbackSource = cashbackEngine.isServiceType(request.getType())
                ? "SERVICE_CASHBACK" : "TRANSACTION_CASHBACK";
        rewardEngine.grant(payer.getId(), cashbackSource, outcome.getRewardType(), outcome.getRewardValue(),
                outcome.getCashback(), transactionId);

        if (transactionRepository.countByUserId(payer.getId()) == 1
                && payer.getReferredBy() != null && !payer.getReferredBy().isBlank()) {
            double newUserReward = referralEngine.rewardForNewUser();
            payer.setBalance(payer.getBalance() + newUserReward);
            rewardEngine.grant(payer.getId(), "REFERRAL_NEW_USER", "CASHBACK", null,
                    newUserReward, transactionId);

            userRepository.findByReferralId(payer.getReferredBy()).ifPresent(referrer -> {
                double referrerReward = referralEngine.rewardForReferrer();
                referrer.setBalance(referrer.getBalance() + referrerReward);
                userRepository.save(referrer);
                rewardEngine.grant(referrer.getId(), "REFERRAL_REFERRER", "CASHBACK", null,
                        referrerReward, transactionId);
            });
        }

        int transactionCount = (int) transactionRepository.countByUserId(payer.getId());
        GoalRule goalRule = goalEngine.check(transactionCount).orElse(null);
        double goalReward = 0;
        String goalMessage = null;
        if (goalRule != null) {
            goalReward = goalRule.getReward();
            if ("CASHBACK".equalsIgnoreCase(goalRule.getRewardType())) {
                payer.setBalance(payer.getBalance() + goalReward);
            }
            rewardEngine.grant(payer.getId(), "GOAL_REWARD", goalRule.getRewardType(),
                    goalRule.getRewardValue(), goalReward, transactionId);
            goalMessage = transactionCount + " transactions completed";
        }

        userRepository.save(payer);

        PaymentResponse response = new PaymentResponse();
        response.setMessage("Payment successful");
        response.setAmount(request.getAmount());
        response.setCashback(outcome.getCashback());
        response.setRewardType(outcome.getRewardType());
        response.setRewardValue(outcome.getRewardValue());
        response.setGoalReward(goalReward);
        response.setBalance(payer.getBalance());
        response.setGoal(goalMessage);
        return response;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByDateDesc();
    }
}
