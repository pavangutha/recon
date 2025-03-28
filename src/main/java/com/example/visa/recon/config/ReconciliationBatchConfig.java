package com.example.visa.recon.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.visa.recon.batch.DbToFileReconciliationProcessor;
import com.example.visa.recon.batch.DbToFileReconciliationReader;
import com.example.visa.recon.batch.DbToFileReconciliationWriter;
import com.example.visa.recon.batch.FileToDbReconciliationProcessor;
import com.example.visa.recon.batch.FileToDbReconciliationReader;
import com.example.visa.recon.batch.FileToDbReconciliationWriter;
import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;

@Configuration
@EnableBatchProcessing
public class ReconciliationBatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public JobLauncher jobLauncher() {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    public Job reconciliationJob() {
        return new JobBuilder("reconciliationJob", jobRepository)
                .start(fileToDbReconciliationStep())
                .next(dbToFileReconciliationStep())
                .build();
    }

    @Bean
    public Step fileToDbReconciliationStep() {
        return new StepBuilder("fileToDbReconciliationStep", jobRepository)
                .<VisaBase2Record, VisaBase2RecordEntity>chunk(100, transactionManager)
                .reader(fileToDbReader())
                .processor(fileToDbProcessor())
                .writer(fileToDbWriter())
                .build();
    }

    @Bean
    public Step dbToFileReconciliationStep() {
        return new StepBuilder("dbToFileReconciliationStep", jobRepository)
                .<VisaBase2RecordEntity, VisaBase2Record>chunk(100, transactionManager)
                .reader(dbToFileReader())
                .processor(dbToFileProcessor())
                .writer(dbToFileWriter())
                .build();
    }

    @Bean
    public ItemReader<VisaBase2Record> fileToDbReader() {
        return new FileToDbReconciliationReader();
    }

    @Bean
    public ItemProcessor<VisaBase2Record, VisaBase2RecordEntity> fileToDbProcessor() {
        return new FileToDbReconciliationProcessor();
    }

    @Bean
    public ItemWriter<VisaBase2RecordEntity> fileToDbWriter() {
        return new FileToDbReconciliationWriter();
    }

    @Bean
    public ItemReader<VisaBase2RecordEntity> dbToFileReader() {
        return new DbToFileReconciliationReader();
    }

    @Bean
    public ItemProcessor<VisaBase2RecordEntity, VisaBase2Record> dbToFileProcessor() {
        return new DbToFileReconciliationProcessor();
    }

    @Bean
    public ItemWriter<VisaBase2Record> dbToFileWriter() {
        return new DbToFileReconciliationWriter();
    }
} 