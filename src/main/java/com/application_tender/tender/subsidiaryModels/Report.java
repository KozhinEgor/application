package com.application_tender.tender.subsidiaryModels;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Report {
    private List<Map<String,Object>> SumByTender;
    private List<Map<String,Object>> SumProduct;
    private List<String> columnProduct;
    private List<String> columnTender;
    public Report() {
    }

    public Report(List<Map<String, Object>> sumByTender, List<Map<String, Object>> sumProduct, List<String> columnProduct, List<String> columnTender) {
        SumByTender = sumByTender;
        this.SumProduct = sumProduct;
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

    public List<Map<String, Object>> getSumByTender() {
        return SumByTender;
    }

    public void setSumByTender(List<Map<String, Object>> sumByTender) {
        SumByTender = sumByTender;
    }

    public List<Map<String, Object>> getSumProduct() {
        return SumProduct;
    }

    public void setSumProduct(List<Map<String, Object>> sumProduct) {
        SumProduct = sumProduct;
    }
}
