package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Customer;
import com.application_tender.tender.models.TypeTender;
import com.application_tender.tender.models.Winner;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;

//{dateStart: '', dateFinish: '', type: '%', custom: '%', winner: '%', minSum: 0, maxSum: 999999999999}
public class ReceivedJSON {
    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;
    private boolean dublicate;
    private boolean typeExclude;
    private TypeTender[] type;
    private boolean customExclude;
    private Customer[] custom;
    private String innCustomer;
    private Long country;
    private boolean winnerExclude;
    private Winner[] winner;
    private BigDecimal minSum;
    private BigDecimal maxSum;
    private Long[] ids;
    private Long[] bicotender;
    private boolean numberShow;
    private ProductReceived[] product;
    public ReceivedJSON() {
    }

    public ZonedDateTime getDateStart() {
        return dateStart;
    }

    public ZonedDateTime getDateFinish() {
        return dateFinish;
    }

    public boolean isDublicate() {
        return dublicate;
    }

    public boolean isTypeExclude() {
        return typeExclude;
    }

    public TypeTender[] getType() {
        return type;
    }

    public boolean isCustomExclude() {
        return customExclude;
    }

    public Customer[] getCustom() {
        return custom;
    }

    public String getInnCustomer() {
        return innCustomer;
    }

    public Long getCountry() {
        return country;
    }

    public boolean isWinnerExclude() {
        return winnerExclude;
    }

    public Winner[] getWinner() {
        return winner;
    }

    public BigDecimal getMinSum() {
        return minSum;
    }

    public BigDecimal getMaxSum() {
        return maxSum;
    }

    public Long[] getIds() {
        return ids;
    }

    public Long[] getBicotender() {
        return bicotender;
    }

    public boolean isNumberShow() {
        return numberShow;
    }

    public ProductReceived[] getProduct() {
        return product;
    }

    @Override
    public String toString() {
        return "ReceivedJSON{" +
                "dateStart=" + dateStart +
                ", dateFinish=" + dateFinish +
                ", dublicate=" + dublicate +
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
                ", productReceiveds=" + Arrays.toString(product) +
                '}';
    }
}
