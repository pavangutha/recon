package com.example.visa.recon.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.visa.recon.model.Discrepancy;

@Service
@Slf4j
public class ExcelReportGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public void generateReport(String reportPath, 
                             List<Discrepancy> switchDiscrepancies,
                             List<Discrepancy> networkDiscrepancies,
                             int totalSwitchRecords,
                             int totalNetworkRecords,
                             LocalDateTime startTime,
                             LocalDateTime endTime) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, switchDiscrepancies, networkDiscrepancies, 
                             totalSwitchRecords, totalNetworkRecords, startTime, endTime);
            
            // Create switch discrepancies sheet
            Sheet switchSheet = workbook.createSheet("Switch Discrepancies");
            createDiscrepancySheet(switchSheet, switchDiscrepancies);
            
            // Create network discrepancies sheet
            Sheet networkSheet = workbook.createSheet("Network Discrepancies");
            createDiscrepancySheet(networkSheet, networkDiscrepancies);
            
            // Create transaction details sheet
            Sheet transactionSheet = workbook.createSheet("Transaction Details");
            createTransactionDetailsSheet(transactionSheet, switchDiscrepancies, networkDiscrepancies);
            
            // Apply formatting
            applyFormatting(workbook);
            
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
                                  List<Discrepancy> networkDiscrepancies,
                                  int totalSwitchRecords,
                                  int totalNetworkRecords,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime) {
        // Create header style
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create section header style
        CellStyle sectionStyle = sheet.getWorkbook().createCellStyle();
        Font sectionFont = sheet.getWorkbook().createFont();
        sectionFont.setBold(true);
        sectionFont.setColor(IndexedColors.BLUE.getIndex());
        sectionStyle.setFont(sectionFont);

        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reconciliation Summary Report");
        titleCell.setCellStyle(headerStyle);
        
        // Processing Information Section
        rowNum++;
        Row processingHeader = sheet.createRow(rowNum++);
        processingHeader.createCell(0).setCellValue("=== Processing Information ===");
        processingHeader.getCell(0).setCellStyle(sectionStyle);
        
        addSummaryRow(sheet, rowNum++, "Report Generated At", LocalDateTime.now().format(DATE_FORMATTER));
        addSummaryRow(sheet, rowNum++, "Processing Start Time", startTime.format(DATE_FORMATTER));
        addSummaryRow(sheet, rowNum++, "Processing End Time", endTime.format(DATE_FORMATTER));
        addSummaryRow(sheet, rowNum++, "Total Processing Time", 
            formatDuration(java.time.Duration.between(startTime, endTime)));
        rowNum++; // Empty row for spacing

        // Transaction Statistics Section
        Row statsHeader = sheet.createRow(rowNum++);
        statsHeader.createCell(0).setCellValue("=== Transaction Statistics ===");
        statsHeader.getCell(0).setCellStyle(sectionStyle);
        
        addSummaryRow(sheet, rowNum++, "Total Switch Records", totalSwitchRecords);
        addSummaryRow(sheet, rowNum++, "Total Network Records", totalNetworkRecords);
        addSummaryRow(sheet, rowNum++, "Total Records Processed", totalSwitchRecords + totalNetworkRecords);
        addSummaryRow(sheet, rowNum++, "Processing Speed", 
            String.format("%.2f records/second", 
                (totalSwitchRecords + totalNetworkRecords) / 
                (java.time.Duration.between(startTime, endTime).toMillis() / 1000.0)));
        rowNum++; // Empty row for spacing

        // Discrepancy Summary Section
        Row discrepancyHeader = sheet.createRow(rowNum++);
        discrepancyHeader.createCell(0).setCellValue("=== Discrepancy Summary ===");
        discrepancyHeader.getCell(0).setCellStyle(sectionStyle);
        
        addSummaryRow(sheet, rowNum++, "Total Discrepancies", 
            switchDiscrepancies.size() + networkDiscrepancies.size());
        addSummaryRow(sheet, rowNum++, "Switch Discrepancies", switchDiscrepancies.size());
        addSummaryRow(sheet, rowNum++, "Network Discrepancies", networkDiscrepancies.size());
        addSummaryRow(sheet, rowNum++, "Match Rate", 
            String.format("%.2f%%", 
                ((totalSwitchRecords + totalNetworkRecords - 
                  (switchDiscrepancies.size() + networkDiscrepancies.size())) * 100.0) / 
                (totalSwitchRecords + totalNetworkRecords)));
        rowNum++; // Empty row for spacing

        // Discrepancy Type Breakdown
        Row breakdownHeader = sheet.createRow(rowNum++);
        breakdownHeader.createCell(0).setCellValue("=== Discrepancy Type Breakdown ===");
        breakdownHeader.getCell(0).setCellStyle(sectionStyle);
        
        Map<String, Long> switchTypeCount = switchDiscrepancies.stream()
            .collect(Collectors.groupingBy(Discrepancy::getDiscrepancyType, Collectors.counting()));
        Map<String, Long> networkTypeCount = networkDiscrepancies.stream()
            .collect(Collectors.groupingBy(Discrepancy::getDiscrepancyType, Collectors.counting()));
        
        for (String type : switchTypeCount.keySet()) {
            addSummaryRow(sheet, rowNum++, "Switch - " + type, switchTypeCount.get(type).toString());
        }
        for (String type : networkTypeCount.keySet()) {
            addSummaryRow(sheet, rowNum++, "Network - " + type, networkTypeCount.get(type).toString());
        }

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTransactionDetailsSheet(Sheet sheet, 
                                             List<Discrepancy> switchDiscrepancies,
                                             List<Discrepancy> networkDiscrepancies) {
        // Create header style
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Transaction ID", "Source", "Discrepancy Type", 
                          "Switch Amount", "Network Amount", "Difference", 
                          "Status", "Created At"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        int rowNum = 1;
        
        // Add switch discrepancies
        for (Discrepancy d : switchDiscrepancies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(d.getTransactionId());
            row.createCell(1).setCellValue("Switch");
            row.createCell(2).setCellValue(d.getDiscrepancyType());
            
            if (d.getAmountSwitch() != null) {
                row.createCell(3).setCellValue(d.getAmountSwitch().doubleValue());
            }
            if (d.getAmountNetwork() != null) {
                row.createCell(4).setCellValue(d.getAmountNetwork().doubleValue());
            }
            if (d.getAmountSwitch() != null && d.getAmountNetwork() != null) {
                row.createCell(5).setCellValue(d.getAmountSwitch().subtract(d.getAmountNetwork()).doubleValue());
            }
            row.createCell(6).setCellValue("Open");
            row.createCell(7).setCellValue(d.getCreatedAt().format(DATE_FORMATTER));
        }

        // Add network discrepancies
        for (Discrepancy d : networkDiscrepancies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(d.getTransactionId());
            row.createCell(1).setCellValue("Network");
            row.createCell(2).setCellValue(d.getDiscrepancyType());
            
            if (d.getAmountSwitch() != null) {
                row.createCell(3).setCellValue(d.getAmountSwitch().doubleValue());
            }
            if (d.getAmountNetwork() != null) {
                row.createCell(4).setCellValue(d.getAmountNetwork().doubleValue());
            }
            if (d.getAmountSwitch() != null && d.getAmountNetwork() != null) {
                row.createCell(5).setCellValue(d.getAmountSwitch().subtract(d.getAmountNetwork()).doubleValue());
            }
            row.createCell(6).setCellValue("Open");
            row.createCell(7).setCellValue(d.getCreatedAt().format(DATE_FORMATTER));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
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
        String[] headers = {"Transaction ID", "Discrepancy Type", "Switch Amount", 
                          "Network Amount", "Difference", "Status", "Created At"};
        
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
            row.createCell(6).setCellValue(d.getCreatedAt().format(DATE_FORMATTER));
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

    private void addSummaryRow(Sheet sheet, int rowNum, String label, long value) {
        addSummaryRow(sheet, rowNum, label, String.valueOf(value));
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, double value) {
        addSummaryRow(sheet, rowNum, label, String.valueOf(value));
    }

    private String formatDuration(java.time.Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void applyFormatting(Workbook workbook) {
        // Apply conditional formatting for amount differences
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().equals("Transaction Details") || 
                sheet.getSheetName().equals("Switch Discrepancies") || 
                sheet.getSheetName().equals("Network Discrepancies")) {
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
                Cell diffCell = row.getCell(5);
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