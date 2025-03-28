package com.example.visa.recon.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;

@Component
public class FileToDbReconciliationWriter implements ItemWriter<VisaBase2RecordEntity> {

    @Autowired
    private VisaBase2RecordRepository repository;

    @Override
    public void write(Chunk<? extends VisaBase2RecordEntity> chunk) throws Exception {
        repository.saveAll(chunk.getItems());
    }
} 