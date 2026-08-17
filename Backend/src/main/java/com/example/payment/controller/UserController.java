package com.example.payment.controller;

import com.example.payment.dto.AddBalanceRequest;
import com.example.payment.dto.DashboardResponse;
import com.example.payment.dto.GoalProgressResponse;
import com.example.payment.dto.LoginRequest;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.dto.UserIdRequest;
import com.example.payment.dto.UserResponse;
import com.example.payment.entity.Reward;
import com.example.payment.entity.Transaction;
import com.example.payment.entity.User;
import com.example.payment.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{id}/dashboard")
    public DashboardResponse dashboard(@PathVariable Long id) {
        return userService.getDashboard(id);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> transactions(@PathVariable Long id) {
        return userService.getTransactions(id);
    }

    @GetMapping("/{id}/rewards")
    public List<Reward> rewards(@PathVariable Long id) {
        return userService.getRewards(id);
    }

    @GetMapping("/{id}/goals")
    public GoalProgressResponse goals(@PathVariable Long id) {
        return userService.getGoalProgress(id);
    }

    @GetMapping("/{id}/referrals")
    public List<User> referrals(@PathVariable Long id) {
        return userService.getReferrals(id);
    }

    @PostMapping("/get")
    public UserResponse getUserByBody(@RequestBody UserIdRequest request) {
        return userService.getUserById(request.getUserId());
    }

    @PostMapping("/get-goals")
    public GoalProgressResponse getGoalsByBody(@RequestBody UserIdRequest request) {
        return userService.getGoalProgress(request.getUserId());
    }

    @PostMapping("/get-rewards")
    public List<Reward> getRewardsByBody(@RequestBody UserIdRequest request) {
        return userService.getRewards(request.getUserId());
    }

    @PostMapping("/{id}/add-balance")
    public ResponseEntity<User> addBalance(@PathVariable Long id, @RequestBody AddBalanceRequest request) {
        User user = userService.addBalance(id, request.getAmount());
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
