package com.application_tender.tender.service;

import com.application_tender.tender.controller.SearchAtribut;
import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.Tender;
import com.application_tender.tender.subsidiaryModels.ReceivedJSON;
import com.application_tender.tender.subsidiaryModels.ReportQuarter;
import com.application_tender.tender.subsidiaryModels.ReportVendorQuarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.*;

@Service
public class ReportService {
    private final ArrayList<Integer[]> quarterEconomic = new ArrayList<>();
    private ArrayList<Integer[]> quarterYear = new ArrayList<>();
    private  final TableMapper tableMapper;
    @Autowired
    private SearchAtribut searchAtribut;

    public ReportService(TableMapper tableMapper, FileService fileService) {
        this.tableMapper = tableMapper;

        quarterEconomic.add(new Integer[]{11, 12, 1});
        quarterEconomic.add(new Integer[]{2, 3, 4});
        quarterEconomic.add(new Integer[]{5, 6, 7});
        quarterEconomic.add(new Integer[]{8, 9, 10});

        quarterYear.add(new Integer[]{1,2,3});
        quarterYear.add(new Integer[]{4,5,6});
        quarterYear.add(new Integer[]{7,8,9});
        quarterYear.add(new Integer[]{10,11,12});
    }

    public ArrayList<ReportQuarter> getQuartalTenderReport ( Long category,  ReceivedJSON json) {
        ArrayList<ReportQuarter> reportQuarters = new ArrayList<>();
        Map<String,String> tender = this.tenders(json);
        if(tender != null) {

            String tenders = tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));

            while (y != year || q != quartal + 1) {

              reportQuarters.add(0, tableMapper.findForOrders((flag?y+1:y), q, this.dataRangeString(flag, json.isQuarter(), q,y), category, tenders));


                Map<String,Integer> n = this.countQuartal(y,q,flag);
                flag = false;
                y=n.get("y");
                q = n.get("q");
            }
            return reportQuarters;
        }
        else{
            return reportQuarters;
        }
    }
    private Map<String,String> tenders (ReceivedJSON json){
        String tenders;
        int year = ZonedDateTime.now().getYear();
        int quartal = ZonedDateTime.now().get(IsoFields.QUARTER_OF_YEAR);
        int y = 2018;
        int q = 1;
        boolean flag = false;
        boolean flagFinish = false;


            List<Tender> tender = searchAtribut.findTenderByTerms(json);
            if(json.getDateFinish() != null){
                year = json.getDateFinish().getYear();
                if (!json.isQuarter()){
                    quartal = json.getDateFinish().get(IsoFields.QUARTER_OF_YEAR);
                }
                else {
                    if(json.getDateFinish().getMonth().getValue() == 11 || json.getDateFinish().getMonth().getValue() == 12){
                        year = year+1;
                    }
                    for(int index = 1;index<=4;index++){
                        if(Arrays.asList(quarterEconomic.get(index-1)).contains(json.getDateFinish().getMonth().getValue())){
                            quartal = index;
                            break;
                        }
                    }
                }
            }
            if(json.getDateStart() != null){
                y = json.getDateStart().getYear();
                if (!json.isQuarter()){
                    q = json.getDateStart().get(IsoFields.QUARTER_OF_YEAR);
                }
                else {
                    if(json.getDateStart().getMonth().getValue() == 11 || json.getDateStart().getMonth().getValue() == 12){
                        y = y+1;
                    }
                    for(int index = 1;index<=4;index++){
                        if(Arrays.asList(quarterEconomic.get(index-1)).contains(json.getDateStart().getMonth().getValue())){
                            if(json.getDateStart().getMonth().getValue() == 1){
                                flag = true;
                                y=y-1;
                            }

                            q = index;
                            break;
                        }
                    }
                }
            }
            if(tender.size() != 0){
                tenders = " and orders.tender in (";
                for (Tender t:tender){
                    tenders = tenders + t.getId() + ",";
                }
                tenders = tenders.substring(0,tenders.length()-1) + ")";
            }
            else{
                return  null;
            }

        HashMap <String,String> answear = new HashMap<>();
        answear.put("year",Integer.toString(year));
        answear.put("y",Integer.toString(y));
        answear.put("quartal",Integer.toString(quartal));
        answear.put("q",Integer.toString(q));
        answear.put("tenders", tenders);
        answear.put("flag",Boolean.toString(flag));
        return answear;
    }
    private String dataRangeString(boolean flag, boolean quarter, int q, int y){
        String dataRange;
        if (quarter) {
            if (q == 1) {
                if (flag) {
                    dataRange = " (year(date_start) ='" + (y+1) + "' and month(date_start) = 1)";
                }
                else {
                    dataRange = "(" +
                            "(" +
                            "year(date_start) ='" + (y-1) + "' and month(date_start) in(11,12)" +
                            " ) " +
                            "or (" +
                            "year(date_start) = " + y  + " and month(date_start)  = 1))";
                }
            } else {
                dataRange = "(year(date_start) ='" + y + "' and month(date_start) in(" + Arrays.toString(quarterEconomic.get(q - 1)).substring(1, Arrays.toString(quarterEconomic.get(q - 1)).length() - 1) + "))";
            }

            return dataRange;
        } else {
            dataRange = "(year(date_start) ='" + y + "' and month(date_start) in(" + Arrays.toString(quarterYear.get(q - 1)).substring(1, Arrays.toString(quarterYear.get(q - 1)).length() - 1) + "))";

            return dataRange;
        }
    }
    private Map<String,Integer> countQuartal(int y,int q, boolean flag){
        if (q == 4) {
            q = 1;
            y = y + 1;
        } else {
            q = q + 1;
        }
        if (flag) {
            y = y + 1;
        }
        HashMap<String,Integer> a = new HashMap<>();
        a.put("q",q);
        a.put("y",y);
        return a;
    }

    public ArrayList<ReportVendorQuarter>getQuartalVendorReport (Long category,  ReceivedJSON json){
        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        Map<String,String> tender = this.tenders(json);
        if(tender != null){
            String tenders = tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));


            String category_en = tableMapper.findOneCategoryENById(category);

            while (y != year || q != quartal+1) {
                Map<String, Integer> vendorCount = new HashMap<String, Integer>();
                List<String> vendors = new LinkedList<>();

                vendors = tableMapper.findVendorForOrders(this.dataRangeString(flag, json.isQuarter(), q,y), category, category_en, tenders);

                for (String vendor : vendors) {
                    if (vendor.equals("No vendor")) {
                    } else if (!vendorCount.containsKey(vendor)) {
                        vendorCount.put(vendor, 1);
                    } else {
                        vendorCount.put(vendor, vendorCount.get(vendor) + 1);
                    }
                }
                if (reportVendorQuarters.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : vendorCount.entrySet()) {
                        ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                        reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                        reportVendorQuarters.add(reportVendorQuarter);

                    }
                }
                else {

                    for (Map.Entry<String, Integer> entry : vendorCount.entrySet()) {
                        boolean flag2 = false;
                        for (ReportVendorQuarter reportVendorQuarter : reportVendorQuarters) {
                            if (reportVendorQuarter.getVendor().equals(entry.getKey())) {
                                reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                                flag2 = true;
                            }

                        }
                        if (!flag2) {
                            ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                            reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                            reportVendorQuarters.add(reportVendorQuarter);
                        }
                    }
                }

                Map<String,Integer> n = this.countQuartal(y,q,flag);
                flag = false;
                y=n.get("y");
                q = n.get("q");

            }
            return reportVendorQuarters;
        }
        else{
            return  reportVendorQuarters;
        }


    }

    public ArrayList<ReportVendorQuarter> getQuartalNoVendorReport ( Long category, ReceivedJSON json) {
        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        Map<String, String> tender = this.tenders(json);
        System.out.println(tender);
        if (tender != null) {
            String tenders = tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));


            String category_en = tableMapper.findOneCategoryENById(category);

            while (y != year || q != quartal + 1) {
                Map<String, Integer> vendorCount = new HashMap<String, Integer>();

                List<String> vendors = tableMapper.findNoVendorForOrders(this.dataRangeString(flag, json.isQuarter(), q, y), category, category_en, tenders);

                for (String vendor : vendors) {
                    if (!vendorCount.containsKey(vendor)) {
                        vendorCount.put(vendor, 1);
                    } else {
                        vendorCount.put(vendor, vendorCount.get(vendor) + 1);
                    }
                }
                if (reportVendorQuarters.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : vendorCount.entrySet()) {
                        ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                        reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                        reportVendorQuarters.add(reportVendorQuarter);

                    }
                } else {

                    for (Map.Entry<String, Integer> entry : vendorCount.entrySet()) {
                        boolean flag2 = false;
                        for (ReportVendorQuarter reportVendorQuarter : reportVendorQuarters) {
                            if (reportVendorQuarter.getVendor().equals(entry.getKey())) {
                                reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                                flag2 = true;
                            }

                        }
                        if (!flag2) {
                            ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                            reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                            reportVendorQuarters.add(reportVendorQuarter);
                        }
                    }
                }

                Map<String, Integer> n = this.countQuartal(y, q, flag);
                flag = false;
                y = n.get("y");
                q = n.get("q");
            }

            return reportVendorQuarters;
        }
        else {
            return reportVendorQuarters;
        }
    }
}
