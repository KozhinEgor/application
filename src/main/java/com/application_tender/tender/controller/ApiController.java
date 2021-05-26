package com.application_tender.tender.controller;


import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
    @Autowired
    private GetCurrency getCurrency;
    @Value("${file.pathname}")
    private String pathname;
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
            //анализатор цепей
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
        File temp = new File(pathname);
        DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        excel.transferTo(temp);
        InputStream ExcelFileToRead = new FileInputStream(temp);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = workbook.getSheetAt(0);

        ZonedDateTime dateCurrency = ZonedDateTime.parse(sheet.getRow(1).getCell(8).getStringCellValue() + " 00:00:00 Z", format_date).plusDays(1);
        Map<String,Double> currency = getCurrency.currency(dateCurrency.format(formatCurrency));
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

            }
            else{

                String INNCustomer = new DataFormatter().formatCellValue(row.getCell(3));
                ZonedDateTime dateStart = ZonedDateTime.parse(row.getCell(8).getStringCellValue() + " 00:00:00 Z", format_date).plusDays(1);
                currency = getCurrency.currency(dateStart.format(formatCurrency));
                double rate = row.getCell(5).getStringCellValue().equals("RUB")  ? 1 : currency.get(row.getCell(5).getStringCellValue());
                id = tableMapper.insertTender(row.getCell(0).getStringCellValue(),
                        "https://www.bicotender.ru/tc/tender/show/tender_id/" + numberTender,
                        row.getCell(1).getHyperlink().getAddress(),
                        ZonedDateTime.parse(row.getCell(8).getStringCellValue() + " 00:00:00 Z", format_date),
                        row.getCell(9).getStringCellValue().length() == 10 ?  ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " 00:00:00 Z", format_date) :
                                ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " Z", format_date),
                        row.getCell(10).getCellType() != CellType.BLANK ?
                                    row.getCell(9).getStringCellValue().length() == 10 ?  ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " 00:00:00 Z", format_date) :
                                    ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " Z", format_date):
                                null,
                        numberTender,
                        new BigDecimal(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING),
                        new BigDecimal(0),
                        row.getCell(5).getStringCellValue(),
                        new BigDecimal(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING),
                        rate,
                        new BigDecimal(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING).multiply(new BigDecimal(rate)),
                        searchAtribut.findCustomer(INNCustomer, sheet.getRow(count).getCell(2).getStringCellValue()),
                        searchAtribut.findTypetender(row.getCell(6).getStringCellValue()),
                        1L
                        );
                id = tableMapper.findTenderByNumber_tender(numberTender);

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
        int y = 2018;
        int q = 3;
        ArrayList<ReportQuarter> reportQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {

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
        int y = 2018;
        int q = 3;

        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {
            Map<String,Integer> vendorCount = new HashMap<String,Integer>();
            System.out.println(y+" "+q);
            List<String> vendors = tableMapper.findVendorForOrders(y,q,category,category_en);
            for (String vendor : vendors) {
                System.out.println(vendor);
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
        int y = 2018;
        int q = 3;

        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {
            Map<String,Integer> vendorCount = new HashMap<String,Integer>();

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
    List<Product> saveProduct(@RequestBody Product product, @PathVariable Long category ){
        if(category == 1L){
            //анализатор спектра\
            Long a = product.getId() == null?
                    tableMapper.InsertSpectrum_analyser(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getPortable(),product.getUsb()) :
                    tableMapper.UpdateSpectrum_analyser(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId(),product.getPortable(),product.getUsb()) ;
            return tableMapper.findAllSpectrum_analyserToProduct();
        }
        else if (category == 2L){
            //генератор сигналов
            Long a = product.getId() == null?
                    tableMapper.InsertSignalGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id()) :
                    tableMapper.UpdateSignalGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId()) ;
            return tableMapper.findAllSignalGeneratorToProduct();
        }
        else if (category == 3L){
            //генератор импульсов
            Long a = product.getId() == null?
                    tableMapper.InsertPulseGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id()) :
                    tableMapper.UpdatePulseGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId()) ;
            return tableMapper.findAllPulseGeneratorToProduct();
        }
        else if (category == 4L){
            //анализатор сигналов
            Long a = product.getId() == null?
            tableMapper.InsertSignalAnalyzer(product.getVendor_code(),product.getFrequency(),product.getVendor_id()) :
            tableMapper.UpdateSignalAnalyzer(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId()) ;
            return tableMapper.findAllSignalAnalyzerToProduct();
        }
        else if (category == 5L){
            //анализатор цепей
            Long a = product.getId() == null?
            tableMapper.InsertNetworkAnalyzers(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getUsb()) :
            tableMapper.UpdateNetworkAnalyzers(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId(),product.getUsb()) ;
            return tableMapper.findAllNetworkAnalyzersToProduct();
        }
        else if (category == 6L){
            //осциллограф
            Long a = product.getId() == null?
                    tableMapper.InsertOscilloscope(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getVxi(),product.getUsb(),product.getChannel()) :
                    tableMapper.UpdateOscilloscope(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId(),product.getVxi(),product.getUsb(),product.getChannel()) ;
            return tableMapper.findAllOscilloscopeToProduct();
        }
        else{
            //Продукты
            Long a = product.getId() == null?
                    tableMapper.InsertAnotherProduct(product.getVendor_code()) :
                    tableMapper.UpdateAnotherProduct(product.getVendor_code(),product.getId()) ;
            return tableMapper.findAllAnotherProductToProduct();
        }

    }
    @PostMapping("/saveTender")
    @ResponseBody
    String saveTender(@RequestBody Tender tender){
        tableMapper.UpdateSum(tender.getPrice(), tender.getPrice().multiply(new BigDecimal(tender.getRate())), tender.getId());
        return "good";
    }
    @RequestMapping( path = "/Test")
    @ResponseBody  String Test () throws IOException {
        for (long i = 6480L;i<6577L;i++){
            Tender tender = tableMapper.findTenderbyId(i);
            tender.setDate_start(tender.getDate_start().plusHours(3));
            tender.setDate_finish(tender.getDate_finish().plusHours(3));
            tableMapper.UpdateDate(tender.getId(),tender.getDate_start(),tender.getDate_finish());
        }
        return "good";
    }
    @PostMapping("/TenderOnProduct")
    @ResponseBody
    List<Tender> TenderOnProduct( @RequestBody TenderProduct json ) {
        return tableMapper.TenderOnProduct(json.getProductCategory(), json.getDateStart(),json.getDateFinish(), json.getProduct());
    }
}
