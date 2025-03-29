package com.example.visa.recon.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    @Value("${reconciliation.thread-pool.core-size:4}")
    private int corePoolSize;
    
    @Value("${reconciliation.thread-pool.max-size:8}")
    private int maxPoolSize;
    
    @Value("${reconciliation.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Bean(name = "reconciliationExecutor")
    public Executor reconciliationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Reconciliation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
} 