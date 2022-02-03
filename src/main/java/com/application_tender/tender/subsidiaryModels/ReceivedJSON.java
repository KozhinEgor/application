package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Company;
import com.application_tender.tender.models.District;
import com.application_tender.tender.models.Region;
import com.application_tender.tender.models.TypeTender;

import java.io.DataInputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

//{dateStart: '', dateFinish: '', type: '%', custom: '%', winner: '%', minSum: 0, maxSum: 999999999999}
public class ReceivedJSON {
    private final DateTimeFormatter format_date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");

    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;
    private boolean dublicate;
    private boolean quarter;
    private boolean typeExclude;
    private TypeTender[] type;
    private boolean customExclude;
    private Company[] custom;
    private String innCustomer;
    private Long country;
    private boolean winnerExclude;
    private Company[] winner;
    private BigDecimal minSum;
    private BigDecimal maxSum;
    private Long[] ids;
    private Long[] bicotender;
    private boolean numberShow;
    private ProductReceived[] product;
    private Region[] regions;
    private District[] districts;
    private boolean plan_schedule;
    private boolean realized;
    public ReceivedJSON() {
    }

    public DateTimeFormatter getFormat_date() {
        return format_date;
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

    public TypeTender[] getType() {
        return type;
    }

    public void setType(TypeTender[] type) {
        this.type = type;
    }

    public boolean isCustomExclude() {
        return customExclude;
    }

    public void setCustomExclude(boolean customExclude) {
        this.customExclude = customExclude;
    }

    public Company[] getCustom() {
        return custom;
    }

    public void setCustom(Company[] custom) {
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

    public Company[] getWinner() {
        return winner;
    }

    public void setWinner(Company[] winner) {
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

    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }

    public Long[] getBicotender() {
        return bicotender;
    }

    public void setBicotender(Long[] bicotender) {
        this.bicotender = bicotender;
    }

    public boolean isNumberShow() {
        return numberShow;
    }

    public void setNumberShow(boolean numberShow) {
        this.numberShow = numberShow;
    }

    public ProductReceived[] getProduct() {
        return product;
    }

    public void setProduct(ProductReceived[] product) {
        this.product = product;
    }

    public Region[] getRegions() {
        return regions;
    }

    public void setRegions(Region[] regions) {
        this.regions = regions;
    }

    public District[] getDistricts() {
        return districts;
    }

    public void setDistricts(District[] districts) {
        this.districts = districts;
    }

    public boolean isPlan_schedule() {
        return plan_schedule;
    }

    public void setPlan_schedule(boolean plan_schedule) {
        this.plan_schedule = plan_schedule;
    }

    public boolean isRealized() {
        return realized;
    }

    public void setRealized(boolean realized) {
        this.realized = realized;
    }

    @Override
    public String toString() {
        return "ReceivedJSON{" +
                "format_date=" + format_date +
                ", dateStart=" + dateStart +
                ", dateFinish=" + dateFinish +
                ", dublicate=" + dublicate +
                ", quarter=" + quarter +
                ", typeExclude=" + typeExclude +
                ", type=" + Arrays.toString(type) +
                ", customExclude=" + customExclude +
                ", custom=" + Arrays.toString(custom) +
                ", innCustomer='" + innCustomer + '\'' +
                ", country=" + country +
                ", winnerExclude=" + winnerExclude +
                ", winner=" + Arrays.toString(winner) +
                ", minSum=" + minSum +
                ", maxSum=" + maxSum +
                ", ids=" + Arrays.toString(ids) +
                ", bicotender=" + Arrays.toString(bicotender) +
                ", numberShow=" + numberShow +
                ", product=" + Arrays.toString(product) +
                ", regions=" + Arrays.toString(regions) +
                ", districts=" + Arrays.toString(districts) +
                ", plan_schedule=" + plan_schedule +
                ", realized=" + realized +
                '}';
    }
}
