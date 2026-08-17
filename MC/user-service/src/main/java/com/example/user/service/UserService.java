package com.example.user.service;

import com.example.user.dto.AddBalanceRequest;
import com.example.user.dto.DashboardResponse;
import com.example.user.dto.GoalProgressResponse;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserResponse;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${engine.service.url}")
    private String engineServiceUrl;

    @Value("${transaction.service.url}")
    private String transactionServiceUrl;

    public UserService(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    public UserResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String referredBy = request.getReferredBy();
        if (referredBy != null && !referredBy.isBlank()
                && userRepository.findByReferralId(referredBy).isEmpty()) {
            throw new RuntimeException("Invalid referral id");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(request.getPassword());
        user.setAccountNo("ACC" + System.nanoTime());
        user.setBalance(request.getBalance());
        user.setReferredBy(referredBy != null && !referredBy.isBlank() ? referredBy : null);
        user = userRepository.save(user);
        user.setReferralId("REF" + user.getId());
        userRepository.save(user);

        return toResponse(user);
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return toResponse(user);
    }

    public UserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public UserResponse addBalance(Long id, AddBalanceRequest request) {
        User user = findUser(id);
        user.setBalance(user.getBalance() + request.getAmount());
        return toResponse(userRepository.save(user));
    }

    public DashboardResponse dashboard(Long id) {
        User user = findUser(id);
        DashboardResponse response = new DashboardResponse();
        response.setName(user.getName());
        response.setAccountNo(user.getAccountNo());
        response.setBalance(user.getBalance());
        response.setReferralId(user.getReferralId());
        response.setTotalTransactions(countTransactions(id));
        response.setTotalRewards(countRewards(id));
        return response;
    }

    public GoalProgressResponse goalProgress(Long id) {
        findUser(id);
        long count = countTransactions(id);

        List<Map<String, Object>> goals = fetchGoals();
        List<String> completed = new ArrayList<>();
        Map<String, Object> nextGoal = null;
        for (Map<String, Object> goal : goals) {
            int target = ((Number) goal.get("transactionCount")).intValue();
            if (count >= target) {
                completed.add(target + " transactions - \u20B9" + goal.get("reward"));
            } else if (nextGoal == null) {
                nextGoal = goal;
            }
        }

        GoalProgressResponse response = new GoalProgressResponse();
        response.setUserId(id);
        response.setTotalTransactions(count);
        response.setCompletedGoals(completed);
        if (nextGoal != null) {
            response.setNextGoal(nextGoal.get("transactionCount") + " transactions");
            response.setNextReward(((Number) nextGoal.get("reward")).doubleValue());
            double progress = (double) count / ((Number) nextGoal.get("transactionCount")).doubleValue() * 100;
            response.setProgress(String.format("%.2f", progress) + "%");
        } else {
            response.setProgress("100%");
        }
        return response;
    }

    public List<Map<String, Object>> transactions(Long id) {
        findUser(id);
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    transactionServiceUrl + "/api/internal/transactions?userId=" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> rewards(Long id) {
        findUser(id);
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    engineServiceUrl + "/api/rewards/user/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> referrals(Long id) {
        User user = findUser(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User referred : userRepository.findByReferredBy(user.getReferralId())) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", referred.getId());
            entry.put("name", referred.getName());
            entry.put("email", referred.getEmail());
            result.add(entry);
        }
        return result;
    }

    public UserResponse internalGet(Long id) {
        return toResponse(findUser(id));
    }

    public UserResponse internalGetByReferral(String referralId) {
        User user = userRepository.findByReferralId(referralId)
                .orElseThrow(() -> new RuntimeException("User not found with referral id: " + referralId));
        return toResponse(user);
    }

    @Transactional
    public UserResponse internalDebit(Long id, double amount) {
        User user = findUser(id);
        if (user.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        user.setBalance(user.getBalance() - amount);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse internalCredit(Long id, double amount) {
        User user = findUser(id);
        user.setBalance(user.getBalance() + amount);
        return toResponse(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private long countTransactions(Long id) {
        try {
            Map<String, Object> body = restTemplate.getForObject(
                    transactionServiceUrl + "/api/internal/transactions/count?userId=" + id,
                    Map.class);
            return ((Number) body.get("count")).longValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    private long countRewards(Long id) {
        try {
            Map<String, Object> body = restTemplate.getForObject(
                    engineServiceUrl + "/api/rewards/user/" + id + "/count",
                    Map.class);
            return ((Number) body.get("count")).longValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    private List<Map<String, Object>> fetchGoals() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    engineServiceUrl + "/api/rules/goals",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private UserResponse toResponse(User user) {
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
}