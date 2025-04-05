package com.example.visa.recon.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.Discrepancy;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class DiscrepancyReportService {
    private static final Logger logger = LoggerFactory.getLogger(DiscrepancyReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class DiscrepancyReport {
        private final LocalDateTime reportGeneratedAt;
        private final LocalDateTime processingStartTime;
        private LocalDateTime processingEndTime;
        private final List<Discrepancy> missingTransactions;
        private final List<Discrepancy> amountMismatches;
        private final List<Discrepancy> duplicateTransactions;
        private final List<Discrepancy> dateMismatches;
        private final List<Discrepancy> otherDiscrepancies;
        private int totalSwitchRecords;
        private int totalNetworkRecords;
        private long processingTimeMillis;

        public DiscrepancyReport() {
            this.reportGeneratedAt = LocalDateTime.now();
            this.processingStartTime = LocalDateTime.now();
            this.processingEndTime = LocalDateTime.now();
            this.missingTransactions = new ArrayList<>();
            this.amountMismatches = new ArrayList<>();
            this.duplicateTransactions = new ArrayList<>();
            this.dateMismatches = new ArrayList<>();
            this.otherDiscrepancies = new ArrayList<>();
        }

        public void setProcessingEndTime() {
            this.processingEndTime = LocalDateTime.now();
            this.processingTimeMillis = java.time.Duration.between(processingStartTime, processingEndTime).toMillis();
        }

        public String getProcessingTimeFormatted() {
            long seconds = processingTimeMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        }

        public void setTotalSwitchRecords(int totalSwitchRecords) {
            this.totalSwitchRecords = totalSwitchRecords;
        }

        public void setTotalNetworkRecords(int totalNetworkRecords) {
            this.totalNetworkRecords = totalNetworkRecords;
        }

        public int getTotalSwitchRecords() {
            return totalSwitchRecords;
        }

        public int getTotalNetworkRecords() {
            return totalNetworkRecords;
        }

        public int getTotalMatchedRecords() {
            return totalSwitchRecords - missingTransactions.size();
        }

        public double getMatchRate() {
            if (totalSwitchRecords == 0) return 0.0;
            return (getTotalMatchedRecords() * 100.0) / totalSwitchRecords;
        }

        public int getTotalDiscrepancies() {
            return missingTransactions.size() + 
                   amountMismatches.size() + 
                   duplicateTransactions.size() + 
                   dateMismatches.size() + 
                   otherDiscrepancies.size();
        }

        public void addDiscrepancy(Discrepancy discrepancy) {
            switch (discrepancy.getDiscrepancyType()) {
                case "Missing in Network":
                    missingTransactions.add(discrepancy);
                    break;
                case "Amount Mismatch":
                    amountMismatches.add(discrepancy);
                    break;
                case "Duplicate Transaction":
                    duplicateTransactions.add(discrepancy);
                    break;
                case "Date Mismatch":
                    dateMismatches.add(discrepancy);
                    break;
                default:
                    otherDiscrepancies.add(discrepancy);
            }
        }

        public String generateReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Transaction Reconciliation Report ===\n");
            report.append("Generated at: ").append(reportGeneratedAt.format(DATE_FORMATTER)).append("\n\n");

            // Summary section
            report.append("Summary:\n");
            report.append("--------\n");
            report.append(String.format("Total Missing Transactions: %d\n", missingTransactions.size()));
            report.append(String.format("Total Amount Mismatches: %d\n", amountMismatches.size()));
            report.append(String.format("Total Duplicate Transactions: %d\n", duplicateTransactions.size()));
            report.append(String.format("Total Date Mismatches: %d\n", dateMismatches.size()));
            report.append(String.format("Other Discrepancies: %d\n", otherDiscrepancies.size()));
            report.append("\n");

            // Detailed sections
            if (!missingTransactions.isEmpty()) {
                report.append("Missing Transactions:\n");
                report.append("-------------------\n");
                missingTransactions.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!amountMismatches.isEmpty()) {
                report.append("Amount Mismatches:\n");
                report.append("-----------------\n");
                amountMismatches.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!duplicateTransactions.isEmpty()) {
                report.append("Duplicate Transactions:\n");
                report.append("----------------------\n");
                duplicateTransactions.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!dateMismatches.isEmpty()) {
                report.append("Date Mismatches:\n");
                report.append("---------------\n");
                dateMismatches.forEach(d -> report.append(formatDiscrepancy(d)));
                report.append("\n");
            }

            if (!otherDiscrepancies.isEmpty()) {
                report.append("Other Discrepancies:\n");
                report.append("-------------------\n");
                otherDiscrepancies.forEach(d -> report.append(formatDiscrepancy(d)));
            }

            return report.toString();
        }

        private String formatDiscrepancy(Discrepancy d) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Transaction ID: %s\n", d.getTransactionId()));
            sb.append(String.format("Type: %s\n", d.getDiscrepancyType()));
            if (d.getAmountSwitch() != null && d.getAmountNetwork() != null) {
                sb.append(String.format("Switch Amount: %s\n", d.getAmountSwitch()));
                sb.append(String.format("Network Amount: %s\n", d.getAmountNetwork()));
                sb.append(String.format("Difference: %s\n", 
                    d.getAmountSwitch().subtract(d.getAmountNetwork())));
            }
            sb.append("------------------------\n");
            return sb.toString();
        }

        public LocalDateTime getProcessingStartTime() {
            return processingStartTime;
        }

        public LocalDateTime getProcessingEndTime() {
            return processingEndTime;
        }

        public long getProcessingTimeMillis() {
            return processingTimeMillis;
        }
    }

    public DiscrepancyReport generateDiscrepancyReport(List<VisaBase2Record> switchTransactions, 
                                                      List<VisaBase2Record> networkTransactions) {
        DiscrepancyReport report = new DiscrepancyReport();
        report.setTotalSwitchRecords(switchTransactions.size());
        report.setTotalNetworkRecords(networkTransactions.size());
        
        // Create maps for faster lookup
        Map<String, VisaBase2Record> networkMap = networkTransactions.stream()
            .collect(Collectors.toMap(VisaBase2Record::getTransactionId, t -> t));

        // Check for missing transactions
        switchTransactions.stream()
            .filter(tx -> !networkMap.containsKey(tx.getTransactionId()))
            .forEach(tx -> report.addDiscrepancy(new Discrepancy(tx.getTransactionId(), "Missing in Network")));

        // Check for amount mismatches and other discrepancies
        switchTransactions.stream()
            .filter(tx -> networkMap.containsKey(tx.getTransactionId()))
            .forEach(tx -> {
                VisaBase2Record networkTx = networkMap.get(tx.getTransactionId());
                if (!tx.getAmount().equals(networkTx.getAmount())) {
                    report.addDiscrepancy(new Discrepancy(
                        tx.getTransactionId(),
                        "Amount Mismatch",
                        new BigDecimal(tx.getAmount()),
                        new BigDecimal(networkTx.getAmount())
                    ));
                }
                if (!tx.getTransactionDate().equals(networkTx.getTransactionDate())) {
                    report.addDiscrepancy(new Discrepancy(tx.getTransactionId(), "Date Mismatch"));
                }
            });

        // Check for duplicates in network transactions
        Map<String, Long> transactionIdCounts = networkTransactions.stream()
            .collect(Collectors.groupingBy(VisaBase2Record::getTransactionId, Collectors.counting()));
        
        transactionIdCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .forEach(entry -> report.addDiscrepancy(new Discrepancy(entry.getKey(), "Duplicate Transaction")));

        report.setProcessingEndTime();
        return report;
    }

    public Map<String, Object> generateSummaryStatistics(DiscrepancyReport report) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDiscrepancies", 
            report.missingTransactions.size() + 
            report.amountMismatches.size() + 
            report.duplicateTransactions.size() + 
            report.dateMismatches.size() + 
            report.otherDiscrepancies.size());
        
        stats.put("missingTransactions", report.missingTransactions.size());
        stats.put("amountMismatches", report.amountMismatches.size());
        stats.put("duplicateTransactions", report.duplicateTransactions.size());
        stats.put("dateMismatches", report.dateMismatches.size());
        stats.put("otherDiscrepancies", report.otherDiscrepancies.size());
        
        // Calculate total amount discrepancy
        BigDecimal totalAmountDiscrepancy = report.amountMismatches.stream()
            .map(d -> d.getAmountSwitch().subtract(d.getAmountNetwork()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("totalAmountDiscrepancy", totalAmountDiscrepancy);
        
        return stats;
    }

    public void generateExcelReport(String reportPath, DiscrepancyReport report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, report);

            // Create Missing Transactions Sheet
            Sheet missingSheet = workbook.createSheet("Missing Transactions");
            createDiscrepancySheet(missingSheet, report.missingTransactions);

            // Create Amount Mismatches Sheet
            Sheet amountSheet = workbook.createSheet("Amount Mismatches");
            createDiscrepancySheet(amountSheet, report.amountMismatches);

            // Create Duplicate Transactions Sheet
            Sheet duplicateSheet = workbook.createSheet("Duplicate Transactions");
            createDiscrepancySheet(duplicateSheet, report.duplicateTransactions);

            // Create Date Mismatches Sheet
            Sheet dateSheet = workbook.createSheet("Date Mismatches");
            createDiscrepancySheet(dateSheet, report.dateMismatches);

            // Create Other Discrepancies Sheet
            Sheet otherSheet = workbook.createSheet("Other Discrepancies");
            createDiscrepancySheet(otherSheet, report.otherDiscrepancies);

            // Apply formatting
            applyFormatting(workbook);

            // Save workbook
            try (FileOutputStream fileOut = new FileOutputStream(reportPath)) {
                workbook.write(fileOut);
            }

            logger.info("Excel report generated successfully at: {}", reportPath);
        } catch (IOException e) {
            logger.error("Error generating Excel report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void createSummarySheet(Sheet sheet, DiscrepancyReport report) {
        // Create header style
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create summary data
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Reconciliation Summary");
        headerCell.setCellStyle(headerStyle);

        // Add summary data
        int rowNum = 2;
        
        // Processing Information Section
        addSummaryRow(sheet, rowNum++, "=== Processing Information ===", "");
        addSummaryRow(sheet, rowNum++, "Report Generated At", report.reportGeneratedAt.format(DATE_FORMATTER));
        addSummaryRow(sheet, rowNum++, "Processing Start Time", report.getProcessingStartTime().format(DATE_FORMATTER));
        addSummaryRow(sheet, rowNum++, "Processing End Time", report.getProcessingEndTime().format(DATE_FORMATTER));
        addSummaryRow(sheet, rowNum++, "Total Processing Time", report.getProcessingTimeFormatted());
        addSummaryRow(sheet, rowNum++, "Processing Speed", 
            String.format("%.2f records/second", 
                (report.getTotalSwitchRecords() + report.getTotalNetworkRecords()) / 
                (report.getProcessingTimeMillis() / 1000.0)));
        addSummaryRow(sheet, rowNum++, "", ""); // Empty row for spacing

        // Transaction Statistics Section
        addSummaryRow(sheet, rowNum++, "=== Transaction Statistics ===", "");
        addSummaryRow(sheet, rowNum++, "Total Switch Records Processed", report.getTotalSwitchRecords());
        addSummaryRow(sheet, rowNum++, "Total Network Records Processed", report.getTotalNetworkRecords());
        addSummaryRow(sheet, rowNum++, "Total Records Processed", 
            report.getTotalSwitchRecords() + report.getTotalNetworkRecords());
        addSummaryRow(sheet, rowNum++, "Total Records Matched", report.getTotalMatchedRecords());
        addSummaryRow(sheet, rowNum++, "Match Rate", String.format("%.2f%%", report.getMatchRate()));
        addSummaryRow(sheet, rowNum++, "", ""); // Empty row for spacing

        // Discrepancy Summary Section
        addSummaryRow(sheet, rowNum++, "=== Discrepancy Summary ===", "");
        addSummaryRow(sheet, rowNum++, "Total Discrepancies", report.getTotalDiscrepancies());
        addSummaryRow(sheet, rowNum++, "Missing Transactions", report.missingTransactions.size());
        addSummaryRow(sheet, rowNum++, "Amount Mismatches", report.amountMismatches.size());
        addSummaryRow(sheet, rowNum++, "Duplicate Transactions", report.duplicateTransactions.size());
        addSummaryRow(sheet, rowNum++, "Date Mismatches", report.dateMismatches.size());
        addSummaryRow(sheet, rowNum++, "Other Discrepancies", report.otherDiscrepancies.size());
        addSummaryRow(sheet, rowNum++, "", ""); // Empty row for spacing

        // Amount Summary Section
        addSummaryRow(sheet, rowNum++, "=== Amount Summary ===", "");
        BigDecimal totalAmountDiscrepancy = report.amountMismatches.stream()
            .map(d -> d.getAmountSwitch().subtract(d.getAmountNetwork()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        addSummaryRow(sheet, rowNum++, "Total Amount Discrepancy", totalAmountDiscrepancy.toString());
        addSummaryRow(sheet, rowNum++, "Average Amount Discrepancy", 
            report.amountMismatches.isEmpty() ? "0" : 
            totalAmountDiscrepancy.divide(new BigDecimal(report.amountMismatches.size()), 2, RoundingMode.HALF_UP).toString());

        // Apply section header formatting
        for (int i = 0; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getStringCellValue().startsWith("===")) {
                    Font sectionFont = sheet.getWorkbook().createFont();
                    sectionFont.setBold(true);
                    sectionFont.setColor(IndexedColors.BLUE.getIndex());
                    CellStyle sectionStyle = sheet.getWorkbook().createCellStyle();
                    sectionStyle.setFont(sectionFont);
                    cell.setCellStyle(sectionStyle);
                }
            }
        }

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDiscrepancySheet(Sheet sheet, List<Discrepancy> discrepancies) {
        if (discrepancies.isEmpty()) {
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("No discrepancies found");
            return;
        }

        // Create header style
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Transaction ID", "Discrepancy Type", "Switch Amount", "Network Amount", "Difference", "Status"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        int rowNum = 1;
        for (Discrepancy d : discrepancies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(d.getTransactionId());
            row.createCell(1).setCellValue(d.getDiscrepancyType());
            
            if (d.getAmountSwitch() != null) {
                row.createCell(2).setCellValue(d.getAmountSwitch().doubleValue());
            }
            if (d.getAmountNetwork() != null) {
                row.createCell(3).setCellValue(d.getAmountNetwork().doubleValue());
            }
            if (d.getAmountSwitch() != null && d.getAmountNetwork() != null) {
                row.createCell(4).setCellValue(d.getAmountSwitch().subtract(d.getAmountNetwork()).doubleValue());
            }
            row.createCell(5).setCellValue("Open");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, int value) {
        addSummaryRow(sheet, rowNum, label, String.valueOf(value));
    }

    private void applyFormatting(Workbook workbook) {
        // Apply conditional formatting for amount differences
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().equals("Amount Mismatches")) {
                applyAmountFormatting(sheet);
            }
        }
    }

    private void applyAmountFormatting(Sheet sheet) {
        // Create red style for negative differences
        CellStyle redStyle = sheet.getWorkbook().createCellStyle();
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font redFont = sheet.getWorkbook().createFont();
        redFont.setColor(IndexedColors.WHITE.getIndex());
        redStyle.setFont(redFont);

        // Create green style for positive differences
        CellStyle greenStyle = sheet.getWorkbook().createCellStyle();
        greenStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font greenFont = sheet.getWorkbook().createFont();
        greenFont.setColor(IndexedColors.WHITE.getIndex());
        greenStyle.setFont(greenFont);

        // Apply formatting to difference column
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell diffCell = row.getCell(4);
                if (diffCell != null && diffCell.getCellType() == CellType.NUMERIC) {
                    double diff = diffCell.getNumericCellValue();
                    if (diff < 0) {
                        diffCell.setCellStyle(redStyle);
                    } else if (diff > 0) {
                        diffCell.setCellStyle(greenStyle);
                    }
                }
            }
        }
    }
} 