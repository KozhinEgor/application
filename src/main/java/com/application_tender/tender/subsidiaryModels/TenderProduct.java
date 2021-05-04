package com.application_tender.tender.subsidiaryModels;

import java.time.ZonedDateTime;

public class TenderProduct {
    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;
    private Long productCategory;
    private String product;
    public TenderProduct() {

    }

    public ZonedDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(ZonedDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public ZonedDateTime getDateFinish() {
        return dateFinish;
    }

    public void setDateFinish(ZonedDateTime dateFinish) {
        this.dateFinish = dateFinish;
    }

    public Long getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(Long productCategory) {
        this.productCategory = productCategory;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "TenderProduct{" +
                "dateStart=" + dateStart +
                ", dateFinish=" + dateFinish +
                ", productCategory=" + productCategory +
                ", product='" + product + '\'' +
                '}';
    }
}
