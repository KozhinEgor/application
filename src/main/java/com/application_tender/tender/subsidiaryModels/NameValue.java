package com.application_tender.tender.subsidiaryModels;

public class NameValue {
    private String name;
    private Integer value;

    public NameValue() {
    }

    public NameValue(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
