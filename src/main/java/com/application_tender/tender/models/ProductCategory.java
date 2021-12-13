package com.application_tender.tender.models;

public class ProductCategory {
    Long id;

    String category;

    String category_en;

    String category_product;

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

    @Override
    public String toString() {
        return "ProductCategory{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", category_en='" + category_en + '\'' +
                ", category_product='" + category_product + '\'' +
                '}';
    }
}
