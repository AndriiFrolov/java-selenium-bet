package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
