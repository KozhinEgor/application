package com.application_tender.tender.subsidiaryModels;

public class Product {
    private Long id;
    private String vendor_code;
    private Long vendor_id;
    private Double frequency;
    private Boolean usb;
    private Boolean vxi;
    private Boolean portable;
    private String vendor;
    private Integer channel;
    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(Long vendor_id) {
        this.vendor_id = vendor_id;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
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

    public Boolean getUsb() {
        return usb;
    }

    public void setUsb(Boolean usb) {
        this.usb = usb;
    }

    public Boolean getVxi() {
        return vxi;
    }

    public void setVxi(Boolean vxi) {
        this.vxi = vxi;
    }

    public Boolean getPortable() {
        return portable;
    }

    public void setPortable(Boolean portable) {
        this.portable = portable;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getChanel() {
        return channel;
    }

    public void setChanel(Integer channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", vendor_code='" + vendor_code + '\'' +
                ", vendor_id=" + vendor_id +
                ", frequency=" + frequency +
                ", usb=" + usb +
                ", vxi=" + vxi +
                ", portable=" + portable +
                ", vendor='" + vendor + '\'' +
                ", channel=" + channel +
                '}';
    }
}
