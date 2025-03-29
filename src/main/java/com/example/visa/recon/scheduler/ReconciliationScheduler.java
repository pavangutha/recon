package com.example.visa.recon.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.visa.recon.service.TwoWayBatchReconciliationService;

@Component
public class ReconciliationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationScheduler.class);

    @Autowired
    private TwoWayBatchReconciliationService reconciliationService;

    @Value("${reconciliation.file.path}")
    private String filePath;

    @Value("${reconciliation.report.path}")
    private String reportPath;

    @Value("${reconciliation.batch.size:1000}")
    private int batchSize;

    @Value("${reconciliation.schedule.enabled:true}")
    private boolean scheduleEnabled;

    @Scheduled(cron = "${reconciliation.schedule.cron:0 0 1 * * ?}") // Default: Run at 1 AM daily
    public void scheduleReconciliation() {
        if (!scheduleEnabled) {
            logger.info("Reconciliation scheduling is disabled");
            return;
        }

        try {
            logger.info("Starting scheduled reconciliation process");
            reconciliationService.performTwoWayReconciliation(filePath, reportPath, batchSize);
            logger.info("Scheduled reconciliation process completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled reconciliation: {}", e.getMessage(), e);
        }
    }
} 