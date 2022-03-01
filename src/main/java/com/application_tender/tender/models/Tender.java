package com.application_tender.tender.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

public class Tender {
    private Long id;

    private  String customer;
    private String inn;

    private String name_tender;

    private String number_tender;

    private String bico_tender;

    private String gos_zakupki;

    private String typetender;

    private BigDecimal price;

    private String currency;

    private double rate;

    private BigDecimal sum;

    private ZonedDateTime date_start;

    private  ZonedDateTime date_finish;

    private  ZonedDateTime date_tranding;

    private BigDecimal full_sum;

    private BigDecimal win_sum;

    private String winner;

    private String product;

    private boolean dublicate;
    private String country;

    private String winner_country;

    private String winner_inn;

    private boolean plan;

    private String tender_plan;

    private String tender_dublicate;
    public Tender() {
    }
    public Tender(String name){
        this.name_tender = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getName_tender() {
        return name_tender;
    }

    public void setName_tender(String name_tender) {
        this.name_tender = name_tender;
    }

    public String getNumber_tender() {
        return number_tender;
    }

    public void setNumber_tender(String number_tender) {
        this.number_tender = number_tender;
    }

    public String getBico_tender() {
        return bico_tender;
    }

    public void setBico_tender(String bico_tender) {
        this.bico_tender = bico_tender;
    }

    public String getGos_zakupki() {
        return gos_zakupki;
    }

    public void setGos_zakupki(String gos_zakupki) {
        this.gos_zakupki = gos_zakupki;
    }

    public String getTypetender() {
        return typetender;
    }

    public void setTypetender(String typetender) {
        this.typetender = typetender;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public ZonedDateTime getDate_start() {
        return date_start;
    }

    public void setDate_start(ZonedDateTime date_start) {
        this.date_start = date_start;
    }

    public ZonedDateTime getDate_finish() {
        return date_finish;
    }

    public void setDate_finish(ZonedDateTime date_finish) {
        this.date_finish = date_finish;
    }

    public ZonedDateTime getDate_tranding() {
        return date_tranding;
    }

    public void setDate_tranding(ZonedDateTime date_tranding) {
        this.date_tranding = date_tranding;
    }

    public BigDecimal getFull_sum() {
        return full_sum;
    }

    public void setFull_sum(BigDecimal full_sum) {
        this.full_sum = full_sum;
    }

    public BigDecimal getWin_sum() {
        return win_sum;
    }

    public void setWin_sum(BigDecimal win_sum) {
        this.win_sum = win_sum;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public boolean isDublicate() {
        return dublicate;
    }

    public void setDublicate(boolean dublicate) {
        this.dublicate = dublicate;
    }



    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWinner_country() {
        return winner_country;
    }

    public void setWinner_country(String winner_country) {
        this.winner_country = winner_country;
    }

    public String getWinner_inn() {
        return winner_inn;
    }

    public void setWinner_inn(String winner_inn) {
        this.winner_inn = winner_inn;
    }

    public boolean isPlan() {
        return plan;
    }

    public void setPlan(boolean plan) {
        this.plan = plan;
    }

    public String getTender_plan() {
        return tender_plan;
    }

    public void setTender_plan(String tender_plan) {
        this.tender_plan = tender_plan;
    }

    public String getTender_dublicate() {
        return tender_dublicate;
    }

    public void setTender_dublicate(String tender_dublicate) {
        this.tender_dublicate = tender_dublicate;
    }

    @Override
    public String toString() {
        return "Tender{" +
                "id=" + id +
                ", customer='" + customer + '\'' +
                ", inn='" + inn + '\'' +
                ", name_tender='" + name_tender + '\'' +
                ", number_tender='" + number_tender + '\'' +
                ", bico_tender='" + bico_tender + '\'' +
                ", gos_zakupki='" + gos_zakupki + '\'' +
                ", typetender='" + typetender + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                ", rate=" + rate +
                ", sum=" + sum +
                ", date_start=" + date_start +
                ", date_finish=" + date_finish +
                ", date_tranding=" + date_tranding +
                ", full_sum=" + full_sum +
                ", win_sum=" + win_sum +
                ", winner='" + winner + '\'' +
                ", product='" + product + '\'' +
                ", dublicate=" + dublicate +
                ", country='" + country + '\'' +
                ", winner_country='" + winner_country + '\'' +
                ", winner_inn='" + winner_inn + '\'' +
                ", plan=" + plan +
                ", tender_plan='" + tender_plan + '\'' +
                ", tender_dublicate='" + tender_dublicate + '\'' +
                '}';
    }
}
