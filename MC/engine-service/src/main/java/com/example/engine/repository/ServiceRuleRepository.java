package com.example.engine.repository;

import com.example.engine.entity.ServiceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRuleRepository extends JpaRepository<ServiceRule, Long> {

    Optional<ServiceRule> findByServiceTypeIgnoreCase(String serviceType);
}