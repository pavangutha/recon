package com.example.visa.recon.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * This service is used to trigger the reconciliation job.
 * It is used to trigger the reconciliation job with the specified file path.
 */
@Service
public class ReconciliationJobService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job reconciliationJob;

    /**
     * Triggers the reconciliation job with the specified file path.
     * 
     * @param filePath Path to the input file for reconciliation
     * @return Job execution ID
     * @throws Exception if job execution fails
     */
    public String triggerReconciliation(String filePath) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("filePath", filePath)
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        return jobLauncher.run(reconciliationJob, jobParameters).getJobId().toString();
    }
} 