package com.example.visa.recon.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.visa.recon.service.CsvFileGenrationService;
import com.example.visa.recon.service.ReconciliationJobService;
import com.example.visa.recon.service.DiscrepancyReportService;
import com.example.visa.recon.service.TwoWayBatchReconciliationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ReconciliationController {
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationController.class);

    @Autowired
    private CsvFileGenrationService csvFileGenrationService;

    @Autowired
    private ReconciliationJobService reconciliationJobService;

    @Autowired
    private DiscrepancyReportService discrepancyReportService;

    @Autowired
    private TwoWayBatchReconciliationService twoWayReconciliationService;

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

    @PostMapping("/reconcile/two-way")
    public ResponseEntity<String> twoWayReconciliation(@RequestBody String request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(request);
            String filePath = jsonNode.get("filepath").asText();
            String reportPath = jsonNode.get("reportPath").asText();
            int batchSize = jsonNode.get("batchSize").asInt(1000);

            twoWayReconciliationService.performTwoWayReconciliation(filePath, reportPath, batchSize);
            return ResponseEntity.ok("Two-way reconciliation completed. Report generated at: " + reportPath);
        } catch (Exception e) {
            logger.error("Error in two-way reconciliation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Reconciliation failed: " + e.getMessage());
        }
    }

    // @PostMapping("/generate-report")
    // public String generateReport(@RequestBody String request) { 
    //     try {
    //         ObjectMapper objectMapper = new ObjectMapper();
    //         JsonNode jsonNode = objectMapper.readTree(request);
    //         String jobId = jsonNode.get("jobId").asText();
    //         return discrepancyReportService.generateDiscrepancyReport(switchTransactions, networkTransactions);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return "Error: " + e.getMessage();
    //     }
    // }
}