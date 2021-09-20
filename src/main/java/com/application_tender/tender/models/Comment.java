package com.application_tender.tender.models;

import java.time.ZonedDateTime;
import java.util.Arrays;

public class Comment {
    private Long id;
    private String text;
    private Long usr;
    private String user;
    private ZonedDateTime date;
    private Long tender;
    private Long[] users;

    public Comment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getUsr() {
        return usr;
    }

    public void setUsr(Long usr) {
        this.usr = usr;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public Long getTender() {
        return tender;
    }

    public void setTender(Long tender) {
        this.tender = tender;
    }

    public Long[] getUsers() {
        return users;
    }

    public void setUsers(Long[] users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", usr=" + usr +
                ", user='" + user + '\'' +
                ", date=" + date +
                ", tender=" + tender +
                ", users=" + Arrays.toString(users) +
                '}';
    }
}
