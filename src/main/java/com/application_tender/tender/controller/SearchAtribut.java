package com.application_tender.tender.controller;

import com.application_tender.tender.mapper.TableMapper;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SearchAtribut {
    private  final TableMapper tableMapper;

    public SearchAtribut(TableMapper tableMapper) {
        this.tableMapper = tableMapper;
    }

    public Long findCustomer (String inn, String name) {
       Long idCustomer = null;
        if(inn.length() == 0){
            inn = "0";
            idCustomer = tableMapper.findCustomerByName(name);
        }
        else {

            idCustomer = tableMapper.findCustomerByInn("23");
        }
        if(idCustomer == null){
            //добавление новой записи
            idCustomer = tableMapper.insertCustomer(name,inn);
        }
        else {
            if(inn.length() != 0 && tableMapper.findCustomerInnById(idCustomer) == "0"){
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

}
