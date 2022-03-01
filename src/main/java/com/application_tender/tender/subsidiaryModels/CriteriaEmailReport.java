package com.application_tender.tender.subsidiaryModels;

import java.time.ZonedDateTime;

public class CriteriaEmailReport {
    ZonedDateTime date_start;
    ZonedDateTime date_finish;
    Integer id_step;

    public CriteriaEmailReport() {
    }

    public ZonedDateTime getDate_start() {
        return date_start;
    }

    public void setDate_start(ZonedDateTime date_start) {
        this.date_start = date_start;
    }

    public ZonedDateTime getDate_finish() {
        return date_finish;
    }

    public void setDate_finish(ZonedDateTime date_finish) {
        this.date_finish = date_finish;
    }

    public Integer getId_step() {
        return id_step;
    }

    public void setId_step(Integer id_step) {
        this.id_step = id_step;
    }

    @Override
    public String toString() {
        return "CriteriaEmailReport{" +
                "date_start=" + date_start +
                ", date_finish=" + date_finish +
                ", id_step=" + id_step +
                '}';
    }
}
