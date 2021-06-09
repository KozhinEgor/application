package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.Customer;
import com.application_tender.tender.models.Tender;
import com.application_tender.tender.models.TypeTender;
import com.application_tender.tender.models.Winner;
import com.application_tender.tender.subsidiaryModels.ProductReceived;
import com.application_tender.tender.subsidiaryModels.ReceivedJSON;
import com.sun.istack.NotNull;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class SearchAtribut {
    private  final TableMapper tableMapper;

    public SearchAtribut(TableMapper tableMapper) {
        this.tableMapper = tableMapper;
    }

    public Long findCustomer (String inn, String name) {
       Long idCustomer = 0L;
        System.out.println(inn);
        if(inn.length() == 0){
            inn = "0";
            System.out.println(name);
            idCustomer = tableMapper.findCustomerByName(name);
        }
        else {
            idCustomer = tableMapper.findCustomerByInn(inn.trim());
        }
        if( idCustomer == null){
            //добавление новой записи
            tableMapper.insertCustomer(inn,name,1L); // Заменить
            idCustomer = tableMapper.findCustomerByNameandINN(name,inn);
        }
        else {
            if(inn.length() != 0L && tableMapper.findCustomerInnById(idCustomer) == "0"){
                tableMapper.updateCustomerInn(inn,idCustomer);
            }
        }
        return idCustomer;
    }

    public Long findTypetender(String type){
        Long idType = tableMapper.findTypeTenderByType(type);
        if(idType == null){
            //добавление новой записи
            idType = tableMapper.insertTypeTender(type);
        }
        return idType;
    }
    public List<Long> searchTenderByProduct(ProductReceived[] products){
        String where = "";
        for(ProductReceived product : products){
            if(product.getVendor_code() == null && product.getVendor() == null){
                where = where + (where.equals("")?" product_category ="+product.getCategory().getId():" or product_category ="+product.getCategory().getId());
                }
           else if(product.getVendor_code() == null){
                List<Long> id_product = tableMapper.findProductByVendor(tableMapper.findOneCategoryENById(product.getCategory().getId()),product.getVendor().getId());
                where = where + (where.equals("")? " product_category = " +product.getCategory().getId() + " and id_product in ("+ id_product.toString().substring(1,id_product.toString().length() - 1)+")":
                        " or product_category =" +product.getCategory().getId() + " and id_product in ("+ id_product.toString().substring(1,id_product.toString().length() - 1)+")");
            }
            else {
                where = where + (where.equals("")?" product_category ="+product.getCategory().getId() + " and id_product =" +product.getVendor_code().getId():
                        " or product_category ="+product.getCategory().getId() + " and id_product =" +product.getVendor_code().getId());
            }
        }
        return tableMapper.findTenderByProduct(where);
    }
    private final DateTimeFormatter format_sql = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    public List<Tender> findTenderByTerms(ReceivedJSON json){
        String where = "where";
        if(json.getDateStart() != null ){
            where = where +  " date_start >= \"" + json.getDateStart().format(format_sql)+"\"";
        }
        if(json.getDateFinish() != null){
            where = where +  (where.equals("where")?" date_start <= \"" + json.getDateFinish().format(format_sql)+"\"":" and date_start <= \"" + json.getDateFinish().format(format_sql)+"\"");
        }
        if( json.getType().length != 0){
            String type = "(";
            for(TypeTender t : json.getType()){
                type = type + t.getId() + ",";
            }
            type = type.substring(0,type.length()-1) + ")";
            if(json.isTypeExclude()){
                where = where +  (where.equals("where")?" typetender not in " + type:" and typetender not in " + type);
            }
            else{
                where = where +  (where.equals("where")?" typetender in " + type:" and typetender in " + type);
            }
        }
        if(json.getCustom().length != 0){
            String customer = "(";
            for(Customer c:json.getCustom()){
                customer = customer + c.getId() + ",";
            }
            customer = customer.substring(0,customer.length() - 1) + ")";
            if(json.isCustomExclude()){
                where = where +  (where.equals("where")?" customer not in " + customer:" and customer not in " + customer);
            }
            else{
                where = where +  (where.equals("where")?" customer in " + customer:" and customer in " + customer);
            }

        }
        if(json.getInnCustomer().length() != 0){
            if(json.isCustomExclude()){
                where = where +  (where.equals("where")?" c.inn not like \""+ json.getInnCustomer() +"\"":" and c.inn not like \""+ json.getInnCustomer() +"\"");
            }
            else{
                where = where +  (where.equals("where")?" c.inn like \""+ json.getInnCustomer() +"\"":" and c.inn like \""+ json.getInnCustomer() +"\"");
            }
        }
        if(json.getCountry() != null){
            if(json.isCustomExclude()){
                where = where +  (where.equals("where")?" c.country <> "+ json.getCountry():" and c.country <> "+ json.getCountry());
            }
            else{
                where = where +  (where.equals("where")?" c.country = "+ json.getCountry():" and c.country = "+ json.getCountry());
            }
        }
        if(json.getWinner().length != 0 ){
            String winner = "(";
            for(Winner w : json.getWinner()){
                winner = winner + w.getId() + ",";
            }
            winner = winner.substring(0,winner.length() - 1) + ")";
            if(json.isWinnerExclude()){
                where = where +  (where.equals("where")?" winner not in " + winner:" and winner not in " + winner);
            }
            else{
                where = where +  (where.equals("where")?" winner in " + winner:" and winner in " + winner);
            }

        }
        if(json.getMinSum() != null){
            where = where +  (where.equals("where")?" sum >= " + json.getMinSum():" and sum >= " + json.getMinSum());
        }
        if(json.getMaxSum() != null){
            where = where +  (where.equals("where")?" sum <= " + json.getMaxSum():" and sum <= " + json.getMaxSum());
        }
        if(!json.isDublicate()){
            where = where +  (where.equals("where")?" dublicate = false":" and dublicate = false");
        }
        if(json.getIds().length != 0){
            String id = "(";
            for(Long i : json.getIds()){
                id = id+ i.toString()+",";
            }
            id = id.substring(0,id.length() - 1) +")";
            if(json.isNumberShow()){
                where = where +  (where.equals("where")?" tender.id not in " + id:" and tender.id not in " + id);
            }
            else{
                where = where +  (where.equals("where")?" tender.id in " + id:" and tender.id in " + id);
            }
        }
        if(json.getBicotender().length != 0){
            String number_tender = "(";
            for(Long b : json.getBicotender()){
                number_tender = number_tender + b.toString() + ",";
            }
            number_tender = number_tender.substring(0,number_tender.length() - 1) + ")";
            if(json.isNumberShow()){
                where = where +  (where.equals("where")?" number_tender not in "+ number_tender:" and number_tender not in "+ number_tender);
            }
            else{
                where = where +  (where.equals("where")?" number_tender in "+ number_tender:" and number_tender in "+ number_tender);
            }
        }
        if(json.getProduct().length != 0){
            List<Long> id = this.searchTenderByProduct(json.getProduct());
            where = where +  (where.equals("where")?" tender.id in (" + id.toString().substring(1,id.toString().length()-1)+")":" and tender.id in (" + id.toString().substring(1,id.toString().length()-1)+")");
        }
        if(where.equals("where")){
            return tableMapper.findAllTenderTerms("");
        }
        else{
            return tableMapper.findAllTenderTerms(where);
        }
    }
}
