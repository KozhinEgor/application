package com.application_tender.tender.models;

import java.math.BigDecimal;

public class OrdersDB {
    Long id;
    private Long tender;
    private Long product_category;
    Long id_product;
    String comment ;
    int number;
    private BigDecimal price;
    private BigDecimal winprice;
    private Long vendor;
    public OrdersDB() {
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
                '}';
    }
}
