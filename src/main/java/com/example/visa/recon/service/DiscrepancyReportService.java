package com.example.visa.recon.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;

@Service
public class DiscrepancyReportService {
    private static final Logger logger = LoggerFactory.getLogger(DiscrepancyReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class DiscrepancyReport {
        private final LocalDateTime reportGeneratedAt;
        private final List<Discrepancy> missingTransactions;
        private final List<Discrepancy> amountMismatches;
        private final List<Discrepancy> duplicateTransactions;
        private final List<Discrepancy> dateMismatches;
        private final List<Discrepancy> otherDiscrepancies;

        public DiscrepancyReport() {
            this.reportGeneratedAt = LocalDateTime.now();
            this.missingTransactions = new ArrayList<>();
            this.amountMismatches = new ArrayList<>();
            this.duplicateTransactions = new ArrayList<>();
            this.dateMismatches = new ArrayList<>();
            this.otherDiscrepancies = new ArrayList<>();
        }

        public void addDiscrepancy(Discrepancy discrepancy) {
            switch (discrepancy.getDiscrepancyType()) {
                case "Missing in Network":
                    missingTransactions.add(discrepancy);
                    break;
                case "Amount Mismatch":
                    amountMismatches.add(discrepancy);
                    break;
                case "Duplicate Transaction":
                    duplicateTransactions.add(discrepancy);
                    break;
                case "Date Mismatch":
                    dateMismatches.add(discrepancy);
                    break;
                default:
                    otherDiscrepancies.add(discrepancy);
            }
        }

        public String generateReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Transaction Reconciliation Report ===\n");
            report.append("Generated at: ").append(reportGeneratedAt.format(DATE_FORMATTER)).append("\n\n");

            // Summary section
            report.append("Summary:\n");
            report.append("--------\n");
            report.append(String.format("Total Missing Transactions: %d\n", missingTransactions.size()));
            report.append(String.format("Total Amount Mismatches: %d\n", amountMismatches.size()));
            report.append(String.format("Total Duplicate Transactions: %d\n", duplicateTransactions.size()));
            report.append(String.format("Total Date Mismatches: %d\n", dateMismatches.size()));
            report.append(String.format("Other Discrepancies: %d\n", otherDiscrepancies.size()));
            report.append("\n");

            // Detailed sections
            if (!missingTransactions.isEmpty()) {
                report.append("Missing Transactions:\n");
                report.append("-------------------\n");
                missingTransactions.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!amountMismatches.isEmpty()) {
                report.append("Amount Mismatches:\n");
                report.append("-----------------\n");
                amountMismatches.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!duplicateTransactions.isEmpty()) {
                report.append("Duplicate Transactions:\n");
                report.append("----------------------\n");
                duplicateTransactions.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!dateMismatches.isEmpty()) {
                report.append("Date Mismatches:\n");
                report.append("---------------\n");
                dateMismatches.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!otherDiscrepancies.isEmpty()) {
                report.append("Other Discrepancies:\n");
                report.append("-------------------\n");
                otherDiscrepancies.forEach(d -> report.append(formatDiscrepancy(d)));
            }

            return report.toString();
        }

        private String formatDiscrepancy(Discrepancy d) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Transaction ID: %s\n", d.getTransactionId()));
            sb.append(String.format("Type: %s\n", d.getDiscrepancyType()));
            if (d.getAmountSwitch() != null && d.getAmountNetwork() != null) {
                sb.append(String.format("Switch Amount: %s\n", d.getAmountSwitch()));
                sb.append(String.format("Network Amount: %s\n", d.getAmountNetwork()));
                sb.append(String.format("Difference: %s\n", 
                    d.getAmountSwitch().subtract(d.getAmountNetwork())));
            }
            sb.append("------------------------\n");
            return sb.toString();
        }
    }

    public DiscrepancyReport generateDiscrepancyReport(List<VisaBase2Record> switchTransactions, 
                                                      List<VisaBase2Record> networkTransactions) {
        DiscrepancyReport report = new DiscrepancyReport();
        
        // Create maps for faster lookup
        Map<String, VisaBase2Record> networkMap = networkTransactions.stream()
            .collect(Collectors.toMap(VisaBase2Record::getTransactionId, t -> t));

        // Check for missing transactions
        switchTransactions.stream()
            .filter(tx -> !networkMap.containsKey(tx.getTransactionId()))
            .forEach(tx -> report.addDiscrepancy(new Discrepancy(tx.getTransactionId(), "Missing in Network")));

        // Check for amount mismatches and other discrepancies
        switchTransactions.stream()
            .filter(tx -> networkMap.containsKey(tx.getTransactionId()))
            .forEach(tx -> {
                VisaBase2Record networkTx = networkMap.get(tx.getTransactionId());
                if (!tx.getAmount().equals(networkTx.getAmount())) {
                    report.addDiscrepancy(new Discrepancy(
                        tx.getTransactionId(),
                        "Amount Mismatch",
                        new BigDecimal(tx.getAmount()),
                        new BigDecimal(networkTx.getAmount())
                    ));
                }
                if (!tx.getTransactionDate().equals(networkTx.getTransactionDate())) {
                    report.addDiscrepancy(new Discrepancy(tx.getTransactionId(), "Date Mismatch"));
                }
            });

        // Check for duplicates in network transactions
        Map<String, Long> transactionIdCounts = networkTransactions.stream()
            .collect(Collectors.groupingBy(VisaBase2Record::getTransactionId, Collectors.counting()));
        
        transactionIdCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .forEach(entry -> report.addDiscrepancy(new Discrepancy(entry.getKey(), "Duplicate Transaction")));

        return report;
    }

    public Map<String, Object> generateSummaryStatistics(DiscrepancyReport report) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDiscrepancies", 
            report.missingTransactions.size() + 
            report.amountMismatches.size() + 
            report.duplicateTransactions.size() + 
            report.dateMismatches.size() + 
            report.otherDiscrepancies.size());
        
        stats.put("missingTransactions", report.missingTransactions.size());
        stats.put("amountMismatches", report.amountMismatches.size());
        stats.put("duplicateTransactions", report.duplicateTransactions.size());
        stats.put("dateMismatches", report.dateMismatches.size());
        stats.put("otherDiscrepancies", report.otherDiscrepancies.size());
        
        // Calculate total amount discrepancy
        BigDecimal totalAmountDiscrepancy = report.amountMismatches.stream()
            .map(d -> d.getAmountSwitch().subtract(d.getAmountNetwork()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("totalAmountDiscrepancy", totalAmountDiscrepancy);
        
        return stats;
    }
} 