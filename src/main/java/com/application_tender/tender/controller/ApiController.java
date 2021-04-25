package com.application_tender.tender.controller;


import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;

@Controller
@RequestMapping(path = "/demo")
public class ApiController {
    private final DateTimeFormatter format_date= DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
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
            //анализатор спектра
            return tableMapper.findAllSpectrum_analyserToProduct();
        }
        else if (id == 2){
            //генератор сигналов
            return tableMapper.findAllSignalGeneratorToProduct();
        }
        else if (id == 3){
            //генератор импульсов
            return tableMapper.findAllPulseGeneratorToProduct();
        }
        else if (id == 4){
            //анализатор сигналов
            return tableMapper.findAllSignalAnalyzerToProduct();
        }
        else if (id == 5){
            //анализатор сигналов
            return tableMapper.findAllNetworkAnalyzersToProduct();
        }
        else if (id == 6){
            //осциллограф
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else{
            //Продукты
            return tableMapper.findAllAnotherProductToProduct();
        }

    }
    @GetMapping("/VendorCodeById/{id}/{id_product}")
    @ResponseBody
    Product ProductById(@PathVariable Long id,@PathVariable Long id_product){
        if(id == 1){
            //анализатор спектра\
            return tableMapper.findAllSpectrum_analyserToProductById(id_product);
        }
        else if (id == 2){
            //генератор сигналов
            return tableMapper.findAllSignalGeneratorToProductById(id_product);
        }
        else if (id == 3){
            //генератор импульсов
            return tableMapper.findAllPulseGeneratorToProductById(id_product);
        }
        else if (id == 4){
            //анализатор сигналов
            return tableMapper.findAllSignalAnalyzerToProductById(id_product);
        }
        else if (id == 5){
            //анализатор сигналов
            return tableMapper.findAllNetworkAnalyzersToProductById(id_product);
        }
        else if (id == 6){
            //осциллограф
            return tableMapper.findAllOscilloscopeToProductById(id_product);
        }
        else{
            //Продукты
            return tableMapper.findAllAnotherProductToProductById(id_product);
        }

    }
    @GetMapping("/OrdersByTender/{tender}")
    @ResponseBody
    OrdersReceived OrdersByTender(@PathVariable Long tender){
        List<OrdersDB> ordersDB = tableMapper.findAllOrdersBDbyTender(tender);
        List<Orders> orders = new LinkedList<>();
        for (OrdersDB orderDB : ordersDB ){
            Long id = orderDB.getProduct_category();
            Product product_id = null;
            if(id == 1){
                //анализатор спектра
                product_id = tableMapper.findOneSpectrum_analyserById(orderDB.getId_product());
                if (product_id.getId().equals(122L)){
                    product_id = null;
                }
            }
            else if (id == 2){
                //генератор сигналов
                product_id = tableMapper.findOneSignalGeneratorById(orderDB.getId_product());
                if (product_id.getId().equals(269L)){
                    product_id = null;
                }
            }
            else if (id == 3){
                //генератор импульсов
                product_id = tableMapper.findOnePulseGeneratorById(orderDB.getId_product());
                if (product_id.getId().equals(30L)){
                    product_id = null;
                }
            }
            else if (id == 4){
                //анализатор сигналов
                product_id = tableMapper.findOneSignalAnalyzerById(orderDB.getId_product());
                if (product_id.getId().equals(122L)){
                    product_id = null;
                }
            }
            else if (id == 5){
                //анализатор цепей
                product_id = tableMapper.findOneNetworkAnalyzersById(orderDB.getId_product());

                if (product_id.getId().equals( 56L)){
                    product_id = null;
                }
            }
            else if (id == 6){
                //осциллограф
                product_id = tableMapper.findOneOscilloscopeById(orderDB.getId_product());
                if (product_id.getId().equals(506L)){
                    product_id = null;
                }
            }
            else{
                //Продукты
                product_id = tableMapper.findOneAnotherProductById(orderDB.getId_product());
            }
            orders.add(new Orders(orderDB.getTender(),
                    tableMapper.findOneCategoryById(orderDB.getProduct_category()),
                    ((product_id == null || product_id.getVendor_id() == null || product_id.getVendor_id().equals(1L)) ? "" : tableMapper.findOneVendorById(product_id.getVendor_id())  + ' ') + (product_id == null ? "": product_id.getVendor_code() + " "),
                    product_id == null ?  tableMapper.findOneVendorById(1L) : tableMapper.findOneVendorById(product_id.getVendor_id()),
                    orderDB.getComment(),
                    orderDB.getNumber(),
                    orderDB.getPrice(),
                    orderDB.getWinprice()));
        }
        return new OrdersReceived(orders,ordersDB);
    }
    @GetMapping("/OrdersBDByTender/{tender}")
    @ResponseBody
    List<OrdersDB> OrdersBDByTender(@PathVariable Long tender){
        return tableMapper.findAllOrdersBDbyTender(tender);
    }
    @RequestMapping(value = "/addTender", method = RequestMethod.POST, consumes = { "multipart/form-data" })
    @ResponseBody
    List<Tender> addTender(MultipartFile excel) throws IOException {

        LinkedList<Tender> tenders = new LinkedList<>();
        File temp = new File("C:\\Users\\egkozhin\\IdeaProjects\\application\\temp.xlsx");

        excel.transferTo(temp);
        InputStream ExcelFileToRead = new FileInputStream(temp);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int count = 1;
        while (sheet.getRow(count).getCell(0) != null) {
            XSSFRow row = sheet.getRow(count);
            String numberTender = new DataFormatter().formatCellValue(row.getCell(7));
            if(numberTender.equals("")){
                break;
            }
            Long id;
            if (tableMapper.findTenderByNumber_tender(numberTender) != null){

                id = tableMapper.findTenderByNumber_tender(numberTender);
                System.out.println(id);
            }
            else{
                System.out.println("ДОБАВИЛ");
                String INNCustomer = new DataFormatter().formatCellValue(row.getCell(3));

                        id = tableMapper.insertTender(row.getCell(0).getStringCellValue(),
                            "https://www.bicotender.ru/tc/tender/show/tender_id/" + numberTender,
                            row.getCell(1).getStringCellValue(),
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
                                1L
                        );
                id = tableMapper.findTenderByNumber_tender(numberTender);
                System.out.println(id);
            }
            tenders.add(tableMapper.findTenderbyId(id));
            count++;
        }

        ExcelFileToRead.close();

        return tenders;
    }

    @GetMapping("/Vendor")
    @ResponseBody
    List<Vendor> Vendor(){
        return tableMapper.findAllVendor();
    }

    @PostMapping("/addOrders")
    @ResponseBody
    String Tender(@RequestBody List<OrdersDB> json){
        List<Long> ordersINDB = tableMapper.findAllOrdersIdbyTender(json.get(0).getTender());
        if(json.get(0).getId_product() != null) {
            for (OrdersDB ordersDB : json) {
                if (ordersINDB.contains(ordersDB.getId())) {
                    ordersINDB.remove(ordersDB.getId());
                }
                System.out.println(ordersDB.toString());
                if (ordersDB.getId() == null) {

                    tableMapper.insertOrder(
                            ordersDB.getComment(),
                            ordersDB.getId_product(),
                            ordersDB.getProduct_category(),
                            ordersDB.getTender(),
                            ordersDB.getNumber(),
                            ordersDB.getPrice() == null ? new BigDecimal(0) : ordersDB.getPrice(),
                            ordersDB.getWinprice() == null ? new BigDecimal(0) : ordersDB.getWinprice()
                    );
                } else {
                    tableMapper.updateOrder(
                            ordersDB.getId(),
                            ordersDB.getComment(),
                            ordersDB.getId_product(),
                            ordersDB.getProduct_category(),
                            ordersDB.getTender(),
                            ordersDB.getNumber(),
                            ordersDB.getPrice() == null ? new BigDecimal(0) : ordersDB.getPrice(),
                            ordersDB.getWinprice() == null ? new BigDecimal(0) : ordersDB.getWinprice()
                    );
                }
            }
        }
        for(Long id : ordersINDB){
            tableMapper.deleteOrder(id);

        }
        List<OrdersDB> ordersDB = tableMapper.findAllOrdersBDbyTender(json.get(0).getTender());
        if(ordersDB != null) {
            List<Orders> orders = new LinkedList<Orders>();
            StringBuilder product = new StringBuilder();
            for (OrdersDB orderDB : ordersDB) {
                Long id = orderDB.getProduct_category();
                Product product_id;
                if(id == 1){
                    //анализатор спектра
                    product_id = tableMapper.findOneSpectrum_analyserById(orderDB.getId_product());

                    if (product_id.getId().equals(122L)){
                        product_id = null;
                    }
                }
                else if (id == 2){
                    //генератор сигналов
                    product_id = tableMapper.findOneSignalGeneratorById(orderDB.getId_product());

                    if (product_id.getId().equals(269L)){
                        product_id = null;
                    }
                }
                else if (id == 3){
                    //генератор импульсов
                    product_id = tableMapper.findOnePulseGeneratorById(orderDB.getId_product());

                    if (product_id.getId().equals(30L)){
                        product_id = null;
                    }
                }
                else if (id == 4){
                    //анализатор сигналов
                    product_id = tableMapper.findOneSignalAnalyzerById(orderDB.getId_product());

                    if (product_id.getId().equals( 122L)){
                        product_id = null;
                    }
                }
                else if (id == 5){
                    //анализатор цепей
                    product_id = tableMapper.findOneNetworkAnalyzersById(orderDB.getId_product());

                    if (product_id.getId().equals( 56L)){
                        product_id = null;
                    }
                }
                else if (id == 6){
                    //осциллограф
                    product_id = tableMapper.findOneOscilloscopeById(orderDB.getId_product());

                    if (product_id.getId().equals(506L)){

                        product_id = null;
                    }
                }
                else{
                    //Продукты
                    product_id = tableMapper.findOneAnotherProductById(orderDB.getId_product());
                }
                orderDB.setVendor(product_id == null ? Long.valueOf(1) : product_id.getVendor_id());
                orders.add(new Orders(orderDB.getTender(),
                        (orderDB.getProduct_category() != 7 ? tableMapper.findOneCategoryById(orderDB.getProduct_category()) : ""),
                         product_id == null ? "" : product_id.getVendor_code() ,
                        product_id == null || product_id.getVendor_id() == null || product_id.getVendor_id() == 1? "" : tableMapper.findOneVendorById(product_id.getVendor_id())  + ' ',
                        orderDB.getComment(),
                        orderDB.getNumber(),
                        orderDB.getPrice(),
                        orderDB.getWinprice()));
            }
            for(Orders order : orders){
                product.append(order.ToDB()).append("; ");
            }
            tableMapper.UpdateProduct(product.toString(), json.get(0).getTender());
        }
        return "good";
    }
    @GetMapping("/CountTenderWithoutOrders")
    @ResponseBody
    Long findCountTenderWithoutOrders(){
        return tableMapper.findCountTenderWithoutOrders();
    }

    @GetMapping("/TenderWithoutOrders")
    @ResponseBody
    List<Tender> findTenderWithoutOrders(){
        return tableMapper.findTenderWithoutOrders();
    }

    @GetMapping("/TendernoDocumentation")
    @ResponseBody
    List<Tender> findTendernoDocumentation(){
        return tableMapper.findTendernoDocumentation();
    }

    @RequestMapping( path = "/quarterTender/{category}")
    @ResponseBody public ArrayList<ReportQuarter> getQuartalTenderReport (@PathVariable Long category){
        int year = ZonedDateTime.now().getYear();
        int quartal = ZonedDateTime.now().get(IsoFields.QUARTER_OF_YEAR);
        int y = year - 2;
        int q = quartal;
        ArrayList<ReportQuarter> reportQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {
                        System.out.println(y);
            System.out.println(q);
            reportQuarters.add(0,tableMapper.findForOrders(y, q, category));

            if (q == 4) {
                q = 1;
                y = y + 1;
            } else {
                q = q + 1;
            }
        }

        return reportQuarters;
    }
    @RequestMapping( path = "/quarterVendor/{category}")
    @ResponseBody public ArrayList<ReportVendorQuarter>getQuartalVendorReport (@PathVariable Long category){
        int year = ZonedDateTime.now().getYear();
        int quartal = ZonedDateTime.now().get(IsoFields.QUARTER_OF_YEAR);
        int y = year - 2;
        int q = quartal;

        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {
            Map<String,Integer> vendorCount = new HashMap<String,Integer>();
            System.out.println(y);
            System.out.println(q);
            List<String> vendors = tableMapper.findVendorForOrders(y,q,category,category_en);
            for (String vendor : vendors) {
                if(vendor.equals("No vendor")){}
                else if (!vendorCount.containsKey(vendor)) {
                    vendorCount.put(vendor, 1);
                } else {
                    vendorCount.put(vendor, vendorCount.get(vendor) + 1);
                }
            }
            if(reportVendorQuarters.isEmpty()){
                for(Map.Entry<String,Integer> entry: vendorCount.entrySet()){
                    ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                    reportVendorQuarter.getQuarter().put(String.valueOf(y) + ' '+ String.valueOf(q),entry.getValue());
                    reportVendorQuarters.add(reportVendorQuarter);

                }
            }
            else{

                for(Map.Entry<String,Integer> entry: vendorCount.entrySet()){
                    boolean flag = false;
                    for(ReportVendorQuarter reportVendorQuarter : reportVendorQuarters){
                        if(reportVendorQuarter.getVendor().equals(entry.getKey())){
                            reportVendorQuarter.getQuarter().put(String.valueOf(y) + ' '+ String.valueOf(q),entry.getValue());
                            flag = true;
                        }

                    }
                    if(!flag){
                        ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                        reportVendorQuarter.getQuarter().put(String.valueOf(y) + ' '+ String.valueOf(q),entry.getValue());
                        reportVendorQuarters.add(reportVendorQuarter);
                    }
                }
            }
            if (q == 4) {
                q = 1;
                y = y + 1;
            } else {
                q = q + 1;
            }
        }

        return reportVendorQuarters;
    }
    @RequestMapping( path = "/quarterNoVendor/{category}")
    @ResponseBody public ArrayList<ReportVendorQuarter>getQuartalNoVendorReport (@PathVariable Long category){
        int year = ZonedDateTime.now().getYear();
        int quartal = ZonedDateTime.now().get(IsoFields.QUARTER_OF_YEAR);
        int y = year - 2;
        int q = quartal;

        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {
            Map<String,Integer> vendorCount = new HashMap<String,Integer>();
            System.out.println(y);
            System.out.println(q);
            List<String> vendors = tableMapper.findNoVendorForOrders(y,q,category,category_en);
            for (String vendor : vendors) {
                if (!vendorCount.containsKey(vendor)) {
                    vendorCount.put(vendor, 1);
                } else {
                    vendorCount.put(vendor, vendorCount.get(vendor) + 1);
                }
            }
            if(reportVendorQuarters.isEmpty()){
                for(Map.Entry<String,Integer> entry: vendorCount.entrySet()){
                    ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                    reportVendorQuarter.getQuarter().put(String.valueOf(y) + ' '+ String.valueOf(q),entry.getValue());
                    reportVendorQuarters.add(reportVendorQuarter);

                }
            }
            else{

                for(Map.Entry<String,Integer> entry: vendorCount.entrySet()){
                    boolean flag = false;
                    for(ReportVendorQuarter reportVendorQuarter : reportVendorQuarters){
                        if(reportVendorQuarter.getVendor().equals(entry.getKey())){
                            reportVendorQuarter.getQuarter().put(String.valueOf(y) + ' '+ String.valueOf(q),entry.getValue());
                            flag = true;
                        }

                    }
                    if(!flag){
                        ReportVendorQuarter reportVendorQuarter = new ReportVendorQuarter(entry.getKey());
                        reportVendorQuarter.getQuarter().put(String.valueOf(y) + ' '+ String.valueOf(q),entry.getValue());
                        reportVendorQuarters.add(reportVendorQuarter);
                    }
                }
            }
            if (q == 4) {
                q = 1;
                y = y + 1;
            } else {
                q = q + 1;
            }
        }

        return reportVendorQuarters;
    }
    @PostMapping("/saveProduct/{category}")
    @ResponseBody
    String saveProduct(@RequestBody Product product, @PathVariable Long category ){
        if(product.getId() != null){
        }
        System.out.println(product.toString());
        System.out.println(category);
        return "good";
    }
    @PostMapping("/saveTender")
    @ResponseBody
    String saveTender(@RequestBody Tender tender){
        tableMapper.UpdateSum(tender.getPrice(), tender.getId());
        return "good";
    }
}
