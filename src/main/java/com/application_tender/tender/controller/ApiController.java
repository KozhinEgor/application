package com.application_tender.tender.controller;


import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.Product;
import com.application_tender.tender.subsidiaryModels.ReceivedJSON;
import com.application_tender.tender.subsidiaryModels.TenderExcel;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/demo")
public class ApiController {
    private DateTimeFormatter format_date= DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
    private  final TableMapper tableMapper;
    @Autowired
    private SearchAtribut searchAtribut;


    public ApiController(TableMapper tableMapper) {
        this.tableMapper = tableMapper;
    }

    @GetMapping("/TypeTender")
    @ResponseBody
    List<TypeTender> typeTender(){
        return tableMapper.findAllType();
    }
    @GetMapping("/Customer")
    @ResponseBody
    List<Customer> customer(){
        return tableMapper.findAllCustomer();
    }
    @GetMapping("/Winner")
    @ResponseBody
    List<Winner> winner(){
        return tableMapper.findAllWinner();
    }
    @PostMapping("/Tender")
    @ResponseBody
    List<Tender> Tender(@RequestBody ReceivedJSON json){
        System.out.println(json.getDateStart());
        return tableMapper.findAllTenderTerms(json.getDateStart(),
                                            json.getDateFinish(),
                                            json.getType(),
                                            json.getWinner(),
                                            json.getCustom(),
                                            json.getMinSum(),
                                            json.getMaxSum());
    }
    @GetMapping("/AnotherProduct")
    @ResponseBody
    List<AnotherProduct> AnotherProduct(){
        return tableMapper.findAllAnotherProduct();
    }
    @GetMapping("/ProductCategory")
    @ResponseBody
    List<ProductCategory> ProductCategory(){
        return tableMapper.findAllProductCategory();
    }
    @GetMapping("/VendorCode/{id}")
    @ResponseBody
    List<Product> Product(@PathVariable int id){
        if(id == 1){
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else if (id == 2){
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else if (id == 3){
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else if (id == 4){
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else if (id == 6){
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else{
            System.out.println(tableMapper.findAllAnotherProductToProduct().get(0).isPortable());
            return tableMapper.findAllAnotherProductToProduct();
        }

    }
    @RequestMapping(value = "/addTender", method = RequestMethod.POST, consumes = { "multipart/form-data" })
    @ResponseBody
    List<Tender> addTender(MultipartFile excel) throws IOException {

        LinkedList<Tender> tenders = new LinkedList<Tender>();
        File temp = new File("C:\\Users\\egkozhin\\IdeaProjects\\application\\temp.xlsx");

        excel.transferTo(temp);
        InputStream ExcelFileToRead = new FileInputStream(temp);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int count = 1;
        while (sheet.getRow(count).getCell(0) != null) {
            XSSFRow row = sheet.getRow(count);
            String numberTender = new DataFormatter().formatCellValue(row.getCell(7));
            Long id;
            if (tableMapper.findTenderByNumber_tender(numberTender)!= null){

                id = tableMapper.findTenderByNumber_tender(numberTender);
                System.out.println(id);
            }
            else{
                System.out.println("ДОБАВИЛ");
                String INNCustomer = new DataFormatter().formatCellValue(row.getCell(3));

                        id = tableMapper.insertTender(row.getCell(0).getStringCellValue(),
                            row.getCell(0).getHyperlink().getAddress(),
                            row.getCell(1).getHyperlink().getAddress(),
                            ZonedDateTime.parse(row.getCell(8).getStringCellValue() + " 00:00:00 Z", format_date),
                            ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " Z", format_date),
                            numberTender,
                            new BigDecimal(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING),
                            new BigDecimal(0),
                            row.getCell(5).getStringCellValue(),
                            new BigDecimal(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING),
                            0.0,
                            new BigDecimal(0),
                            searchAtribut.findCustomer(INNCustomer, sheet.getRow(count).getCell(2).getStringCellValue()),
                            searchAtribut.findTypetender(row.getCell(6).getStringCellValue()),
                            Long.valueOf(1)
                        );
            }
            tenders.add(tableMapper.findTenderbyId(id));
            count++;
        }

        ExcelFileToRead.close();

        return tenders;
    }
    @RequestMapping(value = "/Test", method = RequestMethod.POST, consumes = { "multipart/form-data" })
    @ResponseBody
    String Test(MultipartFile excel) throws IOException {

        File temp = new File("C:\\Users\\egkozhin\\IdeaProjects\\application\\temp.xlsx");
        excel.transferTo(temp);
        InputStream ExcelFileToRead = new FileInputStream(temp);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFileToRead );
        System.out.println(workbook.getNumberOfSheets());
        XSSFSheet sheet = workbook.getSheetAt(0);
        ExcelFileToRead.close();
        return "goodConnect";
    }
}
