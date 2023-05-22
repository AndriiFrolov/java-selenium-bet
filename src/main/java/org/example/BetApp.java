package org.example;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BetApp {

    static List<String> urls = Arrays.asList(
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-1_Veikkausliiga",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-2_Ykk%C3%B6nen",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-12_Suomen%20cup",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-19_Kakkonen%20Lohko%20A",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-20_Kakkonen%20Lohko%20B",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-21_Kakkonen%20Lohko%20C",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-8_Kansallinen%20Liiga"
    );

    static String URL = "https://www.bmbets.com/search/?query=";
    public static void main(String[] args) throws Exception {
        WebDriver driver = new ChromeDriverFactory().getDriver();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        List<BetResult> results = new ArrayList<>();
        for (String url : urls) {
            driver.get(url);
            driver.findElement(By.id("save-all-action")).click();
            List<WebElement> allBetResultsOnPage = driver.findElements(By.xpath("//div[@class='subpage-game-row-mobile']"));
            for (int i = 0; i < allBetResultsOnPage.size(); i++) {
                WebElement betResultEl = allBetResultsOnPage.get(i);
                BetResult betResult = new BetResult(url);
                String teamA = betResultEl.findElement(By.xpath("//span[contains(@class, 'team--home')]")).getText();
                betResult.setTeamA(teamA);
                betResult.setTeamB(betResultEl.findElement(By.xpath("//span[contains(@class, 'team--away')]")).getText());

                betResult.setDate(betResultEl.findElement(By.xpath("//span[contains(@class, 'date-label')]")).getText());
                betResult.setTime(betResultEl.findElement(By.xpath("//div[contains(@class, 'time-label')]/span[2]")).getText());
                betResult.setOdsFor1(betResultEl.findElement(By.xpath("//div[contains(@class, 'buttons-three-columns')]/button[1]")).getText());
                betResult.setOdsForX(betResultEl.findElement(By.xpath("//div[contains(@class, 'buttons-three-columns')]/button[2]")).getText());
                betResult.setOdsFor2(betResultEl.findElement(By.xpath("//div[contains(@class, 'buttons-three-columns')]/button[3]")).getText());
                betResult.setDivision(betResultEl.findElement(By.xpath(String.format("//div[@class='subpage-bet-row__title-row__sport-title']/h3[%d]", i + 1))).getText());

                betResult.setLink(URL + transformA(teamA));

                results.add(betResult);
            }

        }
        int c = 0;

    }

    private static String transformA(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
