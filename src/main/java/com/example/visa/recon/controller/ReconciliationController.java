package com.example.visa.recon.controller;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.visa.recon.service.CsvFileGenrationService;
import com.example.visa.recon.service.ReconciliationJobService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ReconciliationController {

    @Autowired
    private CsvFileGenrationService csvFileGenrationService;

    @Autowired
    private ReconciliationJobService reconciliationJobService;

    @PostMapping("/dataingestion")  
    public String datainsert(@RequestBody String  visaTransactions) throws IOException {
        System.out.println("Visa Transactions: "+visaTransactions);
        // string to json
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(visaTransactions);
        System.out.println("Json Node: "+jsonNode);
        String filepath = jsonNode.get("filepath").asText();
        int totalRecords = jsonNode.get("totalrecords").asInt();
        return csvFileGenrationService.generateCsvFile(filepath, totalRecords);
    }
@PostMapping("reconcile")
public String reconcileData(@RequestBody String request) {
    //TODO: process POST request
    try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(request);
        System.out.println("Json Node: "+jsonNode);
        String filepath = jsonNode.get("filepath").asText();
        String jobId = reconciliationJobService.triggerReconciliation(filepath);
        return jobId;
    } catch (Exception e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
    }
}

}