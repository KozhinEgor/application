package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Tender;

import java.math.BigDecimal;
import java.util.List;

public class Tenders {
    List<Tender> tenders;
    Long allCount;
    Long withPrice;
    BigDecimal sumWithPrice;
    Long withWinner;
    BigDecimal sumWithWinner;

    public Tenders(List<Tender> tenders, Long allCount, Long withPrice, BigDecimal sumWithPrice, Long withWinner, BigDecimal sumWithWinner) {
        this.tenders = tenders;
        this.allCount = allCount;
        this.withPrice = withPrice;
        this.sumWithPrice = sumWithPrice;
        this.withWinner = withWinner;
        this.sumWithWinner = sumWithWinner;
    }

    public List<Tender> getTenders() {
        return tenders;
    }

    public void setTenders(List<Tender> tenders) {
        this.tenders = tenders;
    }

    public Long getAllCount() {
        return allCount;
    }

    public void setAllCount(Long allCount) {
        this.allCount = allCount;
    }

    public Long getWithPrice() {
        return withPrice;
    }

    public void setWithPrice(Long withPrice) {
        this.withPrice = withPrice;
    }

    public BigDecimal getSumWithPrice() {
        return sumWithPrice;
    }

    public void setSumWithPrice(BigDecimal sumWithPrice) {
        this.sumWithPrice = sumWithPrice;
    }

    public Long getWithWinner() {
        return withWinner;
    }

    public void setWithWinner(Long withWinner) {
        this.withWinner = withWinner;
    }

    public BigDecimal getSumWithWinner() {
        return sumWithWinner;
    }

    public void setSumWithWinner(BigDecimal sumWithWinner) {
        this.sumWithWinner = sumWithWinner;
    }
}
