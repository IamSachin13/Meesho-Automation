package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.meesho.Account;
import org.example.meesho.ExcelRowData;
import org.example.utils.UIHelper;
import org.example.utils.Waits;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DailyReturn {

    public static void main(String[] args) throws AWTException {
        String folderName = "Excel";
        String fileName = folderName + "/return.xlsx";
        File file = new File(fileName);
        if (file.exists() && !file.renameTo(file)) {
            System.out.println("File is currently open. Please close it and try again.");
            return;
        }


        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Account> listOfAccount = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String[] parts = properties.getProperty("account." + i).split(",");
            listOfAccount.add(new Account(parts[0], parts[1], parts[2]));
        }

        List<RowData> excelRows = new ArrayList<>();

        for (Account account: listOfAccount ) {
            String url = "https://supplier.meesho.com/panel/v3/new/root/login";
            String phoneNumber = account.getPhone();
            String password = account.getPassword();
            WebDriver driver = new ChromeDriver();
            driver.manage().window().maximize();


            By phoneNumberLocator = By.xpath("//input[@name='emailOrPhone']");
            By passwordLocator = By.xpath("//input[@name='password']");
            By loginLocator = By.xpath("//*[text()='Log in']");
            By returnLocator = By.xpath("//p[text()='Returns']");
            By returnTarkingLocator = By.xpath("//button[starts-with(text(),'Return Tracking')]");
            By allFilterLocator = By.xpath("//p[text()='All Filters']");
            By yesterdayLocator = By.xpath("//p[text()='Yesterday']");
            By applyLocator = By.xpath("//span[text()='Apply']");

            By subOrderIdLocator = By.xpath("./td[2]//p");
            By returnReasonLocator = By.xpath("./td[3]//p");

            By returnTypeAndDate = By.xpath("./td[5]//p");

            By modalLocator = By.xpath("//div[@role='dialog']");

            By closeModal = By.xpath("//div[@role='dialog']//*[local-name()='svg'][.//*[local-name()='path' and @d='M5.293 5.293a1 1 0 011.414 0L12 10.586l5.293-5.293a1 1 0 111.414 1.414L13.414 12l5.293 5.293a1 1 0 01-1.414 1.414L12 13.414l-5.293 5.293a1 1 0 01-1.414-1.414L10.586 12 5.293 6.707a1 1 0 010-1.414z']]");
            By closeSmallModal = By.xpath("//*[local-name()='svg'][.//*[local-name()='path' and @d='M5.293 5.293a1 1 0 011.414 0L12 10.586l5.293-5.293a1 1 0 111.414 1.414L13.414 12l5.293 5.293a1 1 0 01-1.414 1.414L12 13.414l-5.293 5.293a1 1 0 01-1.414-1.414L10.586 12 5.293 6.707a1 1 0 010-1.414z']]");


            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            Waits wait = new Waits(driver);
            UIHelper helper = new UIHelper(driver);

            driver.get(url);
            Robot robot = new Robot();

            // Press CTRL + Minus 5 times to zoom out (approx to 50%)
            for (int i = 0; i < 6; i++) {
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_SUBTRACT);
                robot.keyRelease(KeyEvent.VK_SUBTRACT);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                try {
                    Thread.sleep(500); // Small delay between key presses
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            driver.findElement(phoneNumberLocator).sendKeys(phoneNumber);
            driver.findElement(passwordLocator).sendKeys(password);
            driver.findElement(loginLocator).click();

            if (!driver.findElements(modalLocator).isEmpty()) {
                driver.findElement(closeModal).click();
            }
            helper.scrollAndClick(returnLocator);

            if (!driver.findElements(modalLocator).isEmpty()) {
                driver.findElement(closeModal).click();
            }


            helper.scrollAndClick(returnTarkingLocator);

            helper.scrollAndClick(By.xpath("//button[text()='Disposed']"));
            helper.scrollAndClick(By.xpath("//button[text()='In transit']"));

            helper.scrollAndClick(allFilterLocator);
            helper.scrollAndClick(yesterdayLocator);
            helper.scrollAndClick(applyLocator);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            By tableLocator = By.cssSelector(".MuiTableContainer-root");
            By rowsLocator = By.cssSelector("tbody tr");

            int previousRowCount = 0;
            boolean flag = false;
            List<WebElement> listOfRows  = new ArrayList<>();

            while (!flag) {
                List<WebElement> rows = new ArrayList<>();
                int attempts = 0;
                while (attempts < 2) {
                    try {
                        wait.waitForLocatorToVisible(tableLocator);
                        WebElement tableElement = driver.findElement(tableLocator);
                        wait.waitForLocatorToVisible(rowsLocator);
                        rows = tableElement.findElements(rowsLocator);
                        helper.scrollToElement(rows.get(rows.size()-1));
                        break;  // Success, exit loop
                    } catch (StaleElementReferenceException e) {
                        System.out.println("exception occur but handled..");
                        attempts++;
                    }catch (TimeoutException e){
                        System.out.println(account.getName() + "=====>"+"No return Present");
                        flag = true;
                        break;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (rows.size() == previousRowCount) {
                    listOfRows = rows;
                    break;
                }
                previousRowCount = rows.size();
            }

            if (flag){
                driver.quit();
                continue;
            }

            System.out.println("Size of Rows" + listOfRows.size());

            for (WebElement row : listOfRows) {

                String subOrderId = row.findElement(subOrderIdLocator).getText();
                System.out.println(subOrderId);
                List<WebElement> list = row.findElements(returnTypeAndDate);
                String date = list.get(0).getText();
                System.out.println(date);
                String returnType = list.get(1).getText();
                System.out.println(returnType);

                String returnReason = row.findElement(returnReasonLocator).getText();


                excelRows.add(new RowData(account.getName(),date,subOrderId,returnType,returnReason));
            }
            driver.quit();
        }

        appendToExcel(excelRows);
    }
    public static void appendToExcel(List<RowData> dataList) {
        String folderName = "Excel";
        File folder = new File(folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = folderName + "/return.xlsx";
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
                headerRow.createCell(3).setCellValue("return Type");
                headerRow.createCell(4).setCellValue("return Reason");

            } else {
                sheet = workbook.getSheetAt(0); // Get existing sheet
            }

            int lastRowNum = sheet.getLastRowNum();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Append data rows
            for (RowData data : dataList) {
                Row row = sheet.createRow(++lastRowNum);
                row.createCell(0).setCellValue(data.getName());
                row.createCell(1).setCellValue(data.getDate());
                row.createCell(2).setCellValue(data.getSubOrderId());
                row.createCell(3).setCellValue(data.getReturnDate());
                row.createCell(4).setCellValue(data.getReturnReason());

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


}

class RowData{
    String name ;
    String date ;
    String subOrderId ;
    String returnDate;
    String returnReason;

    public RowData(String name,String date, String subOrderId, String returnDate, String returnReason) {
        this.name = name;
        this.date = date;
        this.subOrderId = subOrderId;
        this.returnDate = returnDate;
        this.returnReason = returnReason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubOrderId() {
        return subOrderId;
    }

    public void setSubOrderId(String subOrderId) {
        this.subOrderId = subOrderId;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }
}
