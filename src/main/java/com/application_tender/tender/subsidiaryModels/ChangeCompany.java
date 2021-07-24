package com.application_tender.tender.subsidiaryModels;

public class ChangeCompany {
    private Long company;
    private Long newCompany;

    public ChangeCompany() {
    }

    public Long getCompany() {
        return company;
    }

    public Long getNewCompany() {
        return newCompany;
    }

    @Override
    public String toString() {
        return "ChangeCompany{" +
                "company=" + company +
                ", newCompany=" + newCompany +
                '}';
    }
}
