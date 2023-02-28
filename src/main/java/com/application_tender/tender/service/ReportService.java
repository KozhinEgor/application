package com.application_tender.tender.service;

import com.application_tender.tender.controller.SearchAtribut;
import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.Tender;
import com.application_tender.tender.subsidiaryModels.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;

@Service
public class ReportService {
    private final DateTimeFormatter format_date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
    private final ArrayList<Integer[]> quarterEconomic = new ArrayList<>();
    private ArrayList<Integer[]> quarterYear = new ArrayList<>();
    private  final TableMapper tableMapper;
    @Autowired
    private SearchAtribut searchAtribut;
    private String tenders;

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

    public ArrayList<ReportQuarter> getQuartalTenderReport ( Long category,  SearchParameters json) {
        ArrayList<ReportQuarter> reportQuarters = new ArrayList<>();
        Map<String,String> tender = this.tenders(json);
        if(tender != null) {

            String tenders =" and orders.tender in "+ tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));

            while (y != year || q != quartal ) {

              reportQuarters.add(0, tableMapper.findForOrders((flag?y+1:y), q, this.dataRangeString(flag, json.isQuarter(), q,y), "product_category = "+category, tenders));


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


    private Map<String,String> tenders (SearchParameters json){
        String tenders;
        int year = ZonedDateTime.now().getYear();
        int quartal = ZonedDateTime.now().get(IsoFields.QUARTER_OF_YEAR);
        int y = 2018;
        int q = 1;
        boolean flag = false;
        boolean flagFinish = false;


            List<Tender> tender = tableMapper.findAllTenderTerms(searchAtribut.findTenderByTerms(json));
            System.out.println(tender.size());
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
                tenders = "(";
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

    public ArrayList<ReportVendorQuarter>getQuartalVendorReport (Long category,  SearchParameters json){
        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        Map<String,String> tender = this.tenders(json);

        if(tender != null){
            String tenders =" and orders.tender in "+ tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));


            String category_en = tableMapper.findOneCategoryENById(category);

            while (y != year || q != quartal) {
                Map<String, Integer> vendorCount = new HashMap<String, Integer>();
                List<String> vendors = new LinkedList<>();

                String range = this.dataRangeString(flag, json.isQuarter(), q,y);

                vendors = tableMapper.findVendorForOrders(range,"product_category = "+category, category_en, tenders);


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



    public ArrayList<ReportVendorQuarter> getQuartalNoVendorReport ( Long category, SearchParameters json) {
        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        Map<String, String> tender = this.tenders(json);

        if (tender != null) {
            String tenders =" and orders.tender in "+ tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));


            String category_en = tableMapper.findOneCategoryENById(category);

            while (y != year || q != quartal) {
                Map<String, Integer> vendorCount = new HashMap<String, Integer>();

                List<String> vendors = tableMapper.findNoVendorForOrders(this.dataRangeString(flag, json.isQuarter(), q, y), "product_category = "+category, category_en, tenders);

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



    public ArrayList<ReportVendorQuarter>getQuartalCustomerReport ( Long company,SearchParameters json){
        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        Map<String,String> tender = this.tenders(json);
        if(tender != null){
            String tenders =" and tender.id in "+ tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));


            while (y != year || q != quartal) {
                Map<String, Integer> customerCount = new HashMap<>();
                List<ReportCompany> companies = new LinkedList<>();
                if(company == 0){
                     companies = tableMapper.CustomerForOrders(this.dataRangeString(flag, json.isQuarter(), q,y),tenders);
                }
                else{
                    companies = tableMapper.WinnerForOrders(this.dataRangeString(flag, json.isQuarter(), q,y),tenders);
                }
                for(ReportCompany a:companies ){
                    if(a.getName() != null) {
                        customerCount.put(a.getName(), a.getCount());
                    }
                }

                if (reportVendorQuarters.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : customerCount.entrySet()) {

                        ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                        reportVendorQuarter.getQuarter().put(String.valueOf((flag?y+1:y)) + ' ' + String.valueOf(q), entry.getValue());
                        reportVendorQuarters.add(reportVendorQuarter);

                    }
                }
                else {

                    for (Map.Entry<String, Integer> entry : customerCount.entrySet()) {
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

    public List<String> getQuartal (  SearchParameters json) {
        List<String> Quartal = new LinkedList<>();
        Map<String,String> tender = this.tenders(json);
        if(tender != null) {

            String tenders =" and orders.tender in "+ tender.get("tenders");
            int year = Integer.parseInt(tender.get("year"));
            int quartal = Integer.parseInt(tender.get("quartal"));
            int y = Integer.parseInt(tender.get("y"));
            int q = Integer.parseInt(tender.get("q"));
            boolean flag = Boolean.parseBoolean(tender.get("flag"));

            while (y != year || q != quartal) {
                Quartal.add(y+" "+q);

                Map<String,Integer> n = this.countQuartal(y,q,flag);
                flag = false;
                y=n.get("y");
                q = n.get("q");
            }
            return Quartal;
        }
        else{
            return Quartal;
        }
    }
    public Object[] selectForReportCompany( Long company, ReportCriteria reportCriteria){
         /*
    Select
    w.name,
    count(tender.id) as 'Количество тендеров',
    sum(tender.win_sum) as 'Сумма тендеров',
    count(case when year(tender.date_start)=2018 then tender.id end) as 'Количество 2018',
    sum(case when year(tender.date_start)=2018 then tender.win_sum end) as 'Сумма 2018',
    count(case when year(tender.date_start)=2019 then tender.id end) as 'Количество 2019',
    sum(case when year(tender.date_start)=2019 then tender.win_sum end) as 'Сумма 2019',
    count(case when year(tender.date_start)=2020 then tender.id end) as 'Количество 2020',
    sum(case when year(tender.date_start)=2020 then tender.win_sum end) as 'Сумма 2020',
    count(case when year(tender.date_start)=2021 then tender.id end) as 'Количество 2021',
    sum(case when year(tender.date_start)=2021 then tender.win_sum end) as 'Сумма 2021',
    count(case when year(tender.date_start)=2022 then tender.id end) as 'Количество 2022',
    sum(case when year(tender.date_start)=2022 then tender.win_sum end) as 'Сумма 2022'
from tender
left join winner w on tender.winner = w.id
    where  tender.dublicate = false and tender.win_sum > 1 and tender.id in (SELECT tender from orders left join product p on orders.product = p.id where  p.product_category in (13,11,5,14))
group by w.name;
    */
        if(reportCriteria.getSearchParameters().getDateStart() == null){
            reportCriteria.getSearchParameters().setDateStart(ZonedDateTime.parse("01.01.2018 00:00:00 Z",format_date));
        }
        if(reportCriteria.getSearchParameters().getDateFinish() == null){
            reportCriteria.getSearchParameters().setDateFinish(ZonedDateTime.now());
        }
        String tender = searchAtribut.WhereWithoutProduct(reportCriteria.getSearchParameters()).substring(5);
        String product = searchAtribut.searchTenderByProduct(reportCriteria.getSearchParameters().getProduct());
        String select = "";
        String sum = (company == 0L ? "sum": "win_sum");
        String fromWithJoin = "from tender left join " + (company == 0L
                ? "customer c on tender.customer"
                : "winner c on tender.winner" ) + " = c.id\n";
        select = "Select c.name as 'Компания', sum(tender."+ sum +") as 'Сумма'\n, count(tender.id) as 'Количество тендеров'\n, ";
        List<String> column = new ArrayList<String>();
        column.add("Компания");
        column.add("Сумма");
        column.add("Количество тендеров");

        switch (reportCriteria.getInterval()){
            case "Год":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    select = select + " sum(case when year(tender.date_start)=" + year + " then tender." + sum + " end) as 'Сумма в " + year + "',\n " +
                            "count(case when year(tender.date_start)=" + year + " then tender.id end) as 'Количество в " + year + "',\n ";
                    column.add("Сумма в " + year);
                    column.add("Количество в " + year);
                }
                break;
            case "Финансовый год":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    select = select + " sum(case when (year(date_start) + if(month(date_start)>10,1,0) )= " + year + " then tender."+ sum +" end) as 'Сумма в " + year + "',\n " +
                            "count(case when (year(date_start) + if(month(date_start)>10,1,0) )= " + year + " then tender.id end) as 'Количество в " + year + "',\n ";
                    column.add("Сумма в " + year);
                    column.add("Количество в " + year);
                }
                break;
            case "Неделя":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int week = 1; week <= 52; week++) {
                        select = select + " sum(case when year(tender.date_start)=" + year + " and week(date_start) = " + week + " then tender."+ sum +" end) as 'Сумма в " + year + "W" + week + "',\n " +
                                "count(case when year(tender.date_start)=" + year + " and week(date_start) = " + week + " then tender.id end) as 'Количество в " + year + "W" + week + "',\n ";
                        column.add("Сумма в " + year + "W" + week);
                        column.add("Количество в " + year + "W" + week);
                    }
                }
                break;
            case "Квартал":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int quarter = 1; quarter <= 4; quarter++) {
                        select = select + " sum(case when year(tender.date_start)=" + year + " and quarter(date_start) = " + quarter + " then tender."+ sum +" end) as 'Сумма в " + year + "Q" + quarter + "',\n " +
                                "count(case when year(tender.date_start)=" + year + " and quarter(date_start) = " + quarter + " then tender.id end) as 'Количество в " + year + "Q" + quarter + "',\n ";
                        column.add("Сумма в " + year + "Q" + quarter);
                        column.add("Количество в " + year + "Q" + quarter);
                    }
                }
                break;
            case"Финансовый квартал":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int quarter = 1; quarter <= 4; quarter++) {
                        select = select + " sum(case when (year(date_start) + if(month(date_start)>10,1,0) )= " + year + " and IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3)) = " + quarter + " then tender."+ sum +" end) as 'Сумма в " + year + "FQ" + quarter + "',\n " +
                                "count(case when (year(date_start) + if(month(date_start)>10,1,0) )= " + year + " and IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3)) = " + quarter + " then tender.id end) as 'Количество в " + year + "FQ" + quarter + "',\n ";
                        column.add("Сумма в " + year + "FQ" + quarter);
                        column.add("Количество в " + year + "FQ" + quarter);
                    }
                }
                break;
            case"Месяц":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int month = 1; month <= 12; month++) {
                        select = select + " sum(case when year(tender.date_start)=" + year + " and month(date_start) = " + month + " then tender."+ sum +" end) as 'Сумма в " + year + "M" + month + "',\n " +
                                "count(case when year(tender.date_start)=" + year + " and month(date_start) = " + month + " then tender.id end) as 'Количество в " + year + "M" + month + "',\n ";
                        column.add("Сумма в " + year + "M" + month);
                        column.add("Количество в " + year + "M" + month);
                    }
                }

                break;
        }


        return new Object[]{select.substring(0, select.lastIndexOf(',') )+
                searchAtribut.findTenderForReport(reportCriteria.getSearchParameters(), fromWithJoin) +
                " group by c.name", column};
    }
}
