package com.application_tender.tender.subsidiaryModels;

public class deleteOrder {
    Long id ;
    Boolean result ;
    Long tender;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Long getTender() {
        return tender;
    }

    public void setTender(Long tender) {
        this.tender = tender;
    }

    @Override
    public String toString() {
        return "deleteOrder{" +
                "id=" + id +
                ", result=" + result +
                ", tender=" + tender +
                '}';
    }
}
