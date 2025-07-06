package org.example;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.regex.*;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException {
        File folder = new File("src/main/resources/");
        File[] pdfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (pdfFiles == null || pdfFiles.length == 0) {
            System.out.println("No PDF files found in the folder.");
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoice Data");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Invoice Date");
        header.createCell(1).setCellValue("Taxable Value");
        header.createCell(2).setCellValue("HSN Code");
        header.createCell(3).setCellValue("Ship to State");
        header.createCell(4).setCellValue("IGST");
        header.createCell(5).setCellValue("CGST");
        header.createCell(6).setCellValue("SGST");
        header.createCell(6).setCellValue("SGST");
        header.createCell(7).setCellValue("File Name");


        int rowNum = 1;

        for (File file : pdfFiles) {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            String invoiceDate = extractPattern(text, "(?i)(?:Invoice\\s+Date|Invoice\\s+No\\s+and\\s+Date).*?(\\d{2}-\\d{2}-\\d{4})");
            String taxableValue = extractTaxableValueFromRs(text);
            String hsnCode = extractPattern(text, "Description.*?(6\\d{3,7})");
            String shipState = extractPattern(text, "(?i)SHIP TO:.*?([A-Za-z\\s]+?),\\s*\\d{6}");
            String igst = extractPattern(text, "IGST\\s*@([0-9.]+)%");
            String cgst = extractPattern(text, "CGST\\s*@([0-9.]+)%");
            String sgst = extractPattern(text, "SGST\\s*@([0-9.]+)%");

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(invoiceDate);
            row.createCell(1).setCellValue(taxableValue);
            row.createCell(2).setCellValue(hsnCode);
            row.createCell(3).setCellValue(shipState);
            row.createCell(4).setCellValue(!igst.isEmpty() ? igst + "%" : "");
            row.createCell(5).setCellValue(!cgst.isEmpty() ? cgst + "%" : "");
            row.createCell(6).setCellValue(!sgst.isEmpty() ? sgst + "%" : "");
            row.createCell(7).setCellValue(file.getName());
        }

        try (FileOutputStream fos = new FileOutputStream("invoice-data.xlsx")) {
            workbook.write(fos);
        }
        workbook.close();

        System.out.println("Data extracted from all PDFs and saved to invoice-data.xlsx");
    }

    private static String extractPattern(String text, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
    private static String extractTaxableValueFromRs(String text) {
        Pattern pattern = Pattern.compile("Rs\\.\\s*([0-9]+\\.[0-9]{2})");
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            if (value > 100) {
                count++;
                if (count == 2) { // 2nd value > 100
                    return matcher.group(1);
                }
            }
        }
        return "";
    }
}