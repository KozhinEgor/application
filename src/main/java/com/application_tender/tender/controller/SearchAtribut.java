package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.*;
import org.springframework.stereotype.Controller;

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

    public String searchTenderByProduct(List<ProductReceived> products) {

        String where = "";
        if(products != null && products.size() != 0){
        for (ProductReceived product : products) {
            String whereProduct = "(";

            if(product.getVendor_code() != null && product.getVendor_code().length != 0){
                for(Product product1 : product.getVendor_code()){
                    whereProduct = whereProduct + (whereProduct.equals("(") ? " pr.product_category =" + product.getCategory()[0].getId() + " and pr.id_product =" + product1.getId() :
                            " or pr.product_category =" + product.getCategory()[0].getId() + " and pr.id_product =" + product1.getId());
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
        if (json.getInnCustomer() != null && json.getInnCustomer().length() != 0) {
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " c.inn not like \"" + json.getInnCustomer().trim() + "\"" : " and c.inn not like \"" + json.getInnCustomer().trim() + "\"");
            } else {
                where = where + (where.equals("where") ? " c.inn like \"" + json.getInnCustomer().trim() + "\"" : " and c.inn like \"" + json.getInnCustomer().trim() + "\"");
            }
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
                where = where + (where.equals("where") ? " tender.id not in(" + idTender.substring(0, idTender.length() - 1) +")": "and  tender.id not in(" + idTender.substring(0, idTender.length() - 1)+")");
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
        if (json.getInnCustomer() != null && json.getInnCustomer().length() != 0) {
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " c.inn not like \"" + json.getInnCustomer().trim() + "\"" : " and c.inn not like \"" + json.getInnCustomer().trim() + "\"");
            } else {
                where = where + (where.equals("where") ? " c.inn like \"" + json.getInnCustomer().trim() + "\"" : " and c.inn like \"" + json.getInnCustomer().trim() + "\"");
            }
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
        if (json.getInnCustomer() != null && json.getInnCustomer().length() != 0) {
            if (json.isCustomExclude()) {
                where = where + (where.equals("where") ? " c.inn not like \"" + json.getInnCustomer().trim() + "\"" : " and c.inn not like \"" + json.getInnCustomer().trim() + "\"");
            } else {
                where = where + (where.equals("where") ? " c.inn like \"" + json.getInnCustomer().trim() + "\"" : " and c.inn like \"" + json.getInnCustomer().trim() + "\"");
            }
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
        String select = "Select";
        Boolean flag = false;
        Boolean flag_Subcategory = false;
        String category = tableMapper.findNameCategoryById(id);
        String[] columns = tableMapper.findcolumnName(category);
//        Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, vxi, channel, name as vendor from oscilloscope as pr left join vendor v on pr.vendor = v.id
        for (String column : columns) {
            if (column.equals("vendor")) {
                select = select + " pr." + column + " as vendor_id,";
                flag = true;
            } else if (column.equals("subcategory")) {
                select = select + " pr." + column + " as subcategory_id,";
                flag_Subcategory = true;
            } else {
                select = select + " pr." + column + ",";
            }

        }
        if (flag) {
            select = select + " v.name as vendor,";
        }
        if (flag_Subcategory) {
            select = select + " s.name as subcategory,";
        }

        select = select + "(select GROUP_CONCAT(`name` separator ' ')  from options_product left join options on options_product.options = options.id where id_product = pr.id and product_category ="+id+") as options from " + category + " as pr";
        if (flag) {
            select = select + " left join vendor as v on pr.vendor = v.id";
        }
        if (flag_Subcategory) {
            select = select + " left join subcategory as s on pr.subcategory = s.id";
        }
        if (flag) {
            select = select + " order by pr.vendor,vendor_code ";
        } else {
            select = select + " order by vendor_code";
        }
        return select;
    }

    public String createSelectProductNoUses(Long id) {
        String select = "Select";
        boolean flag = false;
        String category = tableMapper.findNameCategoryById(id);
        String[] columns = tableMapper.findcolumnName(category);
//        Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, vxi, channel, name as vendor from oscilloscope as pr left join vendor v on pr.vendor = v.id
        for (String column : columns) {
            if (column.equals("vendor")) {
                select = select + " pr." + column + " as vendor_id,";
                flag = true;
            } else {
                select = select + " pr." + column + ",";
            }

        }
        if (flag) {
            select = select + " v.name as vendor from " + category + " as pr left join vendor as v on pr.vendor = v.id ";
        } else {
            select = select.substring(0, select.length() - 1);
            select = select + " from " + category + " as pr";
        }
        select = select + " where pr.id not in (Select distinct id_product  from keysight.orders where product_category = '" + id + "')";
        if (flag) {
            select = select + "order by pr.vendor, vendor_code";
        } else {
            select = select + " order by vendor_code";
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

    public String UpdateProductTender(Long idTender) {
        List<OrdersDB> ordersDB = tableMapper.findAllOrdersBDbyTender(idTender);
        StringBuilder product = new StringBuilder();
        if (ordersDB != null) {

            List<Orders> orders = new LinkedList<Orders>();

            for (OrdersDB orderDB : ordersDB) {
                String comment = "";
                Product product_id = this.ProductToOrders(orderDB.getProduct_category(), orderDB.getId_product());
                orderDB.setVendor(product_id == null ? Long.valueOf(1) : product_id.getVendor_id());
                if(product_id == null){
                    comment = ((orderDB.getOptions() != null && !orderDB.getOptions().equals("")?orderDB.getOptions() + " ":"")
                            + (orderDB.getPortable() !=null && orderDB.getPortable() ? "портативный " : "")
                            + (orderDB.getUsb() !=null && orderDB.getUsb()? "USB " : "")
                            + (orderDB.getVxi() !=null && orderDB.getVxi()? "VXI " : "")
                            + (orderDB.getFrequency() != null && orderDB.getFrequency() != 0 ?  orderDB.getFrequency() +  "ГГц " : "")
                            + (orderDB.getChannel()!=null && orderDB.getChannel()!= 0? orderDB.getChannel()+  "кан. " : "")
                            + (orderDB.getPort()!=null && orderDB.getPort() != 0? orderDB.getPort()+  "порта " : "")
                            + (orderDB.getForm_factor() != null  && !orderDB.getForm_factor().equals("") ? orderDB.getForm_factor()+" ":"")
                            + (orderDB.getPurpose() != null && !orderDB.getPurpose().equals("") ? orderDB.getPurpose() + " ":"")
                            + (orderDB.getVoltage() != null && orderDB.getVoltage() != 0? orderDB.getVoltage()+"В ":"")
                            + (orderDB.getCurrent() != null && orderDB.getCurrent() != 0? orderDB.getCurrent()+"А ":"")
                            + orderDB.getComment());
                }
                else {
                    comment = ((orderDB.getOptions() != null && !orderDB.getOptions().equals("")?orderDB.getOptions() + " ":"")
                            + orderDB.getComment());
                }
                orders.add(new Orders(orderDB.getTender(),
                        (orderDB.getProduct_category() != 7 ? tableMapper.findOneCategoryById(orderDB.getProduct_category()) + this.subcategoryProduct(orderDB.getProduct_category(), orderDB.getId_product()) : ""),
                        product_id == null ? "" : product_id.getVendor_code(),
                        this.VendorToOrders(orderDB.getProduct_category(), orderDB.getId_product()) == null ? "" :  this.VendorToOrders(orderDB.getProduct_category(), orderDB.getId_product()) + ' ',
                        comment,
                        orderDB.getNumber(),
                        orderDB.getPrice(),
                        orderDB.getWinprice()));
            }
            for (Orders order : orders) {
                product.append(order.ToDB()).append("; ");
            }
            tableMapper.UpdateProductTender(product.toString(), idTender);
        }
        return product.toString();
    }


    public BigCategory makeBigCategory(Long big_category) {
        BigCategory bigCategory = new BigCategory();
        bigCategory.setBig_category_id(big_category);
        bigCategory.setBig_category(tableMapper.findBigCategorybyId(big_category));
        List<Long> categoryId = tableMapper.findCategorybyBigCategory(big_category);
        if (categoryId == null) {
            bigCategory.setCategory(null);
        } else {
            for (Long id : categoryId) {
                ProductCategory productCategory = tableMapper.findCategoryById(id);
                if (productCategory != null) {

                    bigCategory.addCategory(productCategory);
                }

            }
        }
        return bigCategory;
    }
}
