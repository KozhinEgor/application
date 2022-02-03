package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.Company;
import com.application_tender.tender.models.District;
import com.application_tender.tender.models.Region;
import com.application_tender.tender.models.TypeTender;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

public class SearchParameters {
    private Long id;
    private String nickname;
    private String name;


    private ZonedDateTime dateStart;
    private ZonedDateTime dateFinish;
    private boolean dublicate;
    private boolean quarter;
    private boolean typeExclude;
    private List<TypeTender> type;
    private boolean customExclude;
    private List<Company> custom;
    private String innCustomer;
    private Long country;
    private boolean winnerExclude;
    private List<Company> winner;
    private BigDecimal minSum;
    private BigDecimal maxSum;
    private Long[] ids;
    private String ids_string;
    private Long[] bicotender;
    private String bicotender_string;
    private boolean numberShow;
    private List<ProductReceived> product;
    private List<Region> regions;
    private List<District> districts;
    private boolean plan_schedule;
    private boolean realized;
    private boolean adjacent_tender;
    private boolean private_search;
    public SearchParameters() {
    }

    public SearchParameters(Long id, String nickname, String name, ZonedDateTime dateStart, ZonedDateTime dateFinish, boolean dublicate, boolean quarter, boolean typeExclude, List<TypeTender> type, boolean customExclude, List<Company> custom, String innCustomer, Long country, boolean winnerExclude, List<Company> winner, BigDecimal minSum, BigDecimal maxSum, String ids_string, String bicotender_string, boolean numberShow, List<ProductReceived> product, List<Region> region, List<District> district,boolean plan_schedule, boolean realized, boolean adjacent_tender, boolean private_search) {
        this.id = id;
        this.nickname = nickname;
        this.name = name;
        this.dateStart = dateStart;
        this.dateFinish = dateFinish;
        this.dublicate = dublicate;
        this.quarter = quarter;
        this.typeExclude = typeExclude;
        this.type = type;
        this.customExclude = customExclude;
        this.custom = custom;
        this.innCustomer = innCustomer;
        this.country = country;
        this.winnerExclude = winnerExclude;
        this.winner = winner;
        this.minSum = minSum;
        this.maxSum = maxSum;
        this.ids_string = ids_string;
        this.bicotender_string = bicotender_string;
        this.numberShow = numberShow;
        this.product = product;
        this.regions = region;
        this.districts = district;
        this.plan_schedule = plan_schedule;
        this.realized = realized;
        this.adjacent_tender = adjacent_tender;
        this.private_search = private_search;
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

    public List<TypeTender> getType() {
        return type;
    }

    public void setType(List<TypeTender> type) {
        this.type = type;
    }

    public boolean isCustomExclude() {
        return customExclude;
    }

    public void setCustomExclude(boolean customExclude) {
        this.customExclude = customExclude;
    }

    public List<Company> getCustom() {
        return custom;
    }

    public void setCustom(List<Company> custom) {
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

    public List<Company> getWinner() {
        return winner;
    }

    public void setWinner(List<Company> winner) {
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

    public String getIds_string() {
        return ids_string;
    }

    public void setIds_string(String ids_string) {
        this.ids_string = ids_string;
    }

    public Long[] getBicotender() {
        return bicotender;
    }

    public void setBicotender(Long[] bicotender) {
        this.bicotender = bicotender;
    }

    public String getBicotender_string() {
        return bicotender_string;
    }

    public void setBicotender_string(String bicotender_string) {
        this.bicotender_string = bicotender_string;
    }

    public boolean isNumberShow() {
        return numberShow;
    }

    public void setNumberShow(boolean numberShow) {
        this.numberShow = numberShow;
    }

    public List<ProductReceived> getProduct() {
        return product;
    }

    public void setProduct(List<ProductReceived> product) {
        this.product = product;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegionList(List<Region> regions) {
        this.regions = regions;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistrictList(List<District> districts) {
        this.districts = districts;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public void setDistricts(List<District> districts) {
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

    public boolean isAdjacent_tender() {
        return adjacent_tender;
    }

    public void setAdjacent_tender(boolean adjacent_tender) {
        this.adjacent_tender = adjacent_tender;
    }

    public boolean isPrivate_search() {
        return private_search;
    }

    public void setPrivate_search(boolean private_search) {
        this.private_search = private_search;
    }

    @Override
    public String toString() {
        return "SearchParameters{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", name='" + name + '\'' +
                ", dateStart=" + dateStart +
                ", dateFinish=" + dateFinish +
                ", dublicate=" + dublicate +
                ", quarter=" + quarter +
                ", typeExclude=" + typeExclude +
                ", type=" + type +
                ", customExclude=" + customExclude +
                ", custom=" + custom +
                ", innCustomer='" + innCustomer + '\'' +
                ", country=" + country +
                ", winnerExclude=" + winnerExclude +
                ", winner=" + winner +
                ", minSum=" + minSum +
                ", maxSum=" + maxSum +
                ", ids=" + Arrays.toString(ids) +
                ", ids_string='" + ids_string + '\'' +
                ", bicotender=" + Arrays.toString(bicotender) +
                ", bicotender_string='" + bicotender_string + '\'' +
                ", numberShow=" + numberShow +
                ", product=" + product +
                ", regions=" + regions +
                ", districts=" + districts +
                ", plan_schedule=" + plan_schedule +
                ", realized=" + realized +
                ", adjacent_tender=" + adjacent_tender +
                ", private_search=" + private_search +
                '}';
    }
}
