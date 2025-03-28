package com.example.visa.recon.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelReportGenerator {
    
    public void generateReport(String reportPath, 
                             List<Discrepancy> switchDiscrepancies,
                             List<Discrepancy> networkDiscrepancies) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, switchDiscrepancies, networkDiscrepancies);
            
            // Create switch discrepancies sheet
            Sheet switchSheet = workbook.createSheet("Switch Discrepancies");
            createDiscrepancySheet(switchSheet, switchDiscrepancies);
            
            // Create network discrepancies sheet
            Sheet networkSheet = workbook.createSheet("Network Discrepancies");
            createDiscrepancySheet(networkSheet, networkDiscrepancies);
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(reportPath)) {
                workbook.write(fileOut);
            }
            
            log.info("Reconciliation report generated successfully at: {}", reportPath);
        } catch (IOException e) {
            log.error("Error generating reconciliation report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate reconciliation report", e);
        }
    }
    
    private void createSummarySheet(Sheet sheet, 
                                  List<Discrepancy> switchDiscrepancies,
                                  List<Discrepancy> networkDiscrepancies) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Reconciliation Summary");
        
        Row switchRow = sheet.createRow(1);
        switchRow.createCell(0).setCellValue("Total Switch Discrepancies");
        switchRow.createCell(1).setCellValue(switchDiscrepancies.size());
        
        Row networkRow = sheet.createRow(2);
        networkRow.createCell(0).setCellValue("Total Network Discrepancies");
        networkRow.createCell(1).setCellValue(networkDiscrepancies.size());
        
        // Add discrepancy type breakdown
        createDiscrepancyBreakdown(sheet, 4, switchDiscrepancies, "Switch");
        createDiscrepancyBreakdown(sheet, switchDiscrepancies.size() + 6, networkDiscrepancies, "Network");
    }
    
    private void createDiscrepancySheet(Sheet sheet, List<Discrepancy> discrepancies) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Transaction ID");
        headerRow.createCell(1).setCellValue("Discrepancy Type");
        headerRow.createCell(2).setCellValue("Switch Amount");
        headerRow.createCell(3).setCellValue("Network Amount");
        
        // Create data rows
        int rowNum = 1;
        for (Discrepancy discrepancy : discrepancies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(discrepancy.getTransactionId());
            row.createCell(1).setCellValue(discrepancy.getDiscrepancyType());
            
            if (discrepancy.getAmountSwitch() != null) {
                row.createCell(2).setCellValue(discrepancy.getAmountSwitch().doubleValue());
            }
            if (discrepancy.getAmountNetwork() != null) {
                row.createCell(3).setCellValue(discrepancy.getAmountNetwork().doubleValue());
            }
        }
        
        // Autosize columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDiscrepancyBreakdown(Sheet sheet, int startRow, List<Discrepancy> discrepancies, String source) {
        Row headerRow = sheet.createRow(startRow);
        headerRow.createCell(0).setCellValue(source + " Discrepancy Breakdown");
        
        Map<String, Long> typeCount = discrepancies.stream()
            .collect(Collectors.groupingBy(Discrepancy::getDiscrepancyType, Collectors.counting()));
        
        int rowNum = startRow + 1;
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }
}