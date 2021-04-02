package com.application_tender.tender.models;

public class PulseGenerator {
    Long id;

    private String vendor;


    String vendor_code;

    double frequency;

    public PulseGenerator() {
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
