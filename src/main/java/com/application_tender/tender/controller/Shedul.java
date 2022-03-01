package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class Shedul {
    @Autowired
    private GetCurrency getCurrency;
    private final TableMapper tableMapper;
    private final DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    Shedul(TableMapper tableMapper){
        this.tableMapper =tableMapper;
    }

    @Scheduled(cron = "0 0 4 * * *") // Каждый день в 4 часа утра
    public void setCurrency() {
        Map<String, Double> currency = new HashMap<>();
        currency = getCurrency.currency(ZonedDateTime.now().minusDays(1).format(formatCurrency));
        double rate =  currency.get("USD");
        tableMapper.InsertRate(ZonedDateTime.now().minusDays(1),rate);
    }
}
