package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Orders;
import com.application_tender.tender.models.OrdersDB;

import java.util.List;

public class OrdersReceived {
    List<Orders> orders;
    List<OrdersDB> ordersDB;

    public OrdersReceived() {
    }

    public OrdersReceived(List<Orders> orders, List<OrdersDB> ordersDB) {
        this.orders = orders;
        this.ordersDB = ordersDB;
    }

    public List<Orders> getOrders() {
        return orders;
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders;
    }

    public List<OrdersDB> getOrdersDB() {
        return ordersDB;
    }

    public void setOrdersDB(List<OrdersDB> ordersDB) {
        this.ordersDB = ordersDB;
    }
}
