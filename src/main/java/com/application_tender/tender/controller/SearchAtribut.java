package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Controller
public class SearchAtribut {
    private final TableMapper tableMapper;

    public SearchAtribut(TableMapper tableMapper) {
        this.tableMapper = tableMapper;
    }

    public Long findVendor(String name) {
        return tableMapper.findOneVendorByName(name);
    }

    public Long findCustomer(String inn, String name) {
        Long idCustomer = 0L;

        if (inn.equals("null")) {
            inn = "0";

            idCustomer = tableMapper.findCustomerByName(name);
        } else {
            idCustomer = tableMapper.findCustomerByInn(inn.trim());
        }
        if (idCustomer == null) {
            //добавление новой записи
            if (inn.equals("0")) {
                tableMapper.insertCustomer(inn, name, 1L);
            } else {
                tableMapper.insertCustomer(inn, name, 2L);
            }
            idCustomer = tableMapper.findCustomerByNameandINN(name, inn);
        } else {
            if (inn.length() != 0L && tableMapper.findCustomerInnById(idCustomer).equals("0")) {
                tableMapper.updateCustomerInn(inn, idCustomer);
            }
        }
        return idCustomer;
    }

    public Long findTypetender(String type) {
        Long idType = tableMapper.findTypeTenderByType(type);
        if (idType == null) {
            //добавление новой записи
            idType = tableMapper.insertTypeTender(type);
        }
        return idType;
    }

    public String searchTenderIdByProduct(List<ProductReceived> products){
        return  "Select tender from orders o left join product as pr on pr.id = o.product left join product_category on pr.product_category = product_category.id left join subcategory on pr.subcategory = subcategory.id where " + searchTenderByProduct(products);
    }
    public String searchTenderByProduct(List<ProductReceived> products) {

        String where = "";
        if(products != null && products.size() != 0){
        for (ProductReceived product : products) {
            String whereProduct = "(";

            if(product.getVendor_code() != null && product.getVendor_code().length != 0){
                for(Product product1 : product.getVendor_code()){
                    whereProduct = whereProduct + (whereProduct.equals("(") ? " pr.id ="  + product1.getId() :
                            " or pr.id =" + product1.getId());
                }
                where =where + whereProduct + ") or";
                continue;
            }
            if(product.getCategory_product() != null && !product.getCategory_product().equals("")){
                whereProduct = whereProduct + "category_product like '"+ product.getCategory_product()+"'";
            }
            if (product.getCategory() != null && product.getCategory().length != 0 ) {
                whereProduct = whereProduct + (whereProduct.equals("(") ? " pr.product_category in (":" and pr.product_category in (");
                for (ProductCategory productCategory : product.getCategory()){
                    whereProduct = whereProduct + productCategory.getId() + ",";
                }
                whereProduct = whereProduct.substring(0,whereProduct.length()-1) + ")";
            }
            if (product.getVendor() != null && product.getVendor().length != 0) {
                whereProduct = whereProduct + (whereProduct.equals("(") ? " pr.vendor in (":" and pr.vendor in (");
                for (Vendor vendor : product.getVendor()) {
                    whereProduct = whereProduct + vendor.getId() + ",";
                }
                whereProduct = whereProduct.substring(0,whereProduct.length()-1) + ")";
            }

            if ( product.getSubcategory() != null && product.getSubcategory().length != 0) {
                whereProduct = whereProduct + (whereProduct.equals("(") ? "(":" and (");
                for(String subcategory : product.getSubcategory()){
                    whereProduct = whereProduct + "subcategory.name like '"+ subcategory+"' or ";
                }
                whereProduct = whereProduct.substring(0,whereProduct.length()-3) + ")";
            }
             where = where + whereProduct + ") or";
        }

        return where.substring(0,where.length()-2);
        }
        else{
            return where;
        }
    }

    private final DateTimeFormatter format_sql = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    public String findTenderByTerms(SearchParameters json) {
        String where = "where";
        if (json.getDateStart() != null) {
            where = where + " date_start >= \"" + json.getDateStart().format(format_sql) + "\"";
        }
        if (json.getDateFinish() != null) {
            ZonedDateTime finish = json.getDateFinish().plusHours(23L - json.getDateFinish().getHour());
            where = where + (where.equals("where") ? " date_start <= \"" + finish.format(format_sql) + "\"" : " and date_start <= \"" + finish.format(format_sql) + "\"");
        }
        if (json.getType() != null && json.getType().size() != 0) {
            String type = "(";
            for (TypeTender t : json.getType()) {
                type = type + t.getId() + ",";
            }
            type = type.substring(0, type.length() - 1) + ")";
            if (json.isTypeExclude()) {
                where = where + (where.equals("where") ? " typetender not in " + type : " and typetender not in " + type);
            } else {
                where = where + (where.equals("where") ? " typetender in " + type : " and typetender in " + type);
            }
        }
        if(json.getRegions() != null && json.getRegions().size() != 0){
            String regions = "(";
            for (Region c : json.getRegions()) {
                regions = regions + " (c.inn like '" +c.getNumber()+ "%' and c.country = '2') or";
            }
            regions = regions.substring(0, regions.length() - 2) + ")";
                where = where + (where.equals("where") ? regions : " and  " + regions);

        }
        if(json.getDistricts() != null && json.getDistricts().size() != 0){
            String regions = "(";
            for (District c : json.getDistricts()) {
                for(String region : tableMapper.regionInDistrict(c.getId())){
                    regions = regions + " (c.inn like '" +region+ "%' and c.country = '2')or";
                }

            }
            regions = regions.substring(0, regions.length() - 2) + ")";
            System.out.println(regions);
            where = where + (where.equals("where") ? regions : " and  " + regions);
        }
        if (json.getCustom() != null && json.getCustom().size() != 0) {
            String customer = "(";
            for (Company c : json.getCustom()) {
                customer = customer + c.getId() + ",";
            }
            customer = customer.substring(0, customer.length() - 1) + ")";
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " customer not in " + customer : " and customer not in " + customer);
            } else {
                where = where + (where.equals("where") ? " customer in " + customer : " and customer in " + customer);
            }

        }
        if (json.getInnCustomer() != null && json.getInnCustomer().length != 0) {
            where = where + (where.equals("where") ? "(":" and (");
            for(String inn : json.getInnCustomer()){
                if (json.isCustomExclude()) {
                    where = where + " c.inn not like \"" + inn + "\" or";
                } else {
                    where = where +" c.inn like \"" + inn + "\" or";
                }
            }
            where = where.substring(0,where.length()-2)+")";
        }
        if (json.getCountry() != null && json.getCountry() != null) {
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " c.country <> " + json.getCountry() : " and c.country <> " + json.getCountry());
            } else {
                where = where + (where.equals("where") ? " c.country = " + json.getCountry() : " and c.country = " + json.getCountry());
            }
        }
        if (json.getWinner() != null && json.getWinner().size() != 0) {
            String winner = "(";
            for (Company w : json.getWinner()) {
                winner = winner + w.getId() + ",";
            }
            winner = winner.substring(0, winner.length() - 1) + ")";
            if (json.isWinnerExclude()) {
                where = where + (where.equals("where") ? " winner not in " + winner : " and winner not in " + winner);
            } else {
                where = where + (where.equals("where") ? " winner in " + winner : " and winner in " + winner);
            }

        }
        if (json.getMinSum() != null) {
            where = where + (where.equals("where") ? " sum >= " + json.getMinSum() : " and sum >= " + json.getMinSum());
        }
        if (json.getMaxSum() != null) {
            where = where + (where.equals("where") ? " sum <= " + json.getMaxSum() : " and sum <= " + json.getMaxSum());
        }
        if (!json.isDublicate()) {
            where = where + (where.equals("where") ? " dublicate = false" : " and dublicate = false");
        }
        if (json.getIds() != null && json.getIds().length != 0) {
            String id = "(";
            for (Long i : json.getIds()) {
                id = id + i.toString() + ",";
            }
            id = id.substring(0, id.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.equals("where") ? " tender.id not in " + id : " and tender.id not in " + id);
            } else {
                where = where + (where.equals("where") ? " tender.id in " + id : " and tender.id in " + id);
            }
        }
        if (json.getBicotender() != null && json.getBicotender().length != 0) {
            String number_tender = "(";
            for (Long b : json.getBicotender()) {
                number_tender = number_tender + b.toString() + ",";
            }
            number_tender = number_tender.substring(0, number_tender.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.equals("where") ? " number_tender not in " + number_tender : " and number_tender not in " + number_tender);
            } else {
                where = where + (where.equals("where") ? " number_tender in " + number_tender : " and number_tender in " + number_tender);
            }
        }
        if(json.isRealized() && json.isPlan_schedule()){
            String idTender = "";
            for(Long a : this.tableMapper.AllIdPlan()){
                idTender = idTender + a.toString() + ',';
            }
            if(!idTender.equals("")) {
                where = where + (where.equals("where") ? " tender.id not in(" + idTender.substring(0, idTender.length() - 1) +")": " and  tender.id not in(" + idTender.substring(0, idTender.length() - 1)+")");
            }
        }
        if (json.getProduct() != null && json.getProduct().size() != 0) {

            String idString = this.searchTenderByProduct(json.getProduct());

            if (idString.length() != 0) {
                where = where + (where.equals("where") ? " "+this.searchTenderByProduct(json.getProduct()) : " and ("+this.searchTenderByProduct(json.getProduct())+")");
            } else {
                where = where + " tender.id = 0";
            }
            // where = where +  (where.equals("where")?" tender.id in (" + id.toString().substring(1,id.toString().length()-1)+")":" and tender.id in (" + id.toString().substring(1,id.toString().length()-1)+")");
        }
        if (where.equals("where")) {
            return "";
        } else {

            return where;
        }
    }

    public String findTenderForReport(SearchParameters json, String  fromWithJoin) {
        boolean containsCustomer = fromWithJoin.contains("customer") ? true : false;
        if(!containsCustomer){
            fromWithJoin = fromWithJoin + " left join customer cus on tender.customer = cus.id \n";
        }
        String where = "";
        if (json.getDateStart() != null) {
            where = where + " date_start >= \"" + json.getDateStart().format(format_sql) + "\"\n";
        }
        if (json.getDateFinish() != null) {
            ZonedDateTime finish = json.getDateFinish().plusHours(23L - json.getDateFinish().getHour());
            where = where + (where.isBlank() ? " date_start <= \"" + finish.format(format_sql) + "\"\n" : " and date_start <= \"" + finish.format(format_sql) + "\"\n");
        }
        if (json.getType() != null && json.getType().size() != 0) {
            String type = "(";
            for (TypeTender t : json.getType()) {
                type = type + t.getId() + ",";
            }
            type = type.substring(0, type.length() - 1) + ")\n";
            if (json.isTypeExclude()) {
                where = where + (where.isBlank() ? " typetender not in " + type : " and typetender not in " + type);
            } else {
                where = where + (where.isBlank()? " typetender in " + type : " and typetender in " + type);
            }
        }
        if(json.getRegions() != null && json.getRegions().size() != 0){
            String regions = "(";
            for (Region c : json.getRegions()) {
                regions = regions + " (" + (containsCustomer?"c":"cus") + ".inn like '" +c.getNumber()+ "%' and " + (containsCustomer?"c":"cus") + ".country = '2')\n or";
            }
            regions = regions.substring(0, regions.length() - 2) + ")\n";
            where = where + (where.isBlank() ? regions : " and  " + regions);

        }
        if(json.getDistricts() != null && json.getDistricts().size() != 0){
            String regions = "(";
            for (District c : json.getDistricts()) {
                for(String region : tableMapper.regionInDistrict(c.getId())){
                    regions = regions + " (" + (containsCustomer?"c":"cus") + ".inn like '" +region+ "%' and " + (containsCustomer?"c":"cus") + ".country = '2')or";
                }

            }
            regions = regions.substring(0, regions.length() - 2) + ")\n";
            System.out.println(regions);
            where = where + (where.isBlank() ? regions : " and  " + regions);
        }
        if (json.getCustom() != null && json.getCustom().size() != 0) {
            String customer = "(";
            for (Company c : json.getCustom()) {
                customer = customer + c.getId() + ",";
            }
            customer = customer.substring(0, customer.length() - 1) + ")\n";
            if (json.isCustomExclude()) {
                where = where + (where.isBlank() ? " customer not in " + customer : " and customer not in " + customer);
            } else {
                where = where + (where.isBlank() ? " customer in " + customer : " and customer in " + customer);
            }

        }
        if (json.getInnCustomer() != null && json.getInnCustomer().length != 0) {
            where = where + (where.isBlank() ? "(":" and (");
            for(String inn : json.getInnCustomer()){
                if (json.isCustomExclude()) {
                    where = where + " " + (containsCustomer?"c":"cus") + ".inn not like \"" + inn + "\" or";
                } else {
                    where = where +" " + (containsCustomer?"c":"cus") + ".inn like \"" + inn + "\" or";
                }
            }
            where = where.substring(0,where.length()-2)+")\n";
        }
        if (json.getCountry() != null && json.getCountry() != null) {
            if (json.isCustomExclude()) {
                where = where + (where.isBlank() ? " " + (containsCustomer?"c":"cus") + ".country <> " + json.getCountry() : " and " + (containsCustomer?"c":"cus") + ".country <> " + json.getCountry());
            } else {
                where = where + (where.isBlank() ? " " + (containsCustomer?"c":"cus") + ".country = " + json.getCountry() : " and " + (containsCustomer?"c":"cus") + ".country = " + json.getCountry());
            }
        }
        if (json.getWinner() != null && json.getWinner().size() != 0) {
            String winner = "(";
            for (Company w : json.getWinner()) {
                winner = winner + w.getId() + ",";
            }
            winner = winner.substring(0, winner.length() - 1) + ")\n";
            if (json.isWinnerExclude()) {
                where = where + (where.isBlank() ? " winner not in " + winner : " and winner not in " + winner);
            } else {
                where = where + (where.isBlank() ? " winner in " + winner : " and winner in " + winner);
            }

        }
        if (json.getMinSum() != null) {
            where = where + (where.isBlank() ? " sum >= " + json.getMinSum() : " and sum >= " + json.getMinSum());
        }
        if (json.getMaxSum() != null) {
            where = where + (where.isBlank() ? " sum <= " + json.getMaxSum() : " and sum <= " + json.getMaxSum());
        }
        if (!json.isDublicate()) {
            where = where + (where.isBlank() ? " dublicate = false" : " and dublicate = false");
        }
        if (json.getIds() != null && json.getIds().length != 0) {
            String id = "(";
            for (Long i : json.getIds()) {
                id = id + i.toString() + ",";
            }
            id = id.substring(0, id.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.isBlank() ? " tender.id not in " + id : " and tender.id not in " + id);
            } else {
                where = where + (where.isBlank() ? " tender.id in " + id : " and tender.id in " + id);
            }
        }
        if (json.getBicotender() != null && json.getBicotender().length != 0) {
            String number_tender = "(";
            for (Long b : json.getBicotender()) {
                number_tender = number_tender + b.toString() + ",";
            }
            number_tender = number_tender.substring(0, number_tender.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.isBlank() ? " number_tender not in " + number_tender : " and number_tender not in " + number_tender);
            } else {
                where = where + (where.isBlank()? " number_tender in " + number_tender : " and number_tender in " + number_tender);
            }
        }
        if(json.isRealized() && json.isPlan_schedule()){
            String idTender = "";
            for(Long a : this.tableMapper.AllIdPlan()){
                idTender = idTender + a.toString() + ',';
            }
            if(!idTender.equals("")) {
                where = where + (where.isBlank() ? " tender.id not in(" + idTender.substring(0, idTender.length() - 1) +")": " and  tender.id not in(" + idTender.substring(0, idTender.length() - 1)+")");
            }
        }
        if (json.getProduct() != null && json.getProduct().size() != 0) {


            String idString = this.searchTenderIdByProduct(json.getProduct());

            if (idString.length() != 0) {
                where = where + (where.isBlank() ? " tender.id in ("+idString + ") " : " and tender.id in ("+idString+")");
            } else {
                where = where + " tender.id = 0";
            }
            // where = where +  (where.equals("where")?" tender.id in (" + id.toString().substring(1,id.toString().length()-1)+")":" and tender.id in (" + id.toString().substring(1,id.toString().length()-1)+")");
        }
        return fromWithJoin + " where " + where;
    }

    public String orderTender(Long page, String sortName, String sortDirection, Long pageSize){
        switch (sortName){
            case "id":
                sortName = "tender.id";
                break;
            case "nameTender":
                sortName = "tender.name_tender";
                break;
            case "customer":
                sortName = "c.name";
                break;
            case "typetender":
                sortName = "t.type";
                break;
            case "sum":
                sortName = "tender.sum";
                break;
            case "dateStart":
                sortName = "tender.date_start";
                break;
            case "dateFinish":
                sortName = "tender.date_finish";
                break;
            case "winSum":
                sortName = "tender.win_sum";
                break;
            case "winner":
                sortName = "w.name";
                break;
        }
        return (sortDirection.equals("")?"":" order by " + sortName + " "+ sortDirection)+ " limit " + pageSize + " offset " + page*pageSize;
    }

    public String WhereWithoutProduct(SearchParameters json) {
        String where = "where";
        if (json.getDateStart() != null) {
            where = where + " date_start >= \"" + json.getDateStart().format(format_sql) + "\"";
        }
        if (json.getDateFinish() != null) {
            ZonedDateTime finish = json.getDateFinish().plusHours(23L - json.getDateFinish().getHour());
            where = where + (where.equals("where") ? " date_start <= \"" + finish.format(format_sql) + "\"" : " and date_start <= \"" + finish.format(format_sql) + "\"");
        }
        if (json.getType() != null && json.getType().size() != 0) {
            String type = "(";
            for (TypeTender t : json.getType()) {
                type = type + t.getId() + ",";
            }
            type = type.substring(0, type.length() - 1) + ")";
            if (json.isTypeExclude()) {
                where = where + (where.equals("where") ? " typetender not in " + type : " and typetender not in " + type);
            } else {
                where = where + (where.equals("where") ? " typetender in " + type : " and typetender in " + type);
            }
        }
        if (json.getCustom() != null && json.getCustom().size() != 0) {
            String customer = "(";
            for (Company c : json.getCustom()) {
                customer = customer + c.getId() + ",";
            }
            customer = customer.substring(0, customer.length() - 1) + ")";
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " customer not in " + customer : " and customer not in " + customer);
            } else {
                where = where + (where.equals("where") ? " customer in " + customer : " and customer in " + customer);
            }

        }
        if (json.getInnCustomer() != null && json.getInnCustomer().length != 0) {
            where = where + (where.equals("where") ? "(":" and (");
            for(String inn : json.getInnCustomer()){
                if (json.isCustomExclude()) {
                    where = where + " c.inn not like \"" + inn + "\" or";
                } else {
                    where = where +" c.inn like \"" + inn + "\" or";
                }
            }
            where = where.substring(0,where.length()-2)+")";
        }
        if (json.getCountry() != null && json.getCountry() != null) {
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " c.country <> " + json.getCountry() : " and c.country <> " + json.getCountry());
            } else {
                where = where + (where.equals("where") ? " c.country = " + json.getCountry() : " and c.country = " + json.getCountry());
            }
        }
        if (json.getWinner() != null && json.getWinner().size() != 0) {
            String winner = "(";
            for (Company w : json.getWinner()) {
                winner = winner + w.getId() + ",";
            }
            winner = winner.substring(0, winner.length() - 1) + ")";
            if (json.isWinnerExclude()) {
                where = where + (where.equals("where") ? " winner not in " + winner : " and winner not in " + winner);
            } else {
                where = where + (where.equals("where") ? " winner in " + winner : " and winner in " + winner);
            }

        }
        if (json.getMinSum() != null) {
            where = where + (where.equals("where") ? " sum >= " + json.getMinSum() : " and sum >= " + json.getMinSum());
        }
        if (json.getMaxSum() != null) {
            where = where + (where.equals("where") ? " sum <= " + json.getMaxSum() : " and sum <= " + json.getMaxSum());
        }
        if (!json.isDublicate()) {
            where = where + (where.equals("where") ? " dublicate = false" : " and dublicate = false");
        }
        if (json.getIds() != null && json.getIds().length != 0) {
            String id = "(";
            for (Long i : json.getIds()) {
                id = id + i.toString() + ",";
            }
            id = id.substring(0, id.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.equals("where") ? " tender.id not in " + id : " and tender.id not in " + id);
            } else {
                where = where + (where.equals("where") ? " tender.id in " + id : " and tender.id in " + id);
            }
        }
        if (json.getBicotender() != null && json.getBicotender().length != 0) {
            String number_tender = "(";
            for (Long b : json.getBicotender()) {
                number_tender = number_tender + b.toString() + ",";
            }
            number_tender = number_tender.substring(0, number_tender.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.equals("where") ? " number_tender not in " + number_tender : " and number_tender not in " + number_tender);
            } else {
                where = where + (where.equals("where") ? " number_tender in " + number_tender : " and number_tender in " + number_tender);
            }
        }
        if (where.equals("where")) {
            return "";
        } else {

            return where;
        }
    }

    public String ParametrsWithoutProductAndDate(SearchParameters json) {
        String where = "where";
        if (json.getType() != null && json.getType().size() != 0) {
        String type = "(";
        for (TypeTender t : json.getType()) {
            type = type + t.getId() + ",";
        }
        type = type.substring(0, type.length() - 1) + ")";
        if (json.isTypeExclude()) {
            where = where + (where.equals("where") ? " typetender not in " + type : " and typetender not in " + type);
        } else {
            where = where + (where.equals("where") ? " typetender in " + type : " and typetender in " + type);
        }
    }
        if (json.getCustom() != null && json.getCustom().size() != 0) {
            String customer = "(";
            for (Company c : json.getCustom()) {
                customer = customer + c.getId() + ",";
            }
            customer = customer.substring(0, customer.length() - 1) + ")";
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " customer not in " + customer : " and customer not in " + customer);
            } else {
                where = where + (where.equals("where") ? " customer in " + customer : " and customer in " + customer);
            }

        }
        if (json.getInnCustomer() != null && json.getInnCustomer().length != 0) {
            where = where + (where.equals("where") ? "(":" and (");
            for(String inn : json.getInnCustomer()){
                if (json.isCustomExclude()) {
                    where = where + " c.inn not like \"" + inn + "\" or";
                } else {
                    where = where +" c.inn like \"" + inn + "\" or";
                }
            }
            where = where.substring(0,where.length()-2)+")";
        }
        if (json.getCountry() != null && json.getCountry() != null) {
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " c.country <> " + json.getCountry() : " and c.country <> " + json.getCountry());
            } else {
                where = where + (where.equals("where") ? " c.country = " + json.getCountry() : " and c.country = " + json.getCountry());
            }
        }
        if (json.getWinner() != null && json.getWinner().size() != 0) {
            String winner = "(";
            for (Company w : json.getWinner()) {
                winner = winner + w.getId() + ",";
            }
            winner = winner.substring(0, winner.length() - 1) + ")";
            if (json.isWinnerExclude()) {
                where = where + (where.equals("where") ? " winner not in " + winner : " and winner not in " + winner);
            } else {
                where = where + (where.equals("where") ? " winner in " + winner : " and winner in " + winner);
            }

        }
        if (json.getMinSum() != null) {
            where = where + (where.equals("where") ? " sum >= " + json.getMinSum() : " and sum >= " + json.getMinSum());
        }
        if (json.getMaxSum() != null) {
            where = where + (where.equals("where") ? " sum <= " + json.getMaxSum() : " and sum <= " + json.getMaxSum());
        }
        if (!json.isDublicate()) {
            where = where + (where.equals("where") ? " dublicate = false" : " and dublicate = false");
        }
        if (json.getIds() != null && json.getIds().length != 0) {
            String id = "(";
            for (Long i : json.getIds()) {
                id = id + i.toString() + ",";
            }
            id = id.substring(0, id.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.equals("where") ? " tender.id not in " + id : " and tender.id not in " + id);
            } else {
                where = where + (where.equals("where") ? " tender.id in " + id : " and tender.id in " + id);
            }
        }
        if (json.getBicotender() != null && json.getBicotender().length != 0) {
            String number_tender = "(";
            for (Long b : json.getBicotender()) {
                number_tender = number_tender + b.toString() + ",";
            }
            number_tender = number_tender.substring(0, number_tender.length() - 1) + ")";
            if (json.isNumberShow()) {
                where = where + (where.equals("where") ? " number_tender not in " + number_tender : " and number_tender not in " + number_tender);
            } else {
                where = where + (where.equals("where") ? " number_tender in " + number_tender : " and number_tender in " + number_tender);
            }
        }
        if (where.equals("where")) {
            return "";
        } else {

            return where;
        }
    }

    public String createSelectProductCategory(Long id) {
        String select="";
        if(id == 0L){
            select = "Select pr.id,pr.subcategory as subcategory_id, s.name as subcategory,pr.product_category as product_category_id, prcat.category as product_category,pr.vendor_code,v.name as vendor, pr.vendor as vendor_id," +
                    " pr.frequency,pr.usb,pr.vxi,pr.portable,pr.channel,pr.port,pr.form_factor,pr.purpose,pr.voltage,pr.current from product as pr" +
                    " left join vendor as v on pr.vendor = v.id left join subcategory as s on pr.subcategory = s.id left join product_category as prcat on pr.product_category = prcat.id order by product_category,pr.vendor,vendor_code";
        }
        else{
            select = "Select pr.id,pr.subcategory as subcategory_id, s.name as subcategory,pr.product_category as product_category_id, prcat.category as product_category, pr.vendor_code,v.name as vendor, pr.vendor as vendor_id," +
                    " pr.frequency,pr.usb,pr.vxi,pr.portable,pr.channel,pr.port,pr.form_factor,pr.purpose,pr.voltage,pr.current from product as pr" +
                    " left join vendor as v on pr.vendor = v.id left join subcategory as s on pr.subcategory = s.id left join product_category as prcat on pr.product_category = prcat.id where pr.product_category = "+id + " order by pr.vendor,vendor_code";

        }
        return select;
    }

    public String createSelectProductNoUses(Long id) {
        String select="";
        if(id == 0L){
            select = "Select pr.id,pr.subcategory as subcategory_id, s.name as subcategory,pr.product_category as product_category_id, prcat.category as product_category, pr.vendor_code,v.name as vendor, pr.vendor as vendor_id," +
                    " pr.frequency,pr.usb,pr.vxi,pr.portable,pr.channel,pr.port,pr.form_factor,pr.purpose,pr.voltage,pr.current from product as pr" +
                    " left join vendor as v on pr.vendor = v.id left join subcategory as s on pr.subcategory = s.id left join product_category as prcat on pr.product_category = prcat.id where pr.id not in (Select distinct product from keysight.orders) order by pr.vendor,vendor_code";
        }
        else{
             select = "Select pr.id,pr.subcategory as subcategory_id, s.name as subcategory,pr.product_category as product_category_id, prcat.category as product_category, pr.vendor_code,v.name as vendor, pr.vendor as vendor_id," +
                    " pr.frequency,pr.usb,pr.vxi,pr.portable,pr.channel,pr.port,pr.form_factor,pr.purpose,pr.voltage,pr.current from product as pr" +
                    " left join vendor as v on pr.vendor = v.id left join subcategory as s on pr.subcategory = s.id left join product_category as prcat on pr.product_category = prcat.id where pr.id not in (Select distinct product from keysight.orders where product_category = '" + id + "') and product_category ='"+id+"' order by pr.vendor,vendor_code";

        }
        return select;
    }

    public Product ProductToOrders(Long id, Long product_id) {
        String select = this.createSelectProductCategory(id);
        int index = select.indexOf("order by");
        select = select.substring(0, index - 1) + " where pr.id ='" + String.valueOf(product_id) + "'" + select.substring(index);
        Product product = tableMapper.findOneProduct(select);
        if (product.getVendor_code().equals("Без артикула")) {
            product = null;
        }
        return product;
    }

    public String VendorToOrders(Long id, Long product_id) {
        if(id != 7 ){
            String vendor = tableMapper.findOneVendorByIdProduct(product_id,tableMapper.findNameCategoryById(id));
            if (vendor.equals("No vendor")) {
                vendor = null;
            }
            return vendor;
        }
        else return null;

    }

    public String subcategoryProduct(Long id, Long product_id) {
        if (Arrays.asList(tableMapper.findcolumnName(tableMapper.findNameCategoryById(id))).contains("subcategory")) {
            return tableMapper.findSubcategoryProduct(tableMapper.findNameCategoryById(id), product_id) != null ? " " + tableMapper.findSubcategoryProduct(tableMapper.findNameCategoryById(id), product_id) : "";
        }

        return "";
    }

    public void UpdateProductTender(Long idTender) {
        List<Orders> orders = tableMapper.findAllOrdersbyTender(idTender);
        StringBuilder product = new StringBuilder();
        Orders anotherProduct = tableMapper.findAnotherProductbyTender(idTender);
        if(anotherProduct != null){
            orders.remove(anotherProduct);
            orders.add(anotherProduct);
        }
        if (orders != null) {
            tableMapper.UpdateProductTender(this.generateProductString(orders), idTender);
        }
        else {
            tableMapper.UpdateProductTender("", idTender);
        }
    }

    public String generateProductString(List<Orders> orders){
        StringBuilder product = new StringBuilder();
        for(Orders o : orders){
            String comment = "";

            if (o.getProduct().equals("Без артикула")){
                comment = ((o.getOptions() != null && !o.getOptions().equals("")?o.getOptions() + " ":"")
                        + (o.getPortable() !=null && o.getPortable() ? "портативный " : "")
                        + (o.getUsb() !=null && o.getUsb()? "USB " : "")
                        + (o.getVxi() !=null && o.getVxi()? "VXI " : "")
                        + (o.getFrequency() != null && o.getFrequency() != 0 ?  o.getFrequency() +  "ГГц " : "")
                        + (o.getChannel()!=null && o.getChannel()!= 0? o.getChannel()+  "кан. " : "")
                        + (o.getPort()!=null && o.getPort() != 0? o.getPort()+  "порта " : "")
                        + (o.getForm_factor() != null  && !o.getForm_factor().equals("") ? o.getForm_factor()+" ":"")
                        + (o.getPurpose() != null && !o.getPurpose().equals("") ? o.getPurpose() + " ":"")
                        + (o.getVoltage() != null && o.getVoltage() != 0? o.getVoltage()+"В ":"")
                        + (o.getCurrent() != null && o.getCurrent() != 0? o.getCurrent()+"А ":"")
                        + o.getComment_DB());
            }
            else{
                comment = ((o.getOptions() != null && !o.getOptions().equals("")?o.getOptions() + " ":"")
                        + o.getComment_DB());
            }
            o.setComment(comment);
            product.append(o.ToDB()).append("; ");
        }
        return product.toString();
    }

public List<Orders> generateOrders(Long idTender){
    List<Orders> orders = tableMapper.findAllOrdersbyTender(idTender);
    Orders anotherProduct = tableMapper.findAnotherProductbyTender(idTender);
    if(anotherProduct != null){
        orders.remove(anotherProduct);
        orders.add(anotherProduct);
    }
    if (orders != null) {

        for(Orders o : orders){
            String comment = "";

            if (o.getProduct().equals("Без артикула")){
                comment = ((o.getOptions() != null && !o.getOptions().equals("")?o.getOptions() + " ":"")
                        + (o.getPortable() !=null && o.getPortable() ? "портативный " : "")
                        + (o.getUsb() !=null && o.getUsb()? "USB " : "")
                        + (o.getVxi() !=null && o.getVxi()? "VXI " : "")
                        + (o.getFrequency() != null && o.getFrequency() != 0 ?  o.getFrequency() +  "ГГц " : "")
                        + (o.getChannel()!=null && o.getChannel()!= 0? o.getChannel()+  "кан. " : "")
                        + (o.getPort()!=null && o.getPort() != 0? o.getPort()+  "порта " : "")
                        + (o.getForm_factor() != null  && !o.getForm_factor().equals("") ? o.getForm_factor()+" ":"")
                        + (o.getPurpose() != null && !o.getPurpose().equals("") ? o.getPurpose() + " ":"")
                        + (o.getVoltage() != null && o.getVoltage() != 0? o.getVoltage()+"В ":"")
                        + (o.getCurrent() != null && o.getCurrent() != 0? o.getCurrent()+"А ":"")
                        + o.getComment_DB());
            }
            else{
                comment = ((o.getOptions() != null && !o.getOptions().equals("")?o.getOptions() + " ":"")
                        + o.getComment_DB());
            }
            o.setComment(comment);
        }
    }

    return orders;
}

    String returnItems(Integer num){
        int rem = num% 100;
        if(rem<5 || rem>20){
            rem = rem%10;
            if(rem == 1) return num.toString()+" наименование";
            else return num.toString()+" наименования";
        }
        else return num.toString()+" наименований";
    }

    LocalDate startDateByPeriod(String period){
        switch (period){
            case "неделя": return LocalDate.now().minusDays(7);
            case "Месяц" : return LocalDate.now().minusDays(30);
            case "Квартал" : return LocalDate.now().minusDays(90);
            case "Год" : return LocalDate.now().minusDays(365);
            default: return LocalDate.now().minusDays(7);
        }
    }
}
