package com.application_tender.tender.subsidiaryModels;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ReportQuarter {

    private int count;
    private BigDecimal sum;
    private int quarter;
    private int year;


    public ReportQuarter() {
    }
    public ReportQuarter(int quarter, int year, String product) {
        this.quarter = quarter;
        this.year = year;
        count = 0;
        sum = new BigDecimal(0);

    }

    public ReportQuarter(String product, int count, BigDecimal sum, int quarter, int year) {

        this.count = count;
        this.sum = sum;
        this.quarter = quarter;
        this.year = year;
    }



    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


    public void addCount(){
        this.count = this.count +1;
    }
    public void addSum(BigDecimal sum){
        this.sum = this.sum.add(sum);
    }

    @Override
    public String toString() {
        return "ReportQuarter{" +
                "count=" + count +
                ", sum=" + sum +
                ", quarter=" + quarter +
                ", year=" + year +
                '}';
    }
}
