package com.example.user.controller;

import com.example.user.dto.AddBalanceRequest;
import com.example.user.dto.DashboardResponse;
import com.example.user.dto.GoalProgressResponse;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserIdRequest;
import com.example.user.dto.UserResponse;
import com.example.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public UserResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/dashboard")
    public DashboardResponse dashboard(@PathVariable Long id) {
        return userService.dashboard(id);
    }

    @GetMapping("/{id}/transactions")
    public List<Map<String, Object>> transactions(@PathVariable Long id) {
        return userService.transactions(id);
    }

    @GetMapping("/{id}/rewards")
    public List<Map<String, Object>> rewards(@PathVariable Long id) {
        return userService.rewards(id);
    }

    @GetMapping("/{id}/goals")
    public GoalProgressResponse goals(@PathVariable Long id) {
        return userService.goalProgress(id);
    }

    @GetMapping("/{id}/referrals")
    public List<Map<String, Object>> referrals(@PathVariable Long id) {
        return userService.referrals(id);
    }

    @PostMapping("/{id}/add-balance")
    public UserResponse addBalance(@PathVariable Long id, @RequestBody AddBalanceRequest request) {
        return userService.addBalance(id, request);
    }

    @PostMapping("/get")
    public UserResponse getByBody(@RequestBody UserIdRequest request) {
        return userService.getUserById(request.getUserId());
    }

    @PostMapping("/get-goals")
    public GoalProgressResponse getGoalsByBody(@RequestBody UserIdRequest request) {
        return userService.goalProgress(request.getUserId());
    }

    @PostMapping("/get-rewards")
    public List<Map<String, Object>> getRewardsByBody(@RequestBody UserIdRequest request) {
        return userService.rewards(request.getUserId());
    }

    @GetMapping("/internal/users/{id}")
    public UserResponse internalGet(@PathVariable Long id) {
        return userService.internalGet(id);
    }

    @GetMapping("/internal/users/referral/{referralId}")
    public UserResponse internalGetByReferral(@PathVariable String referralId) {
        return userService.internalGetByReferral(referralId);
    }

    @PostMapping("/internal/users/{id}/debit")
    public UserResponse internalDebit(@PathVariable Long id, @RequestParam double amount) {
        return userService.internalDebit(id, amount);
    }

    @PostMapping("/internal/users/{id}/credit")
    public UserResponse internalCredit(@PathVariable Long id, @RequestParam double amount) {
        return userService.internalCredit(id, amount);
    }
}