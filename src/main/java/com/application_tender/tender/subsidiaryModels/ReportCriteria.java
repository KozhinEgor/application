package com.application_tender.tender.subsidiaryModels;

import java.time.ZonedDateTime;

public class ReportCriteria {
   private SearchParameters searchParameters;
   private String interval;

   public SearchParameters getSearchParameters() {
      return searchParameters;
   }

   public void setSearchParameters(SearchParameters searchParameters) {
      this.searchParameters = searchParameters;
   }

   public String getInterval() {
      return interval;
   }

   public void setInterval(String interval) {
      this.interval = interval;
   }

   public ReportCriteria() {
   }
}
