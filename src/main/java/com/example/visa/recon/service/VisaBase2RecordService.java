package com.example.visa.recon.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.visa.recon.mapper.VisaBase2RecordMapper;
import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisaBase2RecordService {

    private final VisaBase2RecordRepository repository;
    private final VisaBase2RecordMapper mapper;

    @Transactional
    public VisaBase2Record save(VisaBase2Record record) {
        VisaBase2RecordEntity entity = mapper.toEntity(record);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Optional<VisaBase2Record> findByTransactionId(String transactionId) {
        return Optional.ofNullable(repository.findByTransactionId(transactionId))
                .map(mapper::toDto);
    }

    @Transactional
    public List<VisaBase2Record> saveAll(List<VisaBase2Record> records) {
        List<VisaBase2RecordEntity> entities = records.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        entities = repository.saveAll(entities);
        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisaBase2Record> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteByTransactionId(String transactionId) {
        Optional.ofNullable(repository.findByTransactionId(transactionId))
                .ifPresent(repository::delete);
    }
} 