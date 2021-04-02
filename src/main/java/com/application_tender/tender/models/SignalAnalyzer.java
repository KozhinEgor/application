package com.application_tender.tender.models;

public class SignalAnalyzer {
    private Long id;

    private String vendor ;

    private String vendor_code;

    private double frequency;

    public SignalAnalyzer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getvendor_code() {
        return vendor_code;
    }

    public void setvendor_code(String vendor_code) {
        this.vendor_code = vendor_code;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
}
