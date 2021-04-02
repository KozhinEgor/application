package com.application_tender.tender.models;

public class Oscilloscope {
    private Long id;

    
    private String vendor;
    
    private String vendor_code;
    
    private double frequency;
    
    private boolean usb;
    
    private boolean vxi;

    public Oscilloscope() {
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

    public String getVendor_code() {
        return vendor_code;
    }

    public void setVendor_code(String vendor_code) {
        this.vendor_code = vendor_code;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public boolean isUsb() {
        return usb;
    }

    public void setUsb(boolean usb) {
        this.usb = usb;
    }

    public boolean isVxi() {
        return vxi;
    }

    public void setVxi(boolean vxi) {
        this.vxi = vxi;
    }
}
