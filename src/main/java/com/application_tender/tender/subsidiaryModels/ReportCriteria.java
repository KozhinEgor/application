package com.application_tender.tender.subsidiaryModels;

import java.time.ZonedDateTime;

public class ReportCriteria {
   private ReceivedJSON receivedJSON;
   private String interval;

   public ReceivedJSON getReceivedJSON() {
      return receivedJSON;
   }

   public void setReceivedJSON(ReceivedJSON receivedJSON) {
      this.receivedJSON = receivedJSON;
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
