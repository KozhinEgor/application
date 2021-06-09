package com.application_tender.tender.subsidiaryModels;

import java.time.ZonedDateTime;

public class ReportCriteria {
    private Long category;
    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;

    public ReportCriteria() {
    }

    public Long getCategory() {
        return category;
    }

    public ZonedDateTime getDateStart() {
        return dateStart;
    }

    public ZonedDateTime getDateFinish() {
        return dateFinish;
    }
}
