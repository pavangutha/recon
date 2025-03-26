package com.example.visa.recon.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;

@Service
public class ReconciliationService {
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);
    private final ReconciliationEngine reconciliationEngine;
    private final DiscrepancyReportService reportService;

    public ReconciliationService(ReconciliationEngine reconciliationEngine, DiscrepancyReportService reportService) {
        this.reconciliationEngine = reconciliationEngine;
        this.reportService = reportService;
    }

    // Daily batch reconciliation task
    @Scheduled(cron = "0 0 23 * * ?") // Executes daily at 11 PM
    public void performBatchReconciliation() {
        List<VisaBase2Record> switchTransactions = reconciliationEngine.getSwitchTransactions();
        List<VisaBase2Record> networkTransactions = reconciliationEngine.getNetworkTransactions();
        
        DiscrepancyReportService.DiscrepancyReport report = reportService.generateDiscrepancyReport(
            switchTransactions, networkTransactions);
        
        // Generate and log the report
        String reportText = report.generateReport();
        logger.info("Reconciliation Report:\n{}", reportText);
        
        // Generate and log summary statistics
        Map<String, Object> stats = reportService.generateSummaryStatistics(report);
        logger.info("Reconciliation Statistics: {}", stats);
        
        // Save report to file or database if needed
        saveReport(reportText, stats);
    }

    private void saveReport(String reportText, Map<String, Object> stats) {
        // TODO: Implement report persistence (file, database, etc.)
        logger.info("Report saved successfully");
    }
}
