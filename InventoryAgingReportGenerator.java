//import static spark.Spark.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

public class InventoryAgingReportGenerator {

    public static void main(String[] args) {
        port(8080);
        
        // Enable CORS
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        });
        
        options("/*", (req, res) -> {
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        post("/upload", (req, res) -> {
            try (InputStream is = req.raw().getPart("file").getInputStream()) {
                // Get options from form data
                boolean includeCharts = "true".equals(req.raw().getParameter("includeCharts"));
                boolean includeSummary = "true".equals(req.raw().getParameter("includeSummary"));
                boolean includeRecommendations = "true".equals(req.raw().getParameter("includeRecommendations"));
                
                List<Item> items = readItemsFromExcel(is);
                InventoryAnalysis analysis = analyzeInventory(items);
                ByteArrayOutputStream out = generateSmartReport(analysis, includeCharts, includeSummary, includeRecommendations);

                res.type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                res.header("Content-Disposition", "attachment; filename=smart-inventory-aging-report.xlsx");
                res.raw().getOutputStream().write(out.toByteArray());
                res.raw().getOutputStream().flush();
                return res.raw();
            }
        });
        
        System.out.println("🚀 Smart Inventory Analytics Server started at http://localhost:8080");
    }

    static class Item {
        String name, category, supplier, location;
        int quantity;
        double unitCost, totalValue;
        LocalDate dateReceived;
        long agingDays;
        String riskLevel;
        
        Item(String name, String category, int quantity, LocalDate dateReceived) {
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.dateReceived = dateReceived;
            this.agingDays = ChronoUnit.DAYS.between(dateReceived, LocalDate.now());
            this.unitCost = 10 + Math.random() * 100; // Mock data
            this.totalValue = quantity * unitCost;
            this.supplier = "Supplier-" + (int)(Math.random() * 10 + 1);
            this.location = "Warehouse-" + (char)('A' + (int)(Math.random() * 5));
            this.riskLevel = calculateRiskLevel();
        }
        
        private String calculateRiskLevel() {
            if (agingDays > 90) return "Critical";
            if (agingDays > 60) return "High";
            if (agingDays > 30) return "Medium";
            return "Low";
        }
    }
    
    static class InventoryAnalysis {
        List<Item> items;
        Map<String, List<Item>> agingGroups;
        Map<String, Double> categoryValues;
        Map<String, Integer> supplierCounts;
        double totalValue;
        int totalItems;
        List<String> recommendations;
        Map<String, Object> kpis;
        
        InventoryAnalysis(List<Item> items) {
            this.items = items;
            this.agingGroups = groupByAging(items);
            this.categoryValues = calculateCategoryValues(items);
            this.supplierCounts = calculateSupplierCounts(items);
            this.totalValue = items.stream().mapToDouble(i -> i.totalValue).sum();
            this.totalItems = items.size();
            this.recommendations = generateRecommendations(items);
            this.kpis = calculateKPIs(items);
        }
    }

    private static List<Item> readItemsFromExcel(InputStream is) throws Exception {
        List<Item> items = new ArrayList<>();
        Workbook wb = new XSSFWorkbook(is);
        Sheet sheet = wb.getSheetAt(0);
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                String name = getCellValueAsString(row.getCell(0));
                String category = getCellValueAsString(row.getCell(1));
                int quantity = (int) getCellValueAsNumber(row.getCell(2));
                LocalDate date = parseDate(getCellValueAsString(row.getCell(3)));
                
                if (name != null && !name.trim().isEmpty()) {
                    items.add(new Item(name, category, quantity, date));
                }
            } catch (Exception e) {
                System.err.println("Error processing row " + i + ": " + e.getMessage());
            }
        }
        wb.close();
        return items;
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int)cell.getNumericCellValue());
            default: return "";
        }
    }
    
    private static double getCellValueAsNumber(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC: return cell.getNumericCellValue();
            case STRING: 
                try { return Double.parseDouble(cell.getStringCellValue()); }
                catch (NumberFormatException e) { return 0; }
            default: return 0;
        }
    }
    
    private static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } catch (Exception e2) {
                return LocalDate.now().minusDays((long)(Math.random() * 365));
            }
        }
    }

    private static InventoryAnalysis analyzeInventory(List<Item> items) {
        return new InventoryAnalysis(items);
    }
    
    private static Map<String, List<Item>> groupByAging(List<Item> items) {
        Map<String, List<Item>> groups = new LinkedHashMap<>();
        groups.put("Fresh (0-30 days)", new ArrayList<>());
        groups.put("Moderate (31-60 days)", new ArrayList<>());
        groups.put("Aging (61-90 days)", new ArrayList<>());
        groups.put("Critical (>90 days)", new ArrayList<>());

        for (Item item : items) {
            if (item.agingDays <= 30) groups.get("Fresh (0-30 days)").add(item);
            else if (item.agingDays <= 60) groups.get("Moderate (31-60 days)").add(item);
            else if (item.agingDays <= 90) groups.get("Aging (61-90 days)").add(item);
            else groups.get("Critical (>90 days)").add(item);
        }
        return groups;
    }
    
    private static Map<String, Double> calculateCategoryValues(List<Item> items) {
        return items.stream().collect(
            Collectors.groupingBy(i -> i.category,
            Collectors.summingDouble(i -> i.totalValue))
        );
    }
    
    private static Map<String, Integer> calculateSupplierCounts(List<Item> items) {
        return items.stream().collect(
            Collectors.groupingBy(i -> i.supplier,
            Collectors.summingInt(i -> i.quantity))
        );
    }
    
    private static List<String> generateRecommendations(List<Item> items) {
        List<String> recommendations = new ArrayList<>();
        
        long criticalItems = items.stream().filter(i -> i.agingDays > 90).count();
        if (criticalItems > 0) {
            recommendations.add("🚨 URGENT: " + criticalItems + " items are over 90 days old. Consider liquidation or promotional pricing.");
        }
        
        Map<String, Long> categoryAging = items.stream()
            .filter(i -> i.agingDays > 60)
            .collect(Collectors.groupingBy(i -> i.category, Collectors.counting()));
        
        categoryAging.entrySet().stream()
            .filter(e -> e.getValue() > 5)
            .forEach(e -> recommendations.add("📉 Category '" + e.getKey() + "' has " + e.getValue() + " aging items. Review procurement strategy."));
        
        double totalValue = items.stream().mapToDouble(i -> i.totalValue).sum();
        double criticalValue = items.stream().filter(i -> i.agingDays > 90).mapToDouble(i -> i.totalValue).sum();
        
        if (criticalValue / totalValue > 0.15) {
            recommendations.add("💰 " + String.format("%.1f%%", (criticalValue/totalValue)*100) + " of inventory value is in critical aging. Implement aggressive clearance strategy.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Inventory aging is within acceptable parameters. Continue monitoring.");
        }
        
        return recommendations;
    }
    
    private static Map<String, Object> calculateKPIs(List<Item> items) {
        Map<String, Object> kpis = new HashMap<>();
        
        double totalValue = items.stream().mapToDouble(i -> i.totalValue).sum();
        long criticalCount = items.stream().filter(i -> i.agingDays > 90).count();
        double criticalValue = items.stream().filter(i -> i.agingDays > 90).mapToDouble(i -> i.totalValue).sum();
        
        kpis.put("totalItems", items.size());
        kpis.put("totalValue", totalValue);
        kpis.put("criticalItems", criticalCount);
        kpis.put("criticalValue", criticalValue);
        kpis.put("criticalPercentage", (criticalValue / totalValue) * 100);
        kpis.put("averageAge", items.stream().mapToLong(i -> i.agingDays).average().orElse(0));
        
        return kpis;
    }

    private static ByteArrayOutputStream generateSmartReport(InventoryAnalysis analysis, 
                                                           boolean includeCharts, 
                                                           boolean includeSummary, 
                                                           boolean includeRecommendations) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // Create styles
        Map<String, CellStyle> styles = createStyles(workbook);
        
        if (includeSummary) {
            createExecutiveSummarySheet(workbook, analysis, styles);
        }
        
        createDetailedAgingSheet(workbook, analysis, styles);
        createCategoryAnalysisSheet(workbook, analysis, styles);
        
        if (includeRecommendations) {
            createRecommendationsSheet(workbook, analysis, styles);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out;
    }
    
    private static Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        
        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        styles.put("header", headerStyle);
        
        // Title style
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short)16);
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);
        
        // Critical style
        CellStyle criticalStyle = workbook.createCellStyle();
        criticalStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        criticalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("critical", criticalStyle);
        
        // Currency style
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        styles.put("currency", currencyStyle);
        
        return styles;
    }
    
    private static void createExecutiveSummarySheet(XSSFWorkbook workbook, InventoryAnalysis analysis, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("📈 Executive Summary");
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🚀 Smart Inventory Aging Report - Executive Summary");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum += 2;
        
        // KPIs
        String[][] kpiData = {
            {"Total Items", String.valueOf(analysis.kpis.get("totalItems"))},
            {"Total Value", String.format("$%.2f", (Double)analysis.kpis.get("totalValue"))},
            {"Critical Items (>90 days)", String.valueOf(analysis.kpis.get("criticalItems"))},
            {"Critical Value", String.format("$%.2f", (Double)analysis.kpis.get("criticalValue"))},
            {"Critical Percentage", String.format("%.1f%%", (Double)analysis.kpis.get("criticalPercentage"))},
            {"Average Age (days)", String.format("%.0f", (Double)analysis.kpis.get("averageAge"))}
        };
        
        for (String[] kpi : kpiData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(kpi[0]);
            row.createCell(1).setCellValue(kpi[1]);
        }
        
        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private static void createDetailedAgingSheet(XSSFWorkbook workbook, InventoryAnalysis analysis, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("📅 Detailed Aging Analysis");
        int rowNum = 0;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Aging Bucket", "Item Name", "Category", "Quantity", "Unit Cost", "Total Value", "Date Received", "Days Old", "Risk Level", "Supplier", "Location"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // Data rows
        for (String bucket : analysis.agingGroups.keySet()) {
            for (Item item : analysis.agingGroups.get(bucket)) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(bucket);
                row.createCell(1).setCellValue(item.name);
                row.createCell(2).setCellValue(item.category);
                row.createCell(3).setCellValue(item.quantity);
                
                Cell costCell = row.createCell(4);
                costCell.setCellValue(item.unitCost);
                costCell.setCellStyle(styles.get("currency"));
                
                Cell valueCell = row.createCell(5);
                valueCell.setCellValue(item.totalValue);
                valueCell.setCellStyle(styles.get("currency"));
                
                row.createCell(6).setCellValue(item.dateReceived.toString());
                row.createCell(7).setCellValue(item.agingDays);
                
                Cell riskCell = row.createCell(8);
                riskCell.setCellValue(item.riskLevel);
                if ("Critical".equals(item.riskLevel)) {
                    riskCell.setCellStyle(styles.get("critical"));
                }
                
                row.createCell(9).setCellValue(item.supplier);
                row.createCell(10).setCellValue(item.location);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private static void createCategoryAnalysisSheet(XSSFWorkbook workbook, InventoryAnalysis analysis, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("📋 Category Analysis");
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Category Value Analysis");
        titleCell.setCellStyle(styles.get("title"));
        
        rowNum++;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Category");
        headerRow.createCell(1).setCellValue("Total Value");
        headerRow.createCell(2).setCellValue("Percentage");
        
        // Data
        double totalValue = analysis.categoryValues.values().stream().mapToDouble(Double::doubleValue).sum();
        for (Map.Entry<String, Double> entry : analysis.categoryValues.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            
            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(entry.getValue());
            valueCell.setCellStyle(styles.get("currency"));
            
            row.createCell(2).setCellValue(String.format("%.1f%%", (entry.getValue() / totalValue) * 100));
        }
        
        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private static void createRecommendationsSheet(XSSFWorkbook workbook, InventoryAnalysis analysis, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("💡 AI Recommendations");
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🤖 AI-Powered Inventory Recommendations");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        rowNum += 2;
        
        // Recommendations
        for (String recommendation : analysis.recommendations) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(recommendation);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
            rowNum++; // Add spacing
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
