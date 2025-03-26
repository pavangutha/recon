package com.example.visa.recon.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.visa.recon.model.entity.VisaBase2Txn;
import com.example.visa.recon.model.enums.TransactionStatus;

@Repository
public interface VisaBase2TransactionRepository extends JpaRepository<VisaBase2Txn, Long> {
    
    Optional<VisaBase2Txn> findByTransactionId(String transactionId);
    
    List<VisaBase2Txn> findByMerchantId(String merchantId);
    
    List<VisaBase2Txn> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<VisaBase2Txn> findByStatus(TransactionStatus status);
    
    List<VisaBase2Txn> findByBatchId(String batchId);
    
    @Query("SELECT t FROM VisaBase2Txn t " +
           "WHERE t.timestamp BETWEEN :startDate AND :endDate " +
           "AND t.merchantId = :merchantId " +
           "AND t.status = :status")
    List<VisaBase2Txn> findTransactionsByMerchantAndDateRange(
            @Param("merchantId") String merchantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") TransactionStatus status);
} 