package com.application_tender.tender.subsidiaryModels;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ReportVendorQuarter {
    private Map<String,Integer> quarter = new HashMap<String,Integer>();
    private String vendor;

    public ReportVendorQuarter(String vendor) {
        this.vendor = vendor;
    }

    public ReportVendorQuarter() {
    }

    public Map<String, Integer> getQuarter() {
        return quarter;
    }

    public void setQuarter(Map<String, Integer> quarter) {
        this.quarter = quarter;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
