package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.ProductCategory;
import com.application_tender.tender.models.Vendor;

import java.util.Arrays;

public class ProductReceived {
    private Product[] vendor_code;
    private ProductCategory[] category;
    private Vendor[] vendor;
    private String[] subcategory;
    private String category_product;

    public Product[] getVendor_code() {
        return vendor_code;
    }

    public void setVendor_code(Product[] vendor_code) {
        this.vendor_code = vendor_code;
    }

    public ProductCategory[] getCategory() {
        return category;
    }

    public void setCategory(ProductCategory[] category) {
        this.category = category;
    }

    public Vendor[] getVendor() {
        return vendor;
    }

    public void setVendor(Vendor[] vendor) {
        this.vendor = vendor;
    }

    public String[] getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String[] subcategory) {
        this.subcategory = subcategory;
    }

    public String getCategory_product() {
        return category_product;
    }

    public void setCategory_product(String category_product) {
        this.category_product = category_product;
    }

    @Override
    public String toString() {
        return "ProductReceived{" +
                "vendor_code=" + Arrays.toString(vendor_code) +
                ", category=" + Arrays.toString(category) +
                ", vendor=" + Arrays.toString(vendor) +
                ", subcategory=" + Arrays.toString(subcategory) +
                ", category_product='" + category_product + '\'' +
                '}';
    }
}
