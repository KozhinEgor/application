package com.application_tender.tender.models;

import java.math.BigDecimal;
import java.util.Arrays;

public class OrdersDB {
    Long id;
    private Long tender;
    private Long product_category;
    Long id_product;
    String comment;
    int number;
    private BigDecimal price;
    private BigDecimal winprice;
    private Long vendor;
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

    public OrdersDB() {
    }

    public OrdersDB(Long id, Long tender, Long product_category, Long id_product, String comment, int number, BigDecimal price, BigDecimal winprice, Long vendor, Double frequency, Boolean usb, Boolean vxi, Boolean portable, Integer channel, Integer port, String form_factor, String purpose, Double voltage, Double current, String subcategory, Long subcategory_id, Option[] option, String options) {
        this.id = id;
        this.tender = tender;
        this.product_category = product_category;
        this.id_product = id_product;
        this.comment = comment;
        this.number = number;
        this.price = price;
        this.winprice = winprice;
        this.vendor = vendor;
        this.frequency = frequency;
        this.usb = usb;
        this.vxi = vxi;
        this.portable = portable;
        this.channel = channel;
        this.port = port;
        this.form_factor = form_factor;
        this.purpose = purpose;
        this.voltage = voltage;
        this.current = current;
        this.subcategory = subcategory;
        this.subcategory_id = subcategory_id;
        this.option = option;
        this.options = options;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getWinprice() {
        return winprice;
    }

    public void setWinprice(BigDecimal winprice) {
        this.winprice = winprice;
    }

    public Long getVendor() {
        return vendor;
    }

    public void setVendor(Long vendor) {
        this.vendor = vendor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTender() {
        return tender;
    }

    public void setTender(Long tender) {
        this.tender = tender;
    }

    public Long getProduct_category() {
        return product_category;
    }

    public void setProduct_category(Long product_category) {
        this.product_category = product_category;
    }

    public Long getId_product() {
        return id_product;
    }

    public void setId_product(Long id_product) {
        this.id_product = id_product;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
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

    @Override
    public String toString() {
        return "OrdersDB{" +
                "id=" + id +
                ", tender=" + tender +
                ", product_category=" + product_category +
                ", id_product=" + id_product +
                ", comment='" + comment + '\'' +
                ", number=" + number +
                ", price=" + price +
                ", winprice=" + winprice +
                ", vendor=" + vendor +
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
}
