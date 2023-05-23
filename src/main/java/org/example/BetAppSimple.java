package org.example;


import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.By;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.codeborne.selenide.Selenide.*;

public class BetAppSimple {
    public static final String GAME_EL = "//div[@class='subpage-game-row-mobile']";

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
            "https://www.veikkaus.fi/fi/vedonlyonti?t=1-1-8_Kansallinen%20Liiga"
    );

    static String URL = "https://www.bmbets.com/search/?query=";

    // Define the desired date format
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {
        Configuration.webdriverLogsEnabled = false;
        Configuration.clickViaJs = true;
        Configuration.headless = false; //Set to false if you want to see browser opening
        List<BetResult> results = new ArrayList<>();
        for (int j = 0; j < urls.size(); j++) {
            String url = urls.get(j);
            log("Opening url " + (j + 1) + " out of " + urls.size() + ": " + url);
            open(url);
            waitABit();

            if (j == 0) {
                log("Accepting cookies ");
                $(By.id("save-all-action")).click();
            }
            String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
            if (currentUrl.equals(url)) {
                expandAll();
                ElementsCollection games = $$(By.xpath(GAME_EL));
                if (games.size() == 0) {
                    waitABit();
                    games = $$(By.xpath(GAME_EL));
                }
                log("------ Found games - " + games.size());
                for (int i = 1; i <= games.size(); i++) {
                    log("-------- Fetching data for game " + i);
                    BetResult betResult = new BetResult(url);
                    String teamA = getIfPresent(i, "//span[contains(@class, 'team--home')]");
                    betResult.setTeamA(teamA);
                    betResult.setTeamB(getIfPresent(i, "//span[contains(@class, 'team--away')]"));

                    String date = getIfPresent(i, "//span[contains(@class, 'date-label')]");
                    String dateToShow = getDate(date);

                    betResult.setDate(dateToShow);
                    betResult.setTime(getIfPresent(i, String.format("//div[contains(@class, 'time-label')]/span[%d]", StringUtils.isEmpty(date) ? 1 : 2)));
                    betResult.setOdsFor1(getIfPresent(i, "//div[contains(@class, 'buttons-three-columns')]/button[1]"));
                    betResult.setOdsForX(getIfPresent(i, "//div[contains(@class, 'buttons-three-columns')]/button[2]"));
                    betResult.setOdsFor2(getIfPresent(i, "//div[contains(@class, 'buttons-three-columns')]/button[3]"));

                    betResult.setDivision(games.get(i - 1).$(By.xpath("..//..//h3")).getText());

                    betResult.setLink(URL + transformA(teamA));

                    results.add(betResult);
                }
            } else {
                log("URL " + url + " redirected to " + currentUrl + ", so skipping it");
            }

        }
        saveCSV(results);
        saveExcel(results);
    }

    private static void expandAll() {
        for (SelenideElement el : $$(By.xpath("//div[@class='bet-card__footer']/button"))) {
            try {
                if (el.isDisplayed()) {
                    el.click();
                }
            } catch (Exception e) {
                log("Could not expand one button");

            }
        }

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

                cell = dataRow.createCell(colNum);
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
        try (Writer writer = new FileWriter(PATH_CSV)) {

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

    private static void waitABit() {
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String transformA(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8).toLowerCase();
    }

    private static String getDate(String date) {
        Map<String, String> res = new HashMap<>();
        res.put("Ma", "Monday");
        res.put("Ti", "Tuesday"); //?
        res.put("Ke", "Wednesday");
        res.put("To", "Thursday");
        res.put("Pe", "Friday"); // ?
        res.put("La", "Saturday");
        res.put("Su", "Sunday");

        // Get the current date
        LocalDate currentDate = LocalDate.now();
        if (StringUtils.isEmpty(date)) {
            //Means date is today
            return "Today, " + currentDate.format(formatter);
        } else if (date.contains(".")) {
            //Means date is not in upcoming week
            // Split the input date string into day and month
            String[] parts = date.split("\\.");
            // Get the current year
            int currentYear = currentDate.getYear();
            // Create a date string in the format "yyyy-MM-dd"
            String dateString = currentYear + "-" + parts[1] + "-" + parts[0];

            // Parse the date string into a LocalDate object
            LocalDate parsedDate = LocalDate.parse(dateString, formatter);
            return parsedDate.getDayOfWeek().name() + ", " + parsedDate.format(formatter);
        } else if (res.containsKey(date)) {
            //Means date is in upcoming week, shows as day of week
            String weekDay = res.get(date);
            LocalDate nextDayOfWeek = getNextDayOfWeek(currentDate, weekDay);
            // Format the next occurrence of the day of the week into the desired format
            return weekDay + ", " + nextDayOfWeek.format(formatter);

        } else {
            log("!! Could not get date from " + date);
            return "";
        }

    }

    private static LocalDate getNextDayOfWeek(LocalDate currentDate, String inputDayOfWeek) {
        // Parse the input day of the week into a DayOfWeek enum
        DayOfWeek targetDayOfWeek = DayOfWeek.valueOf(inputDayOfWeek.toUpperCase());

        // Get the current day of the week
        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

        // Calculate the difference in days between the current day of the week and the target day of the week
        int daysToAdd = targetDayOfWeek.getValue() - currentDayOfWeek.getValue();

        if (daysToAdd <= 0) {
            daysToAdd += 7; // Add 7 days to get the next occurrence
        }

        // Add the calculated number of days to the current date to get the next occurrence of the day of the week
        return currentDate.plusDays(daysToAdd);
    }


}


