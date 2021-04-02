package com.application_tender.tender.models;

public class Spectrum_analyzers {
    private Long id;

    private String vendor;


    private String vendor_code;

    private double frequency;

    private boolean portable ;

    private boolean usb;

    public Spectrum_analyzers() {
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

    public boolean isPortable() {
        return portable;
    }

    public void setPortable(boolean portable) {
        this.portable = portable;
    }

    public boolean isUsb() {
        return usb;
    }

    public void setUsb(boolean usb) {
        this.usb = usb;
    }
}
