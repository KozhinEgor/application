package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.ProductCategory;

import java.util.ArrayList;
import java.util.List;

public class BigCategory {

    private Long big_category_id;
    private  String big_category;
    private ArrayList<ProductCategory> category = new ArrayList<>();
    private String productCategory;



    public BigCategory() {
    }
    public Long getBig_category_id() {
        return big_category_id;
    }

    public void setBig_category_id(Long big_category_id) {
        this.big_category_id = big_category_id;
    }

    public String getBig_category() {
        return big_category;
    }

    public void setBig_category(String big_category) {
        this.big_category = big_category;
    }
    public ArrayList<ProductCategory> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<ProductCategory> category) {
        this.category = category;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }



    public void addCategory(ProductCategory category){
        this.productCategory = (this.productCategory == null?"":this.productCategory) + category.getCategory() + "; ";
        this.category.add(category);
    }
}
