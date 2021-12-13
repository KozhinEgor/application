package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Company;
import com.application_tender.tender.models.TypeTender;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class SearchParametersFromDB {
    private Long id;
    private String nickname;
    private String name;


    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;
    private boolean dublicate;
    private boolean quarter;
    private boolean typeExclude;
    private String type;
    private boolean customExclude;
    private String custom;
    private String innCustomer;
    private Long country;
    private boolean winnerExclude;
    private String winner;
    private BigDecimal minSum;
    private BigDecimal maxSum;
    private String ids;
    private String bicotender;
    private boolean numberShow;
    private String product;
    private String region;
    private String district;

    public SearchParametersFromDB() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(ZonedDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public ZonedDateTime getDateFinish() {
        return dateFinish;
    }

    public void setDateFinish(ZonedDateTime dateFinish) {
        this.dateFinish = dateFinish;
    }

    public boolean isDublicate() {
        return dublicate;
    }

    public void setDublicate(boolean dublicate) {
        this.dublicate = dublicate;
    }

    public boolean isQuarter() {
        return quarter;
    }

    public void setQuarter(boolean quarter) {
        this.quarter = quarter;
    }

    public boolean isTypeExclude() {
        return typeExclude;
    }

    public void setTypeExclude(boolean typeExclude) {
        this.typeExclude = typeExclude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCustomExclude() {
        return customExclude;
    }

    public void setCustomExclude(boolean customExclude) {
        this.customExclude = customExclude;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getInnCustomer() {
        return innCustomer;
    }

    public void setInnCustomer(String innCustomer) {
        this.innCustomer = innCustomer;
    }

    public Long getCountry() {
        return country;
    }

    public void setCountry(Long country) {
        this.country = country;
    }

    public boolean isWinnerExclude() {
        return winnerExclude;
    }

    public void setWinnerExclude(boolean winnerExclude) {
        this.winnerExclude = winnerExclude;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public BigDecimal getMinSum() {
        return minSum;
    }

    public void setMinSum(BigDecimal minSum) {
        this.minSum = minSum;
    }

    public BigDecimal getMaxSum() {
        return maxSum;
    }

    public void setMaxSum(BigDecimal maxSum) {
        this.maxSum = maxSum;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getBicotender() {
        return bicotender;
    }

    public void setBicotender(String bicotender) {
        this.bicotender = bicotender;
    }

    public boolean isNumberShow() {
        return numberShow;
    }

    public void setNumberShow(boolean numberShow) {
        this.numberShow = numberShow;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
