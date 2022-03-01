package com.application_tender.tender.subsidiaryModels;

public class SearchTender {
    Long page;
    String sortName;
    String sortDirection;
    Long pageSize;
    SearchParameters searchParametrs;

    public SearchTender() {
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public SearchParameters getSearchParametrs() {
        return searchParametrs;
    }

    public void setSearchParametrs(SearchParameters searchParametrs) {
        this.searchParametrs = searchParametrs;
    }
}
