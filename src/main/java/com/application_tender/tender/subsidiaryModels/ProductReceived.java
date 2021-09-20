package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.ProductCategory;
import com.application_tender.tender.models.Vendor;

public class ProductReceived {
    private Product vendor_code;
    private ProductCategory category;
    private Vendor vendor;
    private BigCategory big_category;

    public Product getVendor_code() {
        return vendor_code;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public BigCategory getBig_category() {
        return big_category;
    }

    @Override
    public String toString() {
        return "ProductReceived{" +
                "vendor_code=" + vendor_code +
                ", category=" + category +
                ", vendor=" + vendor +
                ", big_category=" + big_category +
                '}';
    }
}
