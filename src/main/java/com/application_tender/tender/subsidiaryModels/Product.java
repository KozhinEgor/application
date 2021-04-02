package com.application_tender.tender.subsidiaryModels;

public class Product {
    private Long id;
    private String vendor_code;
    private Double frequency;
    private Boolean usb;
    private Boolean vxi;
    private Boolean portable;
    private String vendor;

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendor_code() {
        return vendor_code;
    }

    public void setVendor_code(String vendor_code) {
        this.vendor_code = vendor_code;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public Boolean isUsb() {
        return usb;
    }

    public void setUsb(Boolean usb) {
        this.usb = usb;
    }

    public Boolean isVxi() {
        return vxi;
    }

    public void setVxi(Boolean vxi) {
        this.vxi = vxi;
    }

    public Boolean isPortable() {
        return portable;
    }

    public void setPortable(Boolean portable) {
        this.portable = portable;
    }

    public String getvendor() {
        return vendor;
    }

    public void setvendor(String vendor) {
        this.vendor = vendor;
    }
}
