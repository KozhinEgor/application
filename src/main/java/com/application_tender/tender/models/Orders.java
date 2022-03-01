package com.application_tender.tender.models;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

public class Orders {
    private Long id;
    private String comment_DB;
    private String comment;
    private Long tender;
    int number;
    private BigDecimal price;

    private String product_category;
    private Long product_category_DB;
    private Long product_DB;
    private String product;
    private String vendor;
    private Long vendor_DB;
    private String subcategory;
    private Long subcategory_DB;

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
    private Option option[];
    private String options;
    public Orders() {
    }

    public Orders(Long tender, String product_category, String vendor, String comment, int number, BigDecimal price) {
        this.tender = tender;
        this.product_category = product_category;

        this.vendor = vendor;
        this.comment = comment;
        this.number = number;
        this.price = price;
       ;
    }

    public Long getTender() {
        return tender;
    }

    public void setTender(Long tender) {
        this.tender = tender;
    }

    public String getProduct_category() {
        return product_category;
    }

    public void setProduct_category(String product_category) {
        this.product_category = product_category;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduct_category_DB() {
        return product_category_DB;
    }

    public void setProduct_category_DB(Long product_category_DB) {
        this.product_category_DB = product_category_DB;
    }

    public Long getProduct_DB() {
        return product_DB;
    }

    public void setProduct_DB(Long product_DB) {
        this.product_DB = product_DB;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getProduct() {
        return product;
    }

    public String getComment_DB() {
        return comment_DB;
    }

    public void setComment_DB(String comment_DB) {
        this.comment_DB = comment_DB;
    }

    public Long getVendor_DB() {
        return vendor_DB;
    }

    public void setVendor_DB(Long vendor_DB) {
        this.vendor_DB = vendor_DB;
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

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public Long getSubcategory_DB() {
        return subcategory_DB;
    }

    public void setSubcategory_DB(Long subcategory_DB) {
        this.subcategory_DB = subcategory_DB;
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

    public String ToDB(){
        if(product_category_DB == 7){
            return   this.product + ' ' +
                    this.comment + ' ' +
                    "- " + this.number;
        }
        else{
            return this.product_category + ' ' +
                    (this.subcategory_DB != null? this.subcategory+" ":"")+
                    (this.vendor_DB != 1 ? this.vendor + " " : "" )+
                    (!this.product.equals("Без артикуля")? this.product + " " : "" )+
                    (this.comment.length() != 0 ?"(" + this.comment + ") ": "") +
                    "- " + this.number;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orders orders = (Orders) o;
        return Objects.equals(id, orders.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", comment_DB='" + comment_DB + '\'' +
                ", comment='" + comment + '\'' +
                ", tender=" + tender +
                ", number=" + number +
                ", price=" + price +
                ", product_category='" + product_category + '\'' +
                ", product_category_DB=" + product_category_DB +
                ", product_DB=" + product_DB +
                ", product='" + product + '\'' +
                ", vendor='" + vendor + '\'' +
                ", vendor_DB=" + vendor_DB +
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
                ", subcategory_DB=" + subcategory_DB +
                ", option=" + Arrays.toString(option) +
                ", options='" + options + '\'' +
                '}';
    }

}
