package com.application_tender.tender.models;

public class TypeTender {

    private Long id;

    private String type;

    public TypeTender() {
    }

    public TypeTender(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
