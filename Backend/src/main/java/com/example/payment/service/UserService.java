package com.example.payment.service;

import com.example.payment.dto.DashboardResponse;
import com.example.payment.dto.GoalProgressResponse;
import com.example.payment.dto.LoginRequest;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.dto.UserResponse;
import com.example.payment.entity.GoalRule;
import com.example.payment.entity.Reward;
import com.example.payment.entity.Transaction;
import com.example.payment.entity.User;
import com.example.payment.repository.RewardRepository;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.repository.GoalRuleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RewardRepository rewardRepository;
    private final GoalRuleRepository goalRuleRepository;

    public UserService(UserRepository userRepository,
                       TransactionRepository transactionRepository,
                       RewardRepository rewardRepository,
                       GoalRuleRepository goalRuleRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.rewardRepository = rewardRepository;
        this.goalRuleRepository = goalRuleRepository;
    }

    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String referredBy = null;
        if (request.getReferredBy() != null && !request.getReferredBy().isBlank()) {
            String enteredReferralId = request.getReferredBy();
            User referrer = userRepository.findByReferralId(enteredReferralId)
                    .orElseThrow(() -> new RuntimeException("Invalid referral ID: " + enteredReferralId));
            referredBy = referrer.getReferralId();
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setAccountNo(request.getAccountNo());
        user.setBalance(request.getBalance());
        user.setReferredBy(referredBy);
        user = userRepository.save(user);

        user.setReferralId("REF" + user.getId());
        user = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("userId", user.getId());
        response.put("referralId", user.getReferralId());
        return response;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("userId", user.getId());
        response.put("name", user.getName());
        return response;
    }

    public DashboardResponse getDashboard(Long userId) {
        User user = getUser(userId);

        DashboardResponse response = new DashboardResponse();
        response.setName(user.getName());
        response.setAccountNo(user.getAccountNo());
        response.setBalance(user.getBalance());
        response.setTotalTransactions(transactionRepository.countByUserId(userId));
        response.setTotalRewards(rewardRepository.countByUserId(userId));
        response.setReferralId(user.getReferralId());
        return response;
    }

    public List<Transaction> getTransactions(Long userId) {
        getUser(userId);
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Reward> getRewards(Long userId) {
        getUser(userId);
        return rewardRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<User> getReferrals(Long userId) {
        User user = getUser(userId);
        return userRepository.findByReferredBy(user.getReferralId());
    }

    public GoalProgressResponse getGoalProgress(Long userId) {
        getUser(userId);
        long totalTransactions = transactionRepository.countByUserId(userId);

        List<GoalRule> rules = goalRuleRepository.findAllByOrderByTransactionCountAsc();
        List<String> completed = new ArrayList<>();
        GoalRule next = null;
        for (GoalRule rule : rules) {
            if (totalTransactions >= rule.getTransactionCount()) {
                completed.add(rule.getTransactionCount() + " transactions completed");
            } else if (next == null) {
                next = rule;
            }
        }

        GoalProgressResponse response = new GoalProgressResponse();
        response.setUserId(userId);
        response.setTotalTransactions(totalTransactions);
        response.setCompletedGoals(completed);
        if (next != null) {
            response.setNextGoal(next.getTransactionCount() + " transactions");
            response.setNextReward(next.getReward());
            response.setProgress(totalTransactions + " of " + next.getTransactionCount() + " transactions");
        } else {
            response.setNextGoal("All goals completed");
            response.setNextReward(0);
            response.setProgress(totalTransactions + " transactions");
        }
        return response;
    }

    public User addBalance(Long userId, double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        User user = getUser(userId);
        user.setBalance(user.getBalance() + amount);
        return userRepository.save(user);
    }

    public UserResponse getUserById(Long userId) {
        User user = getUser(userId);
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAccountNo(user.getAccountNo());
        response.setBalance(user.getBalance());
        response.setReferralId(user.getReferralId());
        response.setReferredBy(user.getReferredBy());
        return response;
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}
