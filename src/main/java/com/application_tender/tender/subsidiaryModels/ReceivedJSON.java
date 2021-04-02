package com.application_tender.tender.subsidiaryModels;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

//{dateStart: '', dateFinish: '', type: '%', custom: '%', winner: '%', minSum: 0, maxSum: 999999999999}
public class ReceivedJSON {
    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;
    private String type;
    private String custom;
    private String winner;
    private BigDecimal minSum;
    private BigDecimal maxSum;

    public ReceivedJSON() {
    }


    public ZonedDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(ZonedDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public ZonedDateTime getDateFinish() {
        return dateFinish;
    }

    public void setDateFinish(ZonedDateTime dateFinish) {
        this.dateFinish = dateFinish;
    }

    public String getType() {
        return type;
    }

    public String getCustom() {
        return custom;
    }

    public String getWinner() {
        return winner;
    }

    public BigDecimal getMinSum() {
        return minSum;
    }

    public BigDecimal getMaxSum() {
        return maxSum;
    }


}
