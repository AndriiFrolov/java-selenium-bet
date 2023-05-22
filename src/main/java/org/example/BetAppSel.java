package org.example;


import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.OperatingSystem;
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.By;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;

public class BetAppSel {
    public static final String GAME_EL = "//div[@class='subpage-game-row-mobile']";
    public static final String CONTAINER_EL = "//div[@class='bet-card pitkaveto-container']";
    public static final String LEAGUE_EL = "//div[@class='pitkaveto-subpage-bet-row']";
    static String currentDirectory = System.getProperty("user.dir");
    public static final String PATH_CSV = currentDirectory + File.separator + "result.csv";
    public static final String PATH_EXCEL = currentDirectory + File.separator + "result.xlsx";
    static List<String> urls = Arrays.asList(
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-1_Veikkausliiga",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-2_Ykk%C3%B6nen",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-12_Suomen%20cup",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-19_Kakkonen%20Lohko%20A",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-20_Kakkonen%20Lohko%20B",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-21_Kakkonen%20Lohko%20C",
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-8_Kansallinen%20Liiga",

            //other
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-5-1_Espanja%3B1-5-2_Espanja"
    );

    static String URL = "https://www.bmbets.com/search/?query=";

    public static void main(String[] args) throws Exception {
        Configuration.webdriverLogsEnabled = false;
        Configuration.clickViaJs = true;
        Configuration.headless = false; //Set to false if you want to see browser opening
        List<BetResult> results = new ArrayList<>();
        for (int j = 0; j < urls.size(); j++) {
            log("Opening url " + (j + 1) + " out of " + urls.size() + ": " + urls.get(j));
            waitFor(10);
            open(urls.get(j));
            if (j == 0) {
                log("Accepting cookies ");
                $(By.id("save-all-action")).click();
            }
            ElementsCollection containers = getContainers();
            log("-- Found " + containers.size() + " betting containers (that have expand button)");
            for (int l = 1; l <= containers.size(); l++) {
                log("Processing container " + l);
                ElementsCollection expandButton = $$(By.xpath(String.format("(//div[@class='bet-card__footer']/button)[%d]", l)));
                if (expandButton.size() > 1) {
                    log("!! Found expand buttons for container - " + expandButton.size());
                }
                if (expandButton.size() == 1) {
                    expandButton.get(0).click();
                }
                ElementsCollection divisions = getDivisions(l);
                for (int k = 1; k <= divisions.size(); k++) {
                    String league = "(" + CONTAINER_EL + LEAGUE_EL + ")[" + k + "]";

                    String divisionName = getIfPresentNotRoot(league + "//div[@class='subpage-bet-row__title-row__sport-title']/h3");
                    log("---- Fetching data for division " + k + "(" + divisionName + ")");
                    ElementsCollection games = $$(By.xpath(league + GAME_EL));
                    log("------ Found games - " + games.size());
                    for (int i = 1; i <= games.size(); i++) {
                        log("-------- Fetching data for game " + i);
                        BetResult betResult = new BetResult(urls.get(j));
                        String teamA = getIfPresent(i, "//span[contains(@class, 'team--home')]");
                        betResult.setTeamA(teamA);
                        betResult.setTeamB(getIfPresent(i, "//span[contains(@class, 'team--away')]"));

                        betResult.setDate(getIfPresent(i, "//span[contains(@class, 'date-label')]"));
                        betResult.setTime(getIfPresent(i, "//div[contains(@class, 'time-label')]/span[2]"));
                        betResult.setOdsFor1(getIfPresent(i, "//div[contains(@class, 'buttons-three-columns')]/button[1]"));
                        betResult.setOdsForX(getIfPresent(i, "//div[contains(@class, 'buttons-three-columns')]/button[2]"));
                        betResult.setOdsFor2(getIfPresent(i, "//div[contains(@class, 'buttons-three-columns')]/button[3]"));

                        betResult.setDivision(divisionName);

                        betResult.setLink(URL + transformA(teamA));

                        results.add(betResult);
                    }
                }
            }
        }
        saveCSV(results);
        saveExcel(results);
    }

    private static ElementsCollection getContainers() {
        ElementsCollection containers = $$(By.xpath(CONTAINER_EL));
        if (containers.size() == 0) {
            sleep(10); //loads slowly sometimes
            containers = $$(By.xpath(CONTAINER_EL));
        }
        if (containers.size() == 0) {
            sleep(20); //loads slowly sometimes
            containers = $$(By.xpath(CONTAINER_EL));
        }
        if (containers.size() == 0) {
            log("---- Found 0 containers by xpath " + CONTAINER_EL);
        } else {
            log("---- Found containers - " + containers.size());
        }
        return containers;
    }

    private static ElementsCollection getDivisions(int l) {
        String xpath = String.format("((%s)[%d])%s", CONTAINER_EL, l, LEAGUE_EL);
        ElementsCollection divisions;
        int attempt = 0;
        do {
           attempt++;
           sleep(5);
           divisions = $$(By.xpath(xpath));
       } while (divisions.size() == 0 && attempt < 5);
        if (divisions.size() == 0) {
            log("---- Found 0 divisions/leagues by xpath " + xpath);
        } else {
            log("---- Found divisions/leagues - " + divisions.size());
        }
        return divisions;
    }

    private static void saveExcel(List<BetResult> results) {
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet("Sheet1");

            // Writing header
            Row headerRow = sheet.createRow(0);
            String[] header = {"Team A", "Team B", "1", "X", "2", "Date", "Time", "Division", "Link", "For URL"};
            for (int i = 0; i < header.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header[i]);
            }

            int rowNum = 1;
            for (BetResult betResult : results) {
                Row dataRow = sheet.createRow(rowNum++);
                int colNum = 0;
                Cell cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getTeamA());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getTeamB());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getOdsFor1());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getOdsForX());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getOdsFor2());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getDate());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getTime());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getDivision());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getLink());

                cell = dataRow.createCell(colNum++);
                cell.setCellValue(betResult.getUrl());
            }

            // Writing to file
            try (FileOutputStream fileOut = new FileOutputStream(PATH_EXCEL)) {
                workbook.write(fileOut);
                System.out.println("Data has been written to " + PATH_EXCEL);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveCSV(List<BetResult> results) throws
            IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        try (Writer writer = new FileWriter(PATH_CSV.toString())) {

            StatefulBeanToCsv<BetResult> sbc = new StatefulBeanToCsvBuilder<BetResult>(writer)
                    .withQuotechar('\'')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            sbc.write(results);
        }
    }

    private static String getIfPresentNotRoot(String xpath) {
        ElementsCollection $$ = $$(By.xpath(xpath));
        if ($$.size() == 0) {
            return "";
        }
        if ($$.size() > 1) {
            log("More than 1 result found for xpath: " + xpath);
        }
        return $$.get(0).getText();
    }

    private static String getIfPresent(int i, String xpath) {
        return getIfPresentNotRoot(String.format("(//div[@class='subpage-game-row-mobile'])[%d]", i) + xpath);
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void waitFor(long s) {
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String transformA(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
