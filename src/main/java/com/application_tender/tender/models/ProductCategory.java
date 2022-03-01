package com.application_tender.tender.models;

public class ProductCategory {
    Long id;

    String category;

    String category_en;

    String category_product;

    private Boolean frequency;
    private Boolean usb;
    private Boolean vxi;
    private Boolean portable;
    private Boolean channel;
    private Boolean port;
    private Boolean form_factor;
    private Boolean purpose;
    private Boolean voltage;
    private Boolean current;
    private Boolean subcategory;


    public ProductCategory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory_en() {
        return category_en;
    }

    public void setCategory_en(String category_en) {
        this.category_en = category_en;
    }

    public String getCategory_product() {
        return category_product;
    }

    public void setCategory_product(String category_product) {
        this.category_product = category_product;
    }

    public Boolean getFrequency() {
        return frequency;
    }

    public void setFrequency(Boolean frequency) {
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

    public Boolean getChannel() {
        return channel;
    }

    public void setChannel(Boolean channel) {
        this.channel = channel;
    }

    public Boolean getPort() {
        return port;
    }

    public void setPort(Boolean port) {
        this.port = port;
    }

    public Boolean getForm_factor() {
        return form_factor;
    }

    public void setForm_factor(Boolean form_factor) {
        this.form_factor = form_factor;
    }

    public Boolean getPurpose() {
        return purpose;
    }

    public void setPurpose(Boolean purpose) {
        this.purpose = purpose;
    }

    public Boolean getVoltage() {
        return voltage;
    }

    public void setVoltage(Boolean voltage) {
        this.voltage = voltage;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }

    public Boolean getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Boolean subcategory) {
        this.subcategory = subcategory;
    }

    @Override
    public String toString() {
        return "ProductCategory{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", category_en='" + category_en + '\'' +
                ", category_product='" + category_product + '\'' +
                ", frequency=" + frequency +
                ", usb=" + usb +
                ", vxi=" + vxi +
                ", portable=" + portable +
                ", channel=" + channel +
                ", port=" + port +
                ", form_factor=" + form_factor +
                ", purpose=" + purpose +
                ", voltage=" + voltage +
                ", current=" + current +
                ", subcategory=" + subcategory +
                '}';
    }
}
