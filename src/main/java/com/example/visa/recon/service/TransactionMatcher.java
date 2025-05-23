package com.example.visa.recon.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for matching transactions between different systems.
 * Provides both exact and fuzzy matching capabilities with configurable tolerance levels.
 * Uses a scoring system to determine the best matches when exact matches are not found.
 */
@Slf4j
@Service
public class TransactionMatcher {

    /**
     * Represents a matched pair of transactions with their match score.
     * Used to store the results of transaction matching operations.
     */
    @Data
    @AllArgsConstructor
    public static class MatchedPair {
        private VisaBase2Record source;
        private VisaBase2Record target;
        private double matchScore;
    }

    /**
     * Creates a unique hash key for a transaction based on its attributes.
     * Used for exact matching of transactions.
     * 
     * @param transaction The transaction to create a key for
     * @return A unique string key combining transaction ID, timestamp, and amount
     */
    private String createMatchingKey(VisaBase2Record transaction) {
        return String.format("%s_%s_%d",
            transaction.getTransactionId(),
            LocalDateTime.parse(transaction.getTransactionDate()+transaction.getTransactionTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            Math.round(Double.parseDouble(transaction.getAmount()) * 100) // Convert to cents to avoid floating point issues
        );
    }

    /**
     * Performs exact matching of transactions based on their hash keys.
     * This method is faster than fuzzy matching but requires exact matches.
     * 
     * @param sourceTransactions List of transactions from the source system
     * @param targetTransactions List of transactions from the target system
     * @return List of matched transaction pairs
     */
    public List<MatchedPair> findExactMatches(List<VisaBase2Record> sourceTransactions, 
                                             List<VisaBase2Record> targetTransactions) {
        log.info("Starting exact matching process with {} source and {} target transactions", 
            sourceTransactions.size(), targetTransactions.size());
        long startTime = System.currentTimeMillis();

        Map<String, VisaBase2Record> targetMap = new HashMap<>();
        List<MatchedPair> matches = new ArrayList<>();

        // Create hash map of target transactions for O(1) lookup
        for (VisaBase2Record target : targetTransactions) {
            targetMap.put(createMatchingKey(target), target);
        }

        // Find matches
        for (VisaBase2Record source : sourceTransactions) {
            String key = createMatchingKey(source);
            VisaBase2Record match = targetMap.get(key);
            if (match != null) {
                matches.add(new MatchedPair(source, match, 1.0));
                targetMap.remove(key); // Remove matched transaction to prevent duplicate matches
                log.debug("Found exact match for transaction {}", source.getTransactionId());
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Exact matching completed in {} ms. Found {} matches", 
            (endTime - startTime), matches.size());
        return matches;
    }

    /**
     * Performs fuzzy matching of transactions with configurable tolerance levels.
     * Uses a scoring system to find the best matches when exact matches are not found.
     * 
     * @param sourceTransactions List of transactions from the source system
     * @param targetTransactions List of transactions from the target system
     * @param timeToleranceMinutes Maximum allowed time difference in minutes
     * @param amountTolerancePercent Maximum allowed amount difference as percentage
     * @return List of matched transaction pairs with their match scores
     */
    public List<MatchedPair> findFuzzyMatches(List<VisaBase2Record> sourceTransactions, 
                                             List<VisaBase2Record> targetTransactions,
                                             long timeToleranceMinutes,
                                             double amountTolerancePercent) {
        log.info("Starting fuzzy matching process with {} source and {} target transactions", 
            sourceTransactions.size(), targetTransactions.size());
        log.info("Using time tolerance: {} minutes, amount tolerance: {}%", 
            timeToleranceMinutes, amountTolerancePercent);
        long startTime = System.currentTimeMillis();

        List<MatchedPair> matches = new ArrayList<>();
        Map<String, List<VisaBase2Record>> targetDateMap = new HashMap<>();

        // Group target transactions by date for faster searching
        for (VisaBase2Record target : targetTransactions) {
            String dateKey = LocalDateTime.parse(target.getTransactionDate()+target.getTransactionTime(), 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(DateTimeFormatter.ISO_LOCAL_DATE);
            targetDateMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(target);
        }

        // Find matches
        for (VisaBase2Record source : sourceTransactions) {
            String dateKey = LocalDateTime.parse(source.getTransactionDate()+source.getTransactionTime(), 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<VisaBase2Record> potentialMatches = targetDateMap.getOrDefault(dateKey, new ArrayList<>());
            
            VisaBase2Record bestMatch = null;
            double bestMatchScore = 0.0;

            for (VisaBase2Record target : potentialMatches) {
                double matchScore = calculateMatchScore(source, target, 
                    timeToleranceMinutes, amountTolerancePercent);
                
                if (matchScore > bestMatchScore && matchScore >= 0.8) { // 80% threshold
                    bestMatch = target;
                    bestMatchScore = matchScore;
                }
            }

            if (bestMatch != null) {
                matches.add(new MatchedPair(source, bestMatch, bestMatchScore));
                potentialMatches.remove(bestMatch); // Remove matched transaction
                log.debug("Found fuzzy match for transaction {} with score {}", 
                    source.getTransactionId(), bestMatchScore);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Fuzzy matching completed in {} ms. Found {} matches", 
            (endTime - startTime), matches.size());
        return matches;
    }

    /**
     * Calculates a match score between two transactions based on multiple criteria.
     * The score is weighted as follows:
     * - Transaction ID match: 30%
     * - Time match: 35%
     * - Amount match: 35%
     * 
     * @param source The source transaction
     * @param target The target transaction
     * @param timeToleranceMinutes Maximum allowed time difference in minutes
     * @param amountTolerancePercent Maximum allowed amount difference as percentage
     * @return Match score between 0.0 and 1.0
     */
    private double calculateMatchScore(VisaBase2Record source, 
                                     VisaBase2Record target,
                                     long timeToleranceMinutes,
                                     double amountTolerancePercent) {
        double score = 0.0;
        
        // Compare transaction IDs if available (30% weight)
        if (Objects.equals(source.getTransactionId(), target.getTransactionId())) {
            score += 0.3;
            log.trace("Transaction ID match for {}", source.getTransactionId());
        }

        // Compare timestamps (35% weight)
        long timeDiffMinutes = Math.abs(ChronoUnit.MINUTES.between(
            LocalDateTime.parse(source.getTransactionDate()+source.getTransactionTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            LocalDateTime.parse(target.getTransactionDate()+target.getTransactionTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        if (timeDiffMinutes <= timeToleranceMinutes) {
            score += 0.35 * (1 - ((double) timeDiffMinutes / timeToleranceMinutes));
            log.trace("Time match for {} with difference {} minutes", 
                source.getTransactionId(), timeDiffMinutes);
        }

        // Compare amounts (35% weight)
        double amountDiffPercent = Math.abs(Double.parseDouble(source.getAmount()) - Double.parseDouble(target.getAmount())) / 
            Double.parseDouble(source.getAmount()) * 100;
        if (amountDiffPercent <= amountTolerancePercent) {
            score += 0.35 * (1 - (amountDiffPercent / amountTolerancePercent));
            log.trace("Amount match for {} with difference {}%", 
                source.getTransactionId(), amountDiffPercent);
        }

        return score;
    }

    /**
     * Generates a detailed report of matched transactions.
     * 
     * @param matches List of matched transaction pairs
     * @return Formatted report string
     */
    public String generateMatchReport(List<MatchedPair> matches) {
        log.info("Generating match report for {} matched pairs", matches.size());
        StringBuilder report = new StringBuilder();
        report.append("Transaction Matching Report\n");
        report.append("==========================\n");
        report.append(String.format("Total Matches Found: %d\n\n", matches.size()));

        for (MatchedPair match : matches) {
            report.append(String.format("Match Score: %.2f%%\n", match.getMatchScore() * 100));
            report.append("Source Transaction: ").append(match.getSource()).append("\n");
            report.append("Target Transaction: ").append(match.getTarget()).append("\n");
            report.append("---------------------------\n");
        }

        log.debug("Match report generated successfully");
        return report.toString();
    }
} 