package com.example.visa.recon.controller;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.visa.recon.service.CsvFileGenrationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ReconciliationController {

    @Autowired
    private CsvFileGenrationService csvFileGenrationService;

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
public String postMethodName(@RequestBody String entity) {
    //TODO: process POST request
    
    return entity;
}

}