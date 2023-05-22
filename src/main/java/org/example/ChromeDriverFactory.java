package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverFactory {

    public WebDriver getDriver() {

        // Set Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("always-authorize-plugins");
        options.addArguments("allow-running-insecure-content");
        options.addArguments("ignore-certificate-errors");
        options.addArguments("start-maximized");

        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }

}
