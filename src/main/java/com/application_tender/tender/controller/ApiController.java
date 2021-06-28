package com.application_tender.tender.controller;


import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.service.FileService;
import com.application_tender.tender.service.MailSender;
import com.application_tender.tender.subsidiaryModels.*;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/demo")
public class ApiController {
    private final DateTimeFormatter format_date= DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
    private final DateTimeFormatter format_dateFile= DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private  final TableMapper tableMapper;
    @Autowired
    private SearchAtribut searchAtribut;
    @Autowired
    private GetCurrency getCurrency;
    @Value("${file.pathname}")
    private String pathname;
    private final FileService fileService;
    public ApiController(TableMapper tableMapper, FileService fileService) {
        this.tableMapper = tableMapper;
        this.fileService = fileService;
    }
    @GetMapping("/AllUsers")
    @ResponseBody
    List<User> allUsers(){
        return tableMapper.findAllUsers();
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
       return searchAtribut.findTenderByTerms(json);
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
    List<Tender> addTender(MultipartFile excel) throws IOException, InvalidFormatException {

        LinkedList<Tender> tenders = new LinkedList<>();
        File temp = new File(pathname);
        DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        excel.transferTo(temp);
        //InputStream ExcelFileToRead= new InputStreamReader(new FileInputStream(temp), "UTF-8");
        XSSFWorkbook workbook = new XSSFWorkbook(temp);
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

                String INNCustomer = new DataFormatter().formatCellValue(row.getCell(3)).trim();
                System.out.println(INNCustomer);
                ZonedDateTime dateStart = ZonedDateTime.parse(row.getCell(8).getStringCellValue() + " 00:00:00 Z", format_date).plusDays(1);
                currency = getCurrency.currency(dateStart.format(formatCurrency));
                double rate = row.getCell(5).getStringCellValue().equals("RUB")  ? 1 : currency.get(row.getCell(5).getStringCellValue());
                id = tableMapper.insertTender(row.getCell(0).getStringCellValue(),
                        "https://www.bicotender.ru/tc/tender/show/tender_id/" + numberTender,
                        row.getCell(1).getStringCellValue(),
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

        //ExcelFileToRead.close();

        return tenders;
    }

    @GetMapping("/Vendor")
    @ResponseBody
    List<Vendor> Vendor(){
        return tableMapper.findAllVendor();
    }

    @PostMapping("/addOrders")
    @ResponseBody
    ResponseEntity<?> Tender(@RequestBody List<OrdersDB> json){
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
        return ResponseEntity.ok("good");
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
        int q = 1;
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
        int q = 1;

        ArrayList<ReportVendorQuarter> reportVendorQuarters = new ArrayList<>();
        String category_en = tableMapper.findOneCategoryENById(category);

        while (y != year || q != quartal+1) {
            Map<String,Integer> vendorCount = new HashMap<String,Integer>();
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
        int y = 2018;
        int q = 1;

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
    ResponseEntity saveTender(@RequestBody Tender tender){
        tableMapper.UpdateTender(tender.getId(), tender.getName_tender(),tender.getBico_tender(),tender.getGos_zakupki(),tender.getDate_start(),tender.getDate_finish(),tender.getDate_tranding(),tender.getNumber_tender(),tender.getFull_sum(),tender.getWin_sum(),tender.getCurrency(),tender.getPrice(),tender.getRate(),tender.getSum(),Long.valueOf(tender.getCustomer()),Long.valueOf(tender.getTypetender()),Long.valueOf(tender.getWinner()),tender.isDublicate());
        return ResponseEntity.ok("good");
    }
    @RequestMapping( path = "/Test")
    @ResponseBody
        ResponseEntity Test () throws IOException, InvalidFormatException {


        return ResponseEntity.ok().build();
    }
    @PostMapping("/TenderOnProduct")
    @ResponseBody
    List<Tender> TenderOnProduct( @RequestBody TenderProduct json ) {
        return tableMapper.TenderOnProduct(json.getProductCategory(), json.getDateStart(),json.getDateFinish(), json.getProduct());
    }
    @PostMapping("/insertWinner")
    @ResponseBody
    ResponseEntity insertWinner(@RequestBody Winner winner){
        System.out.println(winner);
        if (winner.getId() == null){
            tableMapper.insertWinner(winner.getInn(),winner.getName());
        }
        else {
            tableMapper.updateWinner(winner.getId(), winner.getInn(),winner.getName());
        }
        return ResponseEntity.ok().build();
    }
    @PostMapping("/insertCustomer")
    @ResponseBody
    String insertCustomer(@RequestBody Customer customer){
        if(customer.getId() == null){
            tableMapper.insertCustomer(customer.getInn(),customer.getName(),Long.valueOf(customer.getCountry()) );
        }
        else{
            tableMapper.updateCustomer(customer.getId(),customer.getInn(),customer.getName(),Long.valueOf(customer.getCountry()));
        }
        return "good";
    }
    @GetMapping("/Country")
    @ResponseBody
    List<Country> Country(){
        return tableMapper.findAllCountry();
    }
    @GetMapping("/DeleteTender/{tender}")
    @ResponseBody
    String DeleteTender(@PathVariable Long tender){
        System.out.println(tender);
        tableMapper.DeleteTender(tender);
        return "good";
    }


    @PostMapping("/File")
    @ResponseBody
    ResponseEntity<Resource> downloadFile(@RequestBody ReceivedJSON json) throws IOException {
        List<Tender> tenders = searchAtribut.findTenderByTerms(json);
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet = workbook.createSheet("Станица");
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

        XSSFCellStyle hlinkstyle = workbook.createCellStyle();
        XSSFFont hlinkfont = workbook.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(new XSSFColor(new java.awt.Color(30,144,255)));
        hlinkstyle.setFont(hlinkfont);
        hlinkstyle.setWrapText(true);
        hlinkstyle.setBorderTop(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
        hlinkstyle.setBorderRight(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
        hlinkstyle.setBorderBottom(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
        hlinkstyle.setBorderLeft(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);

        XSSFCellStyle body = workbook.createCellStyle();
        body.setBorderTop(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
        body.setBorderRight(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
        body.setBorderBottom(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
        body.setBorderLeft(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);
        body.setWrapText(true);

        XSSFCellStyle price = workbook.createCellStyle();
        price.setBorderTop(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
        price.setBorderRight(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
        price.setBorderBottom(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
        price.setBorderLeft(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);
        price.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new  XSSFColor(new java.awt.Color(0,102,204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255,255,255)));
        header.setFont(headerFont);

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFDataFormat dateFormat = (XSSFDataFormat)workbook.createDataFormat();
        cellStyle.setDataFormat(dateFormat.getFormat("dd.MM.yyyy HH:mm:ss"));
//       cellStyle.setDataFormat(
//               createHelper.createDataFormat().getFormat("dd.MM.yyyy HH:mm:ss"));
       // cellStyle.setWrapText(true);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);
        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0,39*256);
        sheet.setColumnWidth(1,14*256);
        sheet.setColumnWidth(2,14*256);
        sheet.setColumnWidth(3,50*256);
        sheet.setColumnWidth(4,39*256);
        sheet.setColumnWidth(5,13*256);
        sheet.setColumnWidth(6,14*256);
        sheet.setColumnWidth(7,17*256);
        sheet.setColumnWidth(8,5*256);
        sheet.setColumnWidth(9,5*256);
        sheet.setColumnWidth(10,20*256);
        sheet.setColumnWidth(11,20*256);
        sheet.setColumnWidth(12,12*256);
        sheet.setColumnWidth(13,12*256);
        sheet.setColumnWidth(14,12*256);
        sheet.setColumnWidth(15,56*256);
        sheet.setColumnWidth(16,39*256);
        sheet.setColumnWidth(17,20*256);
        row.createCell(0).setCellValue("Заказчик");
        row.getCell(0).setCellStyle(header);
        row.createCell(1).setCellValue("ИН");
        row.getCell(1).setCellStyle(header);
        row.createCell(2).setCellValue("Страна");
        row.getCell(2).setCellStyle(header);
        row.createCell(3).setCellValue("Название");
        row.getCell(3).setCellStyle(header);
        row.createCell(4).setCellValue("Госзакупки");
        row.getCell(4).setCellStyle(header);
        row.createCell(5).setCellValue("Тип тендера");
        row.getCell(5).setCellStyle(header);
        row.createCell(6).setCellValue("Номер");
        row.getCell(6).setCellStyle(header);
        row.createCell(7).setCellValue("Цена");
        row.getCell(7).setCellStyle(header);
        row.createCell(8).setCellValue("Валюта");
        row.getCell(8).setCellStyle(header);
        row.createCell(9).setCellValue("Курс");
        row.getCell(9).setCellStyle(header);
        row.createCell(10).setCellValue("Сумма");
        row.getCell(10).setCellStyle(header);
        row.createCell(11).setCellValue("Полная сумма");
        row.getCell(11).setCellStyle(header);
        row.createCell(12).setCellValue("Начало показа");
        row.getCell(12).setCellStyle(header);
        row.createCell(13).setCellValue("Окончание показа");
        row.getCell(13).setCellStyle(header);
        row.createCell(14).setCellValue("Дата торгов");
        row.getCell(14).setCellStyle(header);
        row.createCell(15).setCellValue("Продукты");
        row.getCell(15).setCellStyle(header);
        row.createCell(16).setCellValue("Победитель");
        row.getCell(16).setCellStyle(header);
        row.createCell(17).setCellValue("Сумма победителя");
        row.getCell(17).setCellStyle(header);
        for(Tender tender: tenders){
            numberRow+=1;
            row = sheet.createRow(numberRow);
            row.setHeight((short) -1);
            row.createCell(0).setCellValue(tender.getCustomer());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(tender.getInn());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(tender.getCountry());
            row.getCell(2).setCellStyle(body);

            row.createCell(3).setCellValue(tender.getName_tender());
            XSSFHyperlink link= (XSSFHyperlink)createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(tender.getBico_tender());row.getCell(0).setCellStyle(body);
            row.getCell(3).setHyperlink((XSSFHyperlink) link);
            row.getCell(3).setCellStyle(hlinkstyle);
            XSSFHyperlink linkGos= (XSSFHyperlink)createHelper.createHyperlink(HyperlinkType.URL);
            linkGos.setAddress(tender.getGos_zakupki());
            row.createCell(4).setHyperlink((XSSFHyperlink) linkGos);
            row.getCell(4).setCellValue(tender.getGos_zakupki());
            row.getCell(4).setCellStyle(hlinkstyle);
            row.createCell(5).setCellValue(tender.getTypetender());
            row.getCell(5).setCellStyle(body);
            row.createCell(6).setCellValue(tender.getNumber_tender());
            row.getCell(6).setCellStyle(body);
            row.createCell(7).setCellValue(tender.getPrice().doubleValue());
            row.getCell(7).setCellStyle(price);
            row.getCell(7).setCellType(CellType.NUMERIC);
            row.createCell(8).setCellValue(tender.getCurrency());
            row.getCell(8).setCellStyle(body);
            row.createCell(9).setCellValue(tender.getRate());
            row.getCell(9).setCellStyle(body);
            row.getCell(9).setCellType(CellType.NUMERIC);
            row.createCell(10).setCellValue(tender.getSum().doubleValue());
            row.getCell(10).setCellStyle(price);
            row.getCell(10).setCellType(CellType.NUMERIC);
            row.createCell(11).setCellValue(tender.getFull_sum().doubleValue());
            row.getCell(11).setCellStyle(price);
            row.getCell(11).setCellType(CellType.NUMERIC);
            row.createCell(12).setCellStyle(body);

            row.getCell(12).setCellValue(tender.getDate_start().toLocalDateTime().format(format_dateFile));

            row.createCell(13).setCellValue(tender.getDate_finish().toLocalDateTime().format(format_dateFile));
            row.getCell(13).setCellStyle(body);

            if(tender.getDate_tranding() != null){
                row.createCell(14).setCellValue( tender.getDate_tranding().toLocalDateTime().format(format_dateFile));
                row.getCell(14).setCellStyle(body);

            }
            else{
                row.createCell(14).setCellValue( "");
                row.getCell(14).setCellStyle(body);
            }
            row.createCell(15).setCellValue(tender.getProduct());
            row.getCell(15).setCellStyle(body);
            row.createCell(16).setCellValue(tender.getWinner());
            row.getCell(16).setCellStyle(body);
            row.createCell(17).setCellValue(tender.getWin_sum().doubleValue());
            row.getCell(17).setCellStyle(price);
            row.getCell(17).setCellType(CellType.NUMERIC);
        }
        File file = new File(pathname);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        Resource file1 = fileService.download("temp.xlsx");
        Path path = file1.getFile()
                .toPath();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file1.getFilename() + "\"")
                .body(file1);
    }

}

