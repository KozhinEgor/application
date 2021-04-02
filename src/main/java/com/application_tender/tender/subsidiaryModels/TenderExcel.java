package com.application_tender.tender.subsidiaryModels;

import org.springframework.web.multipart.MultipartFile;

public class TenderExcel {
    private String name;
    private String url;
    private MultipartFile data;

    public TenderExcel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MultipartFile getData() {
        return data;
    }

    public void setData(MultipartFile data) {
        this.data = data;
    }
}
