package org.example;

import org.example.meesho.Account;
import org.example.meesho.ExcelRowData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AcceptAllOrder {

    public static void main(String[] args) {

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
            By pendingLocator = By.xpath("//button[starts-with(text(),'Pending')]");

            By selectAllRowsLocator = By.xpath("//input[@aria-label='select all rows']");

            By acceptSelectedOrderLocator = By.xpath("//button/span[text()='Accept Selected Orders']");
            By acceptOrderLocator = By.xpath("//button/span[text()='Accept Order']");
//            refresh the page loop
            By gotItButtonLocator = By.xpath("//button/span[text()='Got it']");

            By readyToShipLocator = By.xpath("//button[starts-with(text(),'Ready to Ship')]");

            By clickOnDownloadLabelLocator = By.xpath("(//button//span[text()='Label'])[count(//button//span[text()='Label'])]");

            By checkDownloadLabelStatusLocator = By.xpath("//*[contains(text(),'Labels generated successfully')]");

            By modalLocator = By.xpath("//div[@role='dialog']");

            By closeModal = By.xpath("//*[local-name()='svg'][.//*[local-name()='path' and @d='M5.293 5.293a1 1 0 011.414 0L12 10.586l5.293-5.293a1 1 0 111.414 1.414L13.414 12l5.293 5.293a1 1 0 01-1.414 1.414L12 13.414l-5.293 5.293a1 1 0 01-1.414-1.414L10.586 12 5.293 6.707a1 1 0 010-1.414z']]");



            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            driver.get(url);

            driver.findElement(phoneNumberLocator).sendKeys(phoneNumber);
            driver.findElement(passwordLocator).sendKeys(password);
            driver.findElement(loginLocator).click();

            if (!driver.findElements(modalLocator).isEmpty()) {
                driver.findElement(closeModal).click();
            }else{
                System.out.println("No Modal is Present");
            }


            driver.findElement(orderLocator).click();

            driver.findElement(pendingLocator).click();

            if (driver.findElements(selectAllRowsLocator).isEmpty()) {
                System.out.println("No Order present for Account : " + account.getName());
            }else {
                driver.findElement(selectAllRowsLocator).click();

                driver.findElement(acceptSelectedOrderLocator).click();

                driver.findElement(acceptOrderLocator).click();
                int count = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                        count++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    List<WebElement> gotIt = driver.findElements(gotItButtonLocator);
                    if (!gotIt.isEmpty()) break;
                    if (count==3){
                        driver.navigate().refresh();
                    }
                }

                driver.findElement(gotItButtonLocator).click();
            }

            driver.findElement(readyToShipLocator).click();
            System.out.println("Clicked on ready To ship");
            driver.findElement(selectAllRowsLocator).click();
            System.out.println("selected all rows");
            driver.findElement(clickOnDownloadLabelLocator).click();

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                List<WebElement> status = driver.findElements(checkDownloadLabelStatusLocator);
                if (!status.isEmpty()) break;
                System.out.println("waiting for Labels generated successfully..................");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            driver.findElement(clickOnDownloadLabelLocator).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            driver.quit();
        }
    }
}
