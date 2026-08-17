package com.example.transaction.controller;

import com.example.transaction.dto.PaymentRequest;
import com.example.transaction.dto.PaymentResponse;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.TransactionService;
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
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions/pay")
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        return transactionService.pay(request);
    }

    @GetMapping("/transactions")
    public List<Transaction> allTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/transactions/{id}")
    public Transaction transactionById(@PathVariable Long id) {
        return transactionService.getAllTransactions().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @GetMapping("/internal/transactions/count")
    public Map<String, Long> countByUser(@RequestParam Long userId) {
        return Map.of("count", transactionService.countByUser(userId));
    }

    @GetMapping("/internal/transactions")
    public List<Transaction> transactionsByUser(@RequestParam Long userId) {
        return transactionService.transactionsByUser(userId);
    }
}