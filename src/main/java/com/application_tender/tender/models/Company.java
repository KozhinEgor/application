package com.application_tender.tender.models;

import com.application_tender.tender.subsidiaryModels.Product;

public class Company {
    private Long id;

    private String inn;

    private String name;

    private String country;

    public Company() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", inn='" + inn + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if(obj != null && obj instanceof Company){
            eq = this.toString().equals(((Company) obj).toString());
        }
        return eq;
    }
}
