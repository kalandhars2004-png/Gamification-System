package com.example.payment.repository;

import com.example.payment.entity.ServiceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRuleRepository extends JpaRepository<ServiceRule, Long> {

    Optional<ServiceRule> findByServiceTypeIgnoreCase(String serviceType);
}
