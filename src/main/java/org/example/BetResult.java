package org.example;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.util.List;
import java.util.Map;

public class BetResult {
    @CsvBindByPosition(position = 0)
    private String teamA;
    @CsvBindByPosition(position = 1)
    private String teamB;
    @CsvBindByPosition(position = 2)
    private String odsFor1;
    @CsvBindByPosition(position = 3)
    private String odsForX;
    @CsvBindByPosition(position = 4)
    private String odsFor2;
    @CsvBindByPosition(position = 5)
    private String date;
    @CsvBindByPosition(position = 6)
    private String time;
    @CsvBindByPosition(position = 7)
    private String division;
    @CsvBindByPosition(position = 8)
    private String link;
    private List<String> errors;
    @CsvBindByPosition(position = 9)
    private String url;
    private Map<String, String> data;

    public BetResult(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getTeamA() {
        return teamA;
    }

    public void setTeamA(String teamA) {
        this.teamA = teamA;
    }

    public String getTeamB() {
        return teamB;
    }

    public void setTeamB(String teamB) {
        this.teamB = teamB;
    }

    public String getOdsFor1() {
        return odsFor1;
    }

    public void setOdsFor1(String odsFor1) {
        this.odsFor1 = odsFor1;
    }

    public String getOdsForX() {
        return odsForX;
    }

    public void setOdsForX(String odsForX) {
        this.odsForX = odsForX;
    }

    public String getOdsFor2() {
        return odsFor2;
    }

    public void setOdsFor2(String odsFor2) {
        this.odsFor2 = odsFor2;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
