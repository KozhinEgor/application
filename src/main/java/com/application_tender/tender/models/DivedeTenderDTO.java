package com.application_tender.tender.models;

import java.util.List;


public class DivedeTenderDTO {
    Tender tender;
    List<Orders> orders;

    public DivedeTenderDTO() {
    }

    public Tender getTender() {
        return tender;
    }

    public void setTender(Tender tender) {
        this.tender = tender;
    }

    public List<Orders> getOrders() {
        return orders;
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders;
    }
}
