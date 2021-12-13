package com.application_tender.tender.subsidiaryModels;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class Report {
    private List<Map<String,String>> SumByTender;
    private List<Map<String,String>> SumProduct;
    private List<String> columnProduct;
    private List<String> columnTender;
    public Report() {
    }

    public Report(List<Map<String, String>> sumByTender, List<Map<String, String>> sumProduct, List<String> columnProduct, List<String> columnTender) {
        SumByTender = sumByTender;
        SumProduct = sumProduct;
        this.columnProduct = columnProduct;
        this.columnTender = columnTender;
    }

    public List<String> getColumnProduct() {
        return columnProduct;
    }

    public void setColumnProduct(List<String> columnProduct) {
        this.columnProduct = columnProduct;
    }

    public List<String> getColumnTender() {
        return columnTender;
    }

    public void setColumnTender(List<String> columnTender) {
        this.columnTender = columnTender;
    }

    public List<Map<String, String>> getSumByTender() {
        return SumByTender;
    }

    public void setSumByTender(List<Map<String, String>> sumByTender) {
        SumByTender = sumByTender;
    }

    public List<Map<String, String>> getSumProduct() {
        return SumProduct;
    }

    public void setSumProduct(List<Map<String, String>> sumProduct) {
        SumProduct = sumProduct;
    }
}
