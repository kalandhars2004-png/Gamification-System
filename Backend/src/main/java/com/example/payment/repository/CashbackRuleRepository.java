package com.example.payment.repository;

import com.example.payment.entity.CashbackRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashbackRuleRepository extends JpaRepository<CashbackRule, Long> {

    List<CashbackRule> findAllByOrderByMinAmountAsc();
}
