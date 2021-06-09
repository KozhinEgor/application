package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.ProductCategory;
import com.application_tender.tender.models.Vendor;

public class ProductReceived {
    private Product vendor_code;
    private ProductCategory category;
    private Vendor vendor;

    public Product getVendor_code() {
        return vendor_code;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public Vendor getVendor() {
        return vendor;
    }
}
