package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Option;

import java.util.Arrays;

public class Product {
    private Long id;
    private String product_category;
    private Long product_category_id;
    private String vendor;
    private String vendor_code;
    private Long vendor_id;
    private Double frequency;
    private Boolean usb;
    private Boolean vxi;
    private Boolean portable;
    private Integer channel;
    private Integer port;
    private String form_factor;
    private String purpose;
    private Double voltage;
    private Double current;
    private String subcategory;
    private Long subcategory_id;
    private Option option[];
    private String options;


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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public Long getSubcategory_id() {
        return subcategory_id;
    }

    public void setSubcategory_id(Long subcategory_id) {
        this.subcategory_id = subcategory_id;
    }

    public String getForm_factor() {
        return form_factor;
    }

    public void setForm_factor(String form_factor) {
        this.form_factor = form_factor;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public Option[] getOption() {
        return option;
    }

    public void setOption(Option[] option) {
        this.option = option;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getProduct_category() {
        return product_category;
    }

    public void setProduct_category(String product_category) {
        this.product_category = product_category;
    }

    public Long getProduct_category_id() {
        return product_category_id;
    }

    public void setProduct_category_id(Long product_category_id) {
        this.product_category_id = product_category_id;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", product_category='" + product_category + '\'' +
                ", product_category_id=" + product_category_id +
                ", vendor='" + vendor + '\'' +
                ", vendor_code='" + vendor_code + '\'' +
                ", vendor_id=" + vendor_id +
                ", frequency=" + frequency +
                ", usb=" + usb +
                ", vxi=" + vxi +
                ", portable=" + portable +
                ", channel=" + channel +
                ", port=" + port +
                ", form_factor='" + form_factor + '\'' +
                ", purpose='" + purpose + '\'' +
                ", voltage=" + voltage +
                ", current=" + current +
                ", subcategory='" + subcategory + '\'' +
                ", subcategory_id=" + subcategory_id +
                ", option=" + Arrays.toString(option) +
                ", options='" + options + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if(obj != null && obj instanceof  Product){
            eq = this.toString().equals(((Product) obj).toString());
        }
        return eq;
    }
}
