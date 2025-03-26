package com.example.visa.recon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.visa.recon.model.entity.VisaBase2RecordEntity;

@Repository
public interface VisaBase2RecordRepository extends JpaRepository<VisaBase2RecordEntity, Long> {
    VisaBase2RecordEntity findByTransactionId(String transactionId);
} 