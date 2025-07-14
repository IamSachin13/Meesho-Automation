package org.example.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class UIHelper {
    private final WebDriver driver;
    private final JavascriptExecutor js;
    private final Actions actions;
    private final Waits wait;


    public UIHelper(WebDriver driver) {
        this.driver = driver;
        actions = new Actions(driver);
        wait = new Waits(driver);
        this.js = (JavascriptExecutor) driver;
    }

    public void scrollAndClick(WebElement element) {
        wait.waitForElementToVisible(element);
        scrollToElement(element);
        element.click();
    }

    public void scrollAndClick(By locator) {
        wait.waitForLocatorToVisible(locator);
        WebElement element = driver.findElement(locator);
        scrollToElement(element);
        element.click();
    }
    public void scrollAndWrite(WebElement element,String text) {
        wait.waitForElementToVisible(element);
        scrollToElement(element);
        element.sendKeys(text);
    }
    public void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }
    public void waitForElementToVisible(WebElement element){
        wait.waitForElementToVisible(element);
    }

    public void clickElementWithCtrl(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        actions.keyDown(Keys.CONTROL)
                .click(element)
                .keyUp(Keys.CONTROL)
                .build()
                .perform();
    }

    public boolean textVisibility(String text){
        By locator = By.xpath("//*[normalize-space(text())='"+text+"']");
        wait.waitForLocatorToVisible(locator);
        List<WebElement> elements = driver.findElements(locator);
        return !elements.isEmpty();
    }

    public boolean locatorIsPresent(By locator){
        wait.waitForLocatorToVisible(locator);
        List<WebElement> elements = driver.findElements(locator);
        return !elements.isEmpty();
    }

    public boolean elementIsPresent(WebElement element){
        wait.waitForElementToVisible(element);
        return element.isDisplayed();
    }


    public String getText(By locator) {
        return driver.findElement(locator).getText();
    }
    public String getText(WebElement ele ,By locator) {
        scrollToElement(ele);
        return ele.findElement(locator).getText();
    }


}
