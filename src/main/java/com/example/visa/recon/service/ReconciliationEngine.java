package com.example.visa.recon.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;

/**
 * Core engine responsible for reconciling transactions between switch and network systems.
 * This class handles the comparison of transactions and detection of discrepancies.
 * It uses parallel processing for efficient handling of large transaction sets.
 */
@Service
public class ReconciliationEngine {
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationEngine.class);

    // List of transactions from switch system
    private List<VisaBase2Record> switchTransactions;

    // List of transactions from network (Visa/RuPay)
    private List<VisaBase2Record> networkTransactions;

    /**
     * Initializes the ReconciliationEngine with switch and network transactions.
     * 
     * @param switchTransactions List of transactions from the switch system
     * @param networkTransactions List of transactions from the network (Visa/RuPay)
     */
    public ReconciliationEngine(List<VisaBase2Record> switchTransactions, List<VisaBase2Record> networkTransactions) {
        this.switchTransactions = switchTransactions;
        this.networkTransactions = networkTransactions;
        logger.info("Initialized ReconciliationEngine with {} switch transactions and {} network transactions", 
            switchTransactions.size(), networkTransactions.size());
    }

    /**
     * Retrieves the list of switch transactions.
     * 
     * @return List of switch transactions
     */
    public List<VisaBase2Record> getSwitchTransactions() {
        return switchTransactions;
    }

    /**
     * Retrieves the list of network transactions.
     * 
     * @return List of network transactions
     */
    public List<VisaBase2Record> getNetworkTransactions() {
        return networkTransactions;
    }

    /**
     * Performs the reconciliation process by comparing switch transactions with network transactions.
     * Uses parallel processing for efficient comparison of large transaction sets.
     * 
     * @return List of discrepancies found during reconciliation
     */
    public List<Discrepancy> reconcile() {
        logger.info("Starting reconciliation process");
        long startTime = System.currentTimeMillis();
        
        // Step 1: Compare transactions in parallel
        List<Discrepancy> discrepancies = switchTransactions.parallelStream()
                .map(switchTx -> compareTransactionWithNetwork(switchTx))
                .filter(discrepancy -> discrepancy != null) // Only non-null discrepancies
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        logger.info("Reconciliation completed in {} ms. Found {} discrepancies", 
            (endTime - startTime), discrepancies.size());
        
        return discrepancies;
    }

    /**
     * Compares a single switch transaction with network transactions to find matches and discrepancies.
     * 
     * @param switchTx The switch transaction to compare
     * @return Discrepancy object if a discrepancy is found, null otherwise
     */
    private Discrepancy compareTransactionWithNetwork(VisaBase2Record switchTx) {
        logger.debug("Comparing switch transaction {} with network transactions", switchTx.getTransactionId());
        return networkTransactions.parallelStream()
                .filter(networkTx -> networkTx.getTransactionId().equals(switchTx.getTransactionId()))
                .findFirst()
                .map(networkTx -> detectDiscrepancy(switchTx, networkTx))
                .orElseGet(() -> {
                    logger.debug("Transaction {} not found in network", switchTx.getTransactionId());
                    return new Discrepancy(switchTx.getTransactionId(), "Missing in Network");
                });
    }

    /**
     * Detects discrepancies between a switch transaction and its matching network transaction.
     * Currently checks for amount mismatches, but can be extended to check other fields.
     * 
     * @param switchTx The switch transaction
     * @param networkTx The matching network transaction
     * @return Discrepancy object if a discrepancy is found, null otherwise
     */
    private Discrepancy detectDiscrepancy(VisaBase2Record switchTx, VisaBase2Record networkTx) {
        if (!switchTx.getAmount().equals(networkTx.getAmount())) {
            logger.warn("Amount mismatch detected for transaction {}: Switch={}, Network={}", 
                switchTx.getTransactionId(), switchTx.getAmount(), networkTx.getAmount());
            return new Discrepancy(
                switchTx.getTransactionId(), 
                "Amount Mismatch", 
                new BigDecimal(switchTx.getAmount()),
                new BigDecimal(networkTx.getAmount())
            );
        }
        logger.debug("No discrepancy found for transaction {}", switchTx.getTransactionId());
        return null;
    }
}
