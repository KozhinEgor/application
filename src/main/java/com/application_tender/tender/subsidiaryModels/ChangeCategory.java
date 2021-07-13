package com.application_tender.tender.subsidiaryModels;

public class ChangeCategory {
    private Long category;
    private Long vendor_code;
    private Long newCategory;
    private Long newVendor_code;

    public Long getCategory() {
        return category;
    }

    public Long getVendor_code() {
        return vendor_code;
    }

    public Long getNewCategory() {
        return newCategory;
    }

    public Long getNewVendor_code() {
        return newVendor_code;
    }

    @Override
    public String toString() {
        return "ChangeCategory{" +
                "category=" + category +
                ", vendor_code=" + vendor_code +
                ", newCategory=" + newCategory +
                ", newVendor_code=" + newVendor_code +
                '}';
    }
}
