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

    private BigDecimal full_sum;

    private BigDecimal win_sum;

    private String winner;

    private String product;

    public Tender() {
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
}
