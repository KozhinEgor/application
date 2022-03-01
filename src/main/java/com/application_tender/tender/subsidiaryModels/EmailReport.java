package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Tender;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class EmailReport {
    String name_tender;
    ZonedDateTime date_start;
    ZonedDateTime date_finish;
    Integer number;
    Integer numberWithPrice;
    String type_tender;
    String customer;
    Long customer_id;
    BigDecimal full_sum;
    String currency;
    BigDecimal price;
    List<Tender> tenderIn;

    public EmailReport() {
    }

    public String getName_tender() {
        return name_tender;
    }

    public void setName_tender(String name_tender) {
        this.name_tender = name_tender;
    }

    public ZonedDateTime getDate_finish() {
        return date_finish;
    }

    public void setDate_finish(ZonedDateTime date_finish) {
        this.date_finish = date_finish;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getType_tender() {
        return type_tender;
    }

    public void setType_tender(String type_tender) {
        this.type_tender = type_tender;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Long getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Long customer_id) {
        this.customer_id = customer_id;
    }

    public BigDecimal getFull_sum() {
        return full_sum;
    }

    public void setFull_sum(BigDecimal full_sum) {
        this.full_sum = full_sum;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<Tender> getTenderIn() {
        return tenderIn;
    }

    public void setTenderIn(List<Tender> tenderIn) {
        this.tenderIn = tenderIn;
    }

    public ZonedDateTime getDate_start() {
        return date_start;
    }

    public void setDate_start(ZonedDateTime date_start) {
        this.date_start = date_start;
    }

    public Integer getNumberWithPrice() {
        return numberWithPrice;
    }

    public void setNumberWithPrice(Integer numberWithPrice) {
        this.numberWithPrice = numberWithPrice;
    }
}
