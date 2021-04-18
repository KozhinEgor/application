package com.application_tender.tender.models;

import java.math.BigDecimal;

public class Orders {

    private Long tender;
    private String product_category;
    private String id_product;
    private String vendor;
    private String comment;
    int number;
    private BigDecimal price;
    private BigDecimal winprice;

    public Orders() {
    }

    public Orders(Long tender, String product_category, String id_product, String vendor, String comment, int number, BigDecimal price, BigDecimal winPrice) {
        this.tender = tender;
        this.product_category = product_category;
        this.id_product = id_product;
        this.vendor = vendor;
        this.comment = comment;
        this.number = number;
        this.price = price;
        this.winprice = winPrice;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Long getTender() {
        return tender;
    }

    public void setTender(Long tender) {
        this.tender = tender;
    }

    public String getproduct_category() {
        return product_category;
    }

    public void setproduct_category(String product_category) {
        this.product_category = product_category;
    }

    public String getId_product() {
        return id_product;
    }

    public void setId_product(String id_product) {
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

    public String ToDB(){
        if(product_category.length() == 0){
            return   id_product + ' ' +
                    comment + ' ' +
                    "- " + number;
        }
        else{
            return product_category + ' ' +
                    (vendor.length() != 0 ? vendor + " " : "" )+
                    (id_product.length() != 0? id_product + " " : "" )+
                    (comment.length() != 0 ?"(" + comment + ") ": "") +
                    "- " + number;
        }
    }
    @Override
    public String toString() {

        return "Orders{" +
                "tender=" + tender +
                ", product_category='" + product_category + '\'' +
                ", id_product='" + id_product + '\'' +
                ", vendor='" + vendor + '\'' +
                ", comment='" + comment + '\'' +
                ", number=" + number +
                ", price=" + price +
                ", winprice=" + winprice +
                '}';
    }
}
