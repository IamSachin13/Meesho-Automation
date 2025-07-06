package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.meesho.Account;
import org.example.meesho.ExcelRowData;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MeeshoDailyPL {
    public static void main(String[] args) {
        String folderName = "Excel";
        String fileName = folderName + "/stats.xlsx";
        File file = new File(fileName);
        if (file.exists() && !file.renameTo(file)) {
            System.out.println("File is currently open. Please close it and try again.");
            return;
        }

        LocalDate currentDate = LocalDate.now();

        List<ExcelRowData> excelRows = new ArrayList<>();
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Account> listOfAccount = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String[] parts = properties.getProperty("account." + i).split(",");
            listOfAccount.add(new Account(parts[0], parts[1], parts[2]));
        }

        for (Account account: listOfAccount ) {
            String url = "https://supplier.meesho.com/panel/v3/new/root/login";
            String phoneNumber = account.getPhone();
            String password = account.getPassword();
            WebDriver driver = new ChromeDriver();
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

            By phoneNumberLocator = By.xpath("//input[@name='emailOrPhone']");
            By passwordLocator = By.xpath("//input[@name='password']");
            By loginLocator = By.xpath("//*[text()='Log in']");
            By orderLocator = By.xpath("//p[text()='Orders']");
            By readyToShipLocator = By.xpath("//button[starts-with(text(),'Ready to Ship')]");
            By subOrderIdLocator = By.xpath("./td[3]//p");
            By sukIdLocator = By.xpath("./td[4]//p");
            By quantityLocator = By.xpath("./td[6]//p");
            By sizeLocator = By.xpath("./td[7]//p");

            By modalLocator = By.xpath("//div[@role='dialog']");

            By closeModal = By.xpath("//*[local-name()='svg'][.//*[local-name()='path' and @d='M5.293 5.293a1 1 0 011.414 0L12 10.586l5.293-5.293a1 1 0 111.414 1.414L13.414 12l5.293 5.293a1 1 0 01-1.414 1.414L12 13.414l-5.293 5.293a1 1 0 01-1.414-1.414L10.586 12 5.293 6.707a1 1 0 010-1.414z']]");





            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            driver.get(url);

            driver.findElement(phoneNumberLocator).sendKeys(phoneNumber);
            driver.findElement(passwordLocator).sendKeys(password);
            driver.findElement(loginLocator).click();

            if (!driver.findElements(modalLocator).isEmpty()) {
                driver.findElement(closeModal).click();
            }


            driver.findElement(orderLocator).click();

            driver.findElement(readyToShipLocator).click();

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Locate your table
            WebElement tableElement = driver.findElement(By.xpath("//div[@class='MuiTableContainer-root css-gxsp0q']")); // Adjust locator as needed

            int previousRowCount = 0;

            while (true) {
                List<WebElement> rows = new ArrayList<>();
                int attempts = 0;
                while (attempts < 2) {
                    try {
                        tableElement = driver.findElement(By.xpath("//div[@class='MuiTableContainer-root css-gxsp0q']"));
                        rows = tableElement.findElements(By.tagName("tr"));
                        break;  // Success, exit loop
                    } catch (StaleElementReferenceException e) {
                        System.out.println("exception occur but handled..");
                        attempts++;
                    }
                }
                js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight", tableElement);

                // Wait for rows to load (adjust time as per your app's behavior)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Check if new rows are loaded
                rows = tableElement.findElements(By.tagName("tr"));
                if (rows.size() == previousRowCount) {
                    // No new rows loaded, exit loop
                    break;
                }

                previousRowCount = rows.size();
            }


            List<WebElement> listOfRows = driver.findElements(By.xpath("//tbody/tr"));
            System.out.println("Size of Rows" + listOfRows.size());

            for (WebElement row : listOfRows) {
                List<WebElement> check = row.findElements(By.xpath(".//p[text()='Downloaded']"));
                if(check.isEmpty()) continue;
                String subOrderId = row.findElement(subOrderIdLocator).getText();
                System.out.println(subOrderId);
                String skuId = row.findElement(sukIdLocator).getText();
                System.out.println(skuId);
                String quantity = row.findElement(quantityLocator).getText();
                System.out.println(quantity);
                String size = row.findElement(sizeLocator).getText();
                System.out.println(size);

                excelRows.add(new ExcelRowData(account.getName(),currentDate,subOrderId,skuId,quantity,size));
            }
            driver.quit();
        }

        appendToExcel(excelRows);

    }



    public static void appendToExcel(List<ExcelRowData> dataList) {
        String folderName = "Excel";
        File folder = new File(folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = folderName + "/stats.xlsx";
        File file = new File(fileName);

        try (Workbook workbook = file.exists()
                ? new XSSFWorkbook(new FileInputStream(file))
                : new XSSFWorkbook()) {

            Sheet sheet;

            if (workbook.getNumberOfSheets() == 0) {
                sheet = workbook.createSheet("Data");

                // Create header row if sheet is newly created
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Name");
                headerRow.createCell(1).setCellValue("Date");
                headerRow.createCell(2).setCellValue("SubOrder ID");
                headerRow.createCell(3).setCellValue("SKU ID");
                headerRow.createCell(4).setCellValue("Pcs");
                headerRow.createCell(5).setCellValue("Quantity");
                headerRow.createCell(6).setCellValue("Size");
            } else {
                sheet = workbook.getSheetAt(0); // Get existing sheet
            }

            int lastRowNum = sheet.getLastRowNum();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Append data rows
            for (ExcelRowData data : dataList) {
                Row row = sheet.createRow(++lastRowNum);
                row.createCell(0).setCellValue(data.getname());
                row.createCell(1).setCellValue(data.getDate().format(formatter));
                row.createCell(2).setCellValue(data.getSubOrderId());
                row.createCell(3).setCellValue(data.getSkuId());
                row.createCell(4).setCellValue(extractPcNumber(data.getSkuId()));
                row.createCell(5).setCellValue(data.getQuantity());
                row.createCell(6).setCellValue(data.getSize());
                row.createCell(7).setCellValue(extractPcNumber(data.getSkuId())*Integer.parseInt(data.getQuantity()));

            }

            // Autosize columns (optional)
            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write back to file
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            }

            System.out.println("Rows added successfully to: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int extractPcNumber(String input) {
        if (input == null || input.isEmpty()) {
            return -1; // Return -1 to indicate invalid input
        }

        // Regular expression to find a number followed by "pc" (case-insensitive)
        String regex = "(\\d+)\\s*pc";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1; // Return -1 if "pc" not found
    }

//    public static void waitForLocator(WebDriverWait wait, By locator){
//
//    }
}