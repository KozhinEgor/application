package com.application_tender.tender.controller;


import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.service.FileService;
import com.application_tender.tender.service.ReportService;
import com.application_tender.tender.subsidiaryModels.*;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
    @Value("${file.log}")
    private String log;
    private final FileService fileService;
    private final ReportService reportService;

    public ApiController(TableMapper tableMapper, FileService fileService, ReportService reportService) {
        this.tableMapper = tableMapper;
        this.fileService = fileService;
        this.reportService = reportService;

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
    List<Company> customer(){
        return tableMapper.findAllCustomer();
    }

    @GetMapping("/CustomerNoUses")
    @ResponseBody
    List<Company> customerNoUses(){
        return tableMapper.findAllCustomerNoUses();
    }

    @GetMapping("/DeleteCustomerNoUses")
    @ResponseBody
    List<Company> deleteCustomerNoUses(){

        List<Company> companies = tableMapper.findAllCustomerNoUses();
        for(Company company:companies){
            tableMapper.deleteCustomer(company.getId());
        }
        return tableMapper.findAllCustomerNoUses();
    }

    @PostMapping("/insertCustomer")
    @ResponseBody
    ResponseEntity insertCustomer(@RequestBody Company customer){
        if(customer.getId() == null){
            tableMapper.insertCustomer(customer.getInn(),customer.getName(),Long.valueOf(customer.getCountry()) );
        }
        else{
            tableMapper.updateCustomer(customer.getId(),customer.getInn(),customer.getName(),Long.valueOf(customer.getCountry()));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ChangeCustomer")
    @ResponseBody
    Map<String,String> changeCustomer(@RequestBody ChangeCompany changeCompany){
        List<Long> tender = tableMapper.findTenderByCustomer(changeCompany.getCompany());
        for(Long t: tender){
            tableMapper.changeCustomer(t,changeCompany.getNewCompany());
        }
        HashMap<String,String> a = new HashMap<>();
        a.put("name","Заменил " + tableMapper.findCustomerNameById(changeCompany.getCompany()) + " на " + tableMapper.findCustomerNameById(changeCompany.getNewCompany()));
        return a;
    }

    @GetMapping("/CustomerFile")
    @ResponseBody
    ResponseEntity<Resource> customerFile() throws IOException {
        List<Company> customer = tableMapper.findAllCustomer();
        List<Company> customerNoUses = tableMapper.findAllCustomerNoUses();
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet = workbook.createSheet("Заказчики");
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

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

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new  XSSFColor(new java.awt.Color(0,102,204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255,255,255)));
        header.setFont(headerFont);
        header.setWrapText(true);

        XSSFCellStyle dublicate = workbook.createCellStyle();
        dublicate.setBorderTop(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
        dublicate.setBorderRight(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
        dublicate.setBorderBottom(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
        dublicate.setBorderLeft(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);
        dublicate.setWrapText(true);
        dublicate.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 215, 64)));
        dublicate.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0,5*256);
        sheet.setColumnWidth(1,39*256);
        sheet.setColumnWidth(2,14*256);
        sheet.setColumnWidth(3,14*256);

        row.createCell(0).setCellValue("Id");
        row.getCell(0).setCellStyle(header);
        row.createCell(1).setCellValue("Заказчик");
        row.getCell(1).setCellStyle(header);
        row.createCell(2).setCellValue("ИНН/БИН/УНП");
        row.getCell(2).setCellStyle(header);
        row.createCell(3).setCellValue("Страна");
        row.getCell(3).setCellStyle(header);

        numberRow++;
        for (Company company: customer){
            row = sheet.createRow(numberRow);
            row.createCell(0).setCellValue(company.getId());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(company.getName());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(company.getInn());
            row.getCell(2).setCellStyle(body);
            row.createCell(3).setCellValue(company.getCountry());
            row.getCell(3).setCellStyle(body);
            if(customerNoUses.contains(company)){
                row.getCell(0).setCellStyle(dublicate);
            }
            numberRow++;
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

    @GetMapping("/Winner")
    @ResponseBody
    List<Company> winner(){
        return tableMapper.findAllWinner();
    }

    @GetMapping("/WinnerNoUses")
    @ResponseBody
    List<Company> winnerNoUses(){
        return tableMapper.findAllWinnerNoUses();
    }

    @GetMapping("/DeleteWinnerNoUses")
    @ResponseBody
    List<Company> deleteWinnerNoUses(){
        List<Company> companies = tableMapper.findAllWinnerNoUses();
        for(Company company:companies){
            tableMapper.deleteWinner(company.getId());
        }
        return tableMapper.findAllWinnerNoUses();
    }

    @PostMapping("/insertWinner")
    @ResponseBody
    ResponseEntity insertWinner(@RequestBody Company winner){

        if (winner.getId() == null){
            tableMapper.insertWinner(winner.getInn(),winner.getName(),Long.valueOf(winner.getCountry()));
        }
        else {
            tableMapper.updateWinner(winner.getId(), winner.getInn(),winner.getName(),Long.valueOf(winner.getCountry()));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ChangeWinner")
    @ResponseBody
    Map<String,String> changeWinner(@RequestBody ChangeCompany changeCompany){
        List<Long> tender = tableMapper.findTenderByWinner(changeCompany.getCompany());
        for(Long t: tender){
            tableMapper.changeWinner(t,changeCompany.getNewCompany());
        }
        HashMap<String,String> a = new HashMap<>();
        a.put("name","Заменил " + tableMapper.findWinnerNameById(changeCompany.getCompany()) + " на " + tableMapper.findWinnerNameById(changeCompany.getNewCompany()));
        return a;
    }

    @GetMapping("/WinnerFile")
    @ResponseBody
    ResponseEntity<Resource> winnerFile() throws IOException {
        List<Company> winner = tableMapper.findAllWinner();
        List<Company> winnerNoUses = tableMapper.findAllWinnerNoUses();
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet = workbook.createSheet("Победители");
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

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

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new  XSSFColor(new java.awt.Color(0,102,204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255,255,255)));
        header.setFont(headerFont);
        header.setWrapText(true);

        XSSFCellStyle dublicate = workbook.createCellStyle();
        dublicate.setBorderTop(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
        dublicate.setBorderRight(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
        dublicate.setBorderBottom(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
        dublicate.setBorderLeft(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);
        dublicate.setWrapText(true);
        dublicate.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 215, 64)));
        dublicate.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0,5*256);
        sheet.setColumnWidth(1,39*256);
        sheet.setColumnWidth(2,14*256);
        sheet.setColumnWidth(3,14*256);

        row.createCell(0).setCellValue("Id");
        row.getCell(0).setCellStyle(header);
        row.createCell(1).setCellValue("Победитель");
        row.getCell(1).setCellStyle(header);
        row.createCell(2).setCellValue("ИНН/БИН/УНП");
        row.getCell(2).setCellStyle(header);
        row.createCell(3).setCellValue("Страна");
        row.getCell(3).setCellStyle(header);

        numberRow++;
        for (Company company: winner){
            row = sheet.createRow(numberRow);
            row.createCell(0).setCellValue(company.getId());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(company.getName());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(company.getInn());
            row.getCell(2).setCellStyle(body);
            row.createCell(3).setCellValue(company.getCountry());
            row.getCell(3).setCellStyle(body);
            if(winnerNoUses.contains(company)){
                row.getCell(0).setCellStyle(dublicate);
            }
            numberRow++;
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
    List<Product> Product(@PathVariable Long id){
        if(id == 0L){
            return null;
        }
        else{
            return tableMapper.findListProduct(searchAtribut.createSelectProductCategory(id));
        }
    }

    @GetMapping("/VendorCodeNoUses/{id}")
    @ResponseBody
    List<Product> ProductNoUses(@PathVariable Long id){

        return tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(id));

    }

    @GetMapping("/DeleteVendorCodeNoUses/{id}")
    @ResponseBody
    List<Product> DeleteProductNoUses(@PathVariable Long id){
        List<Product> products = tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(id));
        for(Product product : products){
            tableMapper.DeleteProduct(tableMapper.findNameCategoryById(id),product.getId());
        }
        return tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(id));

    }

    @GetMapping("/VendorCodeById/{id}/{id_product}")
    @ResponseBody
    Product ProductById(@PathVariable Long id,@PathVariable Long id_product){
        String select = searchAtribut.createSelectProductCategory(id);
        int index = select.indexOf("order by");
        select= select.substring(0,index-1)+ " where pr.id =" + String.valueOf(id_product) + " "+select.substring(index);
        return tableMapper.findOneProduct(select);

    }

    @GetMapping("/OrdersByTender/{tender}")
    @ResponseBody
    OrdersReceived OrdersByTender(@PathVariable Long tender){
        List<OrdersDB> ordersDB = tableMapper.findAllOrdersBDbyTender(tender);
        List<Orders> orders = new LinkedList<>();
        for (OrdersDB orderDB : ordersDB ){

            Product product_id = searchAtribut.ProductToOrders(orderDB.getProduct_category(),orderDB.getId_product());

            orders.add(new Orders(orderDB.getTender(),
                    tableMapper.findOneCategoryById(orderDB.getProduct_category()),
                    ((product_id == null || product_id.getVendor_id() == null || product_id.getVendor_id().equals(1L)) ? "" :
                            tableMapper.findOneVendorById(product_id.getVendor_id())  + ' ') + (product_id == null ? "": product_id.getVendor_code() + " "),
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

    @GetMapping("/Vendor/{category}")
    @ResponseBody
    List<Vendor> Vendor(@PathVariable Long category){
        if(category == 0L){
            return tableMapper.findAllVendor();
        }
        else{
            return tableMapper.findAllVendorByCategory(tableMapper.findNameCategoryById(category));
        }
    }

    @PostMapping("/addOrders")
    @ResponseBody
    HashMap<String,String> Tender(@RequestBody List<OrdersDB> json){
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
        StringBuilder product = new StringBuilder();
        if(ordersDB != null) {
            List<Orders> orders = new LinkedList<Orders>();

            for (OrdersDB orderDB : ordersDB) {

                Product product_id = searchAtribut.ProductToOrders(orderDB.getProduct_category(),orderDB.getId_product());

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
            tableMapper.UpdateProductTender(product.toString(), json.get(0).getTender());
        }
        HashMap<String,String> answear = new HashMap<>();
        answear.put("name",product.toString());
        return answear;
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
    @ResponseBody
    public ArrayList<ReportQuarter> getQuartalTenderReport (@PathVariable Long category,@RequestBody ReceivedJSON json){

        return reportService.getQuartalTenderReport(category,json);
    }

    @RequestMapping( path = "/quarterVendor/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter>getQuartalVendorReport (@PathVariable Long category,@RequestBody ReceivedJSON json){

        return reportService.getQuartalVendorReport(category,json);
    }

    @RequestMapping( path = "/quarterNoVendor/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalNoVendorReport (@PathVariable Long category,@RequestBody ReceivedJSON json){

        return reportService.getQuartalNoVendorReport(category,json);
    }

    @PostMapping("/FileReport")
    @ResponseBody
    ResponseEntity<Resource> downloadFileReport(@RequestBody ReceivedJSON json) throws IOException {
        List<ProductCategory> productCategories = tableMapper.findAllProductCategory();
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

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

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new  XSSFColor(new java.awt.Color(0,102,204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255,255,255)));
        header.setFont(headerFont);

        for (ProductCategory productCategory : productCategories) {
            ArrayList<String> colums = new ArrayList<>();
            int rowNumber = 1;
            if (!productCategory.getCategory().equals("Продукты")) {
                XSSFSheet sheet = workbook.createSheet(productCategory.getCategory().replace("/", " или "));
                XSSFRow row = sheet.createRow(rowNumber);
                row.createCell(1).setCellValue("Год");
                row.getCell(1).setCellStyle(header);
                row.createCell(2).setCellValue("Квартал");
                row.getCell(2).setCellStyle(header);
                row.createCell(3).setCellValue("Количество");
                row.getCell(3).setCellStyle(header);
                row.createCell(4).setCellValue("Сумма");
                row.getCell(4).setCellStyle(header);
                rowNumber++;
                ArrayList<ReportQuarter> reportQuarters = this.reportService.getQuartalTenderReport(productCategory.getId(), json);
                if (reportQuarters.size() != 0) {
                    for(ReportQuarter reportQuarter : reportQuarters){
                        row = sheet.createRow(rowNumber);
                        row.createCell(1).setCellValue((json.isQuarter()?"FY":"") +reportQuarter.getYear());
                        row.getCell(1).setCellStyle(body);
                        row.createCell(2).setCellValue("Q"+reportQuarter.getQuarter());
                        row.getCell(2).setCellStyle(body);
                        row.createCell(3).setCellValue(reportQuarter.getCount());
                        row.getCell(3).setCellStyle(body);
                        row.createCell(4).setCellValue(reportQuarter.getSum() != null?reportQuarter.getSum().toString(): "");
                        row.getCell(4).setCellStyle(body);
                        colums.add(reportQuarter.getYear() +" "+reportQuarter.getQuarter());
                        rowNumber++;
                    }
                }
                rowNumber = rowNumber + 2;//Чтобы две строки пропустить
                ArrayList<ReportVendorQuarter> reportVendorQuarters = this.reportService.getQuartalVendorReport(productCategory.getId(),json);
                if (reportVendorQuarters.size() != 0){
                    row = sheet.createRow(rowNumber);
                    row.createCell(1).setCellValue("Название");
                    row.getCell(1).setCellStyle(header);
                    for(int cell = 0;cell<colums.size();cell++){
                        row.createCell(cell+2).setCellValue((json.isQuarter()?"FY":"")  + colums.get(cell).replace(" ","Q"));
                        row.getCell(cell+2).setCellStyle(header);
                    }
                    rowNumber++;
                    for(ReportVendorQuarter reportVendorQuarter : reportVendorQuarters){
                        row = sheet.createRow(rowNumber);
                        row.createCell(1).setCellValue(reportVendorQuarter.getVendor());
                        row.getCell(1).setCellStyle(body);
                        for(int cell = 0;cell<colums.size();cell++){
                            row.createCell(cell+2).setCellStyle(body);
                            if(reportVendorQuarter.getQuarter().containsKey(colums.get(cell))){
                                row.getCell(cell+2).setCellValue(reportVendorQuarter.getQuarter().get(colums.get(cell)));
                            }
                        }
                        rowNumber++;
                    }
                }
                rowNumber = rowNumber + 2;//Чтобы две строки пропустить
                ArrayList<ReportVendorQuarter> reportNoVendorQuarters = this.reportService.getQuartalNoVendorReport(productCategory.getId(),json);
                if (reportVendorQuarters.size() != 0){
                    row = sheet.createRow(rowNumber);
                    row.createCell(1).setCellValue("Название");
                    row.getCell(1).setCellStyle(header);
                    for(int cell = 0;cell<colums.size();cell++){
                        row.createCell(cell+2).setCellValue((json.isQuarter()?"FY":"") + colums.get(cell).replace(" ","Q"));
                        row.getCell(cell+2).setCellStyle(header);
                    }
                    rowNumber++;
                    for(ReportVendorQuarter reportNoVendorQuarter : reportNoVendorQuarters){
                        row = sheet.createRow(rowNumber);
                        row.createCell(1).setCellValue(reportNoVendorQuarter.getVendor());
                        row.getCell(1).setCellStyle(body);
                        for(int cell = 0;cell<colums.size();cell++){
                            row.createCell(cell+2).setCellStyle(body);
                            if(reportNoVendorQuarter.getQuarter().containsKey(colums.get(cell))){
                                row.getCell(cell+2).setCellValue(reportNoVendorQuarter.getQuarter().get(colums.get(cell)));
                            }
                        }
                        rowNumber++;
                    }
                }
            }
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

    @PostMapping("/saveProduct/{id}")
    @ResponseBody
    List<Product> saveProduct(@RequestBody Product product, @PathVariable Long id ){
        String category = tableMapper.findNameCategoryById(id);
        String[] columns = tableMapper.findcolumnName(category);

        if(product.getId() == null){
            StringBuilder insert = new StringBuilder("Insert into " + category + " (");
//            Insert into oscilloscope (vendor_code, frequency ,vendor, vxi, usb, channel) values(#{vendor_code}, #{frequency},#{vendor}, #{vxi},#{usb}, #{channel})
            for(String column : columns){
                if(column.equals("id")){
                   continue;
                }
                insert.append(" ").append(column).append(",");
            }
            insert = new StringBuilder(insert.substring(0, insert.length() - 1) + ") values(");
            for(String column : columns){
                switch (column) {
                    case "id":
                        continue;
                    case "vendor":
                        insert.append("'").append(product.getVendor_id()).append("',");
                        break;
                    case "vendor_code":
                        insert.append("'").append(product.getVendor_code()).append("',");
                        break;
                    case "frequency":
                        insert.append("'").append(product.getFrequency()).append("',");
                        break;
                    case "usb":
                        insert.append("").append(product.getUsb()?1:0).append(",");
                        break;
                    case "vxi":
                        insert.append("").append(product.getVxi()?1:0).append(",");
                        break;
                    case "portable":
                        insert.append("").append(product.getPortable()?1:0).append(",");
                        break;
                    case "channel":
                        insert.append("'").append(product.getChannel()).append("',");
                        break;
                    case "port":
                        insert.append("'").append(product.getPort()).append("',");
                        break;
                }
            }
            insert = new StringBuilder(insert.substring(0, insert.length() - 1) + ")");
            tableMapper.InsertProduct(insert.toString());
            }
            else{
            StringBuilder update = new StringBuilder("Update " + category + " set ");
//            Update oscilloscope set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor}, vxi = #{portable}, usb= #{usb}, channel =#{channel} where id = #{id}
            for(String column : columns){
                switch (column) {
                    case "id":
                        continue;
                    case "vendor":
                        update.append(column).append("='").append(product.getVendor_id()).append("',");
                        break;
                    case "vendor_code":
                        update.append(column).append("='").append(product.getVendor_code()).append("',");
                        break;
                    case "frequency":
                        update.append(column).append("='").append(product.getFrequency()).append("',");
                        break;
                    case "usb":
                        update.append(column).append("=").append(product.getUsb() ?1:0).append(",");
                        break;
                    case "vxi":
                        update.append(column).append("=").append(product.getVxi()?1:0).append(",");
                        break;
                    case "portable":
                        update.append(column).append("=").append(product.getPortable()?1:0).append(",");
                        break;
                    case "channel":
                        update.append(column).append("='").append(product.getChannel()).append("',");
                        break;
                    case "port":
                        update.append(column).append("='").append(product.getPort()).append("',");
                        break;
                }

            }
            update = new StringBuilder(update.substring(0,update.length()-1)+ " where id = '"+product.getId()+"'");
            tableMapper.UpdateProduct(update.toString());
        }
        return tableMapper.findListProduct(searchAtribut.createSelectProductCategory(id));
//        if(category == 1L){
//            //анализатор спектра\
//            Long a = product.getId() == null?
//                    tableMapper.InsertSpectrum_analyser(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getPortable(),product.getUsb()) :
//                    tableMapper.UpdateSpectrum_analyser(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId(),product.getPortable(),product.getUsb()) ;
//            return tableMapper.findAllSpectrum_analyserToProduct();
//        }
//        else if (category == 2L){
//            //генератор сигналов
//            Long a = product.getId() == null?
//                    tableMapper.InsertSignalGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id()) :
//                    tableMapper.UpdateSignalGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId()) ;
//            return tableMapper.findAllSignalGeneratorToProduct();
//        }
//        else if (category == 3L){
//            //генератор импульсов
//            Long a = product.getId() == null?
//                    tableMapper.InsertPulseGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id()) :
//                    tableMapper.UpdatePulseGenerator(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId()) ;
//            return tableMapper.findAllPulseGeneratorToProduct();
//        }
//        else if (category == 4L){
//            //анализатор сигналов
//            Long a = product.getId() == null?
//            tableMapper.InsertSignalAnalyzer(product.getVendor_code(),product.getFrequency(),product.getVendor_id()) :
//            tableMapper.UpdateSignalAnalyzer(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId()) ;
//            return tableMapper.findAllSignalAnalyzerToProduct();
//        }
//        else if (category == 5L){
//            //анализатор цепей
//            Long a = product.getId() == null?
//            tableMapper.InsertNetworkAnalyzers(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getUsb()) :
//            tableMapper.UpdateNetworkAnalyzers(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId(),product.getUsb()) ;
//            return tableMapper.findAllNetworkAnalyzersToProduct();
//        }
//        else if (category == 6L){
//            //осциллограф
//            Long a = product.getId() == null?
//                    tableMapper.InsertOscilloscope(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getVxi(),product.getUsb(),product.getChannel()) :
//                    tableMapper.UpdateOscilloscope(product.getVendor_code(),product.getFrequency(),product.getVendor_id(),product.getId(),product.getVxi(),product.getUsb(),product.getChannel()) ;
//            return tableMapper.findAllOscilloscopeToProduct();
//        }
//        else{
//            //Продукты
//            Long a = product.getId() == null?
//                    tableMapper.InsertAnotherProduct(product.getVendor_code()) :
//                    tableMapper.UpdateAnotherProduct(product.getVendor_code(),product.getId()) ;
//            return tableMapper.findAllAnotherProductToProduct();
//        }

    }

    @PostMapping("/saveTender")
    @ResponseBody
    Tender saveTender(@RequestBody Tender tender){
        tableMapper.UpdateTender(tender.getId(), tender.getName_tender(),tender.getBico_tender(),tender.getGos_zakupki(),tender.getDate_start(),tender.getDate_finish(),tender.getDate_tranding(),tender.getNumber_tender(),tender.getFull_sum(),tender.getWin_sum(),tender.getCurrency(),tender.getPrice(),tender.getRate(),tender.getSum(),Long.valueOf(tender.getCustomer()),Long.valueOf(tender.getTypetender()),Long.valueOf(tender.getWinner()),tender.isDublicate());
        return tableMapper.findTenderbyId(tender.getId());
    }

    @PostMapping("/TenderOnProduct")
    @ResponseBody
    List<Tender> TenderOnProduct( @RequestBody TenderProduct json ) {
        return tableMapper.TenderOnProduct(json.getProductCategory(), json.getDateStart(),json.getDateFinish(), json.getProduct());
    }

    @GetMapping("/Country")
    @ResponseBody
    List<Country> Country(){
        return tableMapper.findAllCountry();
    }

    @GetMapping("/DeleteTender/{tender}")
    @ResponseBody
    Map<String,String> DeleteTender(@PathVariable Long tender){
        tableMapper.DeleteTender(tender);
        HashMap<String,String> a = new HashMap<>();
        a.put("name", "good");
        return a;
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

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new  XSSFColor(new java.awt.Color(0,102,204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255,255,255)));
        header.setFont(headerFont);

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

    @GetMapping("/ProductFile/{category}")
    @ResponseBody
    ResponseEntity<Resource> downloadProductFile(@PathVariable Long category) throws IOException {
        List<Product> products = tableMapper.findListProduct(searchAtribut.createSelectProductCategory(category));
        List<Product> productsNoUses = tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(category));
        if(products.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        else{
            String[] columns = tableMapper.findcolumnName(tableMapper.findNameCategoryById(category));

            XSSFWorkbook workbook = new XSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();
            XSSFSheet sheet = workbook.createSheet("Страница");
            XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

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

            XSSFCellStyle dublicate = workbook.createCellStyle();
            dublicate.setBorderTop(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.TOP,colorborder);
            dublicate.setBorderRight(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.RIGHT,colorborder);
            dublicate.setBorderBottom(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM,colorborder);
            dublicate.setBorderLeft(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.LEFT,colorborder);
            dublicate.setWrapText(true);
            dublicate.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 215, 64)));
            dublicate.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle header = workbook.createCellStyle();
            header.setFillForegroundColor(new  XSSFColor(new java.awt.Color(0,102,204)));
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setColor(new XSSFColor(new java.awt.Color(255,255,255)));
            header.setFont(headerFont);
            header.setWrapText(true);


            int numberRow = 0;
            XSSFRow row = sheet.createRow(numberRow);

            int numberColumn = 0;
            HashMap<String,Integer> column = new HashMap<String, Integer>();
            if(products.get(0).getId() != null){
                sheet.setColumnWidth(numberColumn,5*256);
                row.createCell(numberColumn).setCellValue("ID");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("id",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getVendor() != null){
                sheet.setColumnWidth(numberColumn,14*256);
                row.createCell(numberColumn).setCellValue("Вендор");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("vendor",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getVendor_code() != null){
                sheet.setColumnWidth(numberColumn,50*256);
                row.createCell(numberColumn).setCellValue("Артикул/Название");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("vendor_code",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getFrequency() != null){
                sheet.setColumnWidth(numberColumn,17*256);
                row.createCell(numberColumn).setCellValue("Частота/Полоса пропускания");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("frequency",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getChannel() != null){
                sheet.setColumnWidth(numberColumn,17*256);
                row.createCell(numberColumn).setCellValue("Каналы");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("channel",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getPort() != null){
                sheet.setColumnWidth(numberColumn,17*256);
                row.createCell(numberColumn).setCellValue("Порты");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("port",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getUsb() != null){
                sheet.setColumnWidth(numberColumn,17*256);
                row.createCell(numberColumn).setCellValue("USB");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("usb",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getVxi() != null){
                sheet.setColumnWidth(numberColumn,17*256);
                row.createCell(numberColumn).setCellValue("VXI");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("vxi",numberColumn);
                numberColumn++;
            }
            if(products.get(0).getPortable() != null){
                sheet.setColumnWidth(numberColumn,17*256);
                row.createCell(numberColumn).setCellValue("Портативный");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("portable",numberColumn);

            }

            for(Product product: products){

                numberRow+=1;
                row = sheet.createRow(numberRow);
                row.setHeight((short) -1);
                boolean flag = false;
                for(String col: columns){
                    if(column.containsKey(col)){

                        XSSFCell cell = row.createCell(column.get(col));
                        switch (col) {
                            case "id":
                                if(productsNoUses.contains(product)){
                                    flag = true;
                                }
                                cell.setCellValue(product.getId());
                                break;
                            case "vendor":
                                cell.setCellValue(product.getVendor());
                                break;
                            case "vendor_code":
                                cell.setCellValue(product.getVendor_code());
                                break;
                            case "frequency":
                                cell.setCellValue(product.getFrequency());
                                break;
                            case "usb":
                                cell.setCellValue(product.getUsb());
                                break;
                            case "vxi":
                                cell.setCellValue(product.getVxi());
                                break;
                            case "portable":
                                cell.setCellValue(product.getPortable());
                                break;
                            case "channel":
                                cell.setCellValue(product.getChannel());
                                break;
                            case "port":
                                cell.setCellValue(product.getPort());
                                break;
                        }
                        if (flag) {
                            cell.setCellStyle(dublicate);
                        } else {
                            cell.setCellStyle(body);
                        }
                    }
                }
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

    @PostMapping("/CreateTable")
    @ResponseBody
    Map<String,String> CreateTable(@RequestBody NewTable table) throws JSONException {
        tableMapper.InsertCategory(table.getName(),table.getName_en());
//        CREATE TABLE `keysight`.`table` (
//  `id` BIGINT NOT NULL AUTO_INCREMENT,
//  `vendor_code` VARCHAR(225) NOT NULL,
//  `usb` TINYINT NOT NULL,
//  `freq` DOUBLE NOT NULL,
//  `vendor` BIGINT NOT NULL,
//  PRIMARY KEY (`id`),
//  INDEX `vendor_idx` (`vendor` ASC) VISIBLE,
//  CONSTRAINT `vendor`
//    FOREIGN KEY (`vendor`)
//    REFERENCES `keysight`.`vendor` (`id`)
//    ON DELETE NO ACTION
//    ON UPDATE NO ACTION);
//  PRIMARY KEY (`id`));
        String create = "Create table "+table.getName_en()+" (`id` BIGINT NOT NULL AUTO_INCREMENT, `vendor_code` VARCHAR(225) NOT NULL,";
        if (table.isFrequency()){
            create = create + "`frequency` DOUBLE NOT NULL DEFAULT 0,";
        }
        if(table.isChannel()){
            create = create + "`channel` INT NOT NULL DEFAULT 0,";
        }
        if(table.isPort()){
            create = create + "`port` INT NOT NULL DEFAULT 0,";
        }
        if(table.isUsb()){
            create = create + "`usb` TINYINT NOT NULL DEFAULT b'0',";
        }
        if(table.isVxi()){
            create = create + "`vxi` TINYINT NOT NULL DEFAULT b'0',";
        }
        if(table.isPortable()){
            create = create + "`portable` TINYINT NOT NULL DEFAULT b'0',";
        }
        if(table.isVendor()){
            create = create + "`vendor` BIGINT NOT NULL DEFAULT 1,INDEX `vendor_idx` (`vendor` ASC), CONSTRAINT `vendor_"+table.getName_en()+"` FOREIGN KEY (`vendor`) REFERENCES `keysight`.`vendor` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,";
        }
        create = create + " PRIMARY KEY (`id`));";
        tableMapper.CreateTable(create);
        tableMapper.InsertProduct("Insert into "+table.getName_en()+"(vendor_code"+(table.isVendor()?",vendor)":")")+" values ('Без артикуля'"+(table.isVendor()?",'1')":")"));
        HashMap<String,String> answear = new HashMap<>();
        answear.put("name",table.getName());
        return answear;
    }

    @GetMapping("/ChangeProduct/{categoryFirst}/{categorySecond}")
    @ResponseBody
    String RemoveProduct(@PathVariable Long categoryFirst,@PathVariable Long categorySecond){
        List<Product> FirstProduct= tableMapper.findListProduct(searchAtribut.createSelectProductCategory(categoryFirst));
        String category = tableMapper.findNameCategoryById(categorySecond);
        String[] columns = tableMapper.findcolumnName(category);
        for(Product product:FirstProduct){
            Long id;
            StringBuilder insert = new StringBuilder("Insert into " + category + " (");
//            Insert into oscilloscope (vendor_code, frequency ,vendor, vxi, usb, channel) values(#{vendor_code}, #{frequency},#{vendor}, #{vxi},#{usb}, #{channel})
            if(tableMapper.findOneProduct(searchAtribut.createSelectProductCategory(categorySecond) + " where pr.vendor_code ='" + product.getVendor_code()+"'") == null){
                for(String column : columns){

                    if(column.equals("id")){
                        continue;
                    }
                    insert.append(" ").append(column).append(",");
                }
                insert = new StringBuilder(insert.substring(0, insert.length() - 1) + ") values(");
                for(String column : columns){
                    switch (column) {
                        case "id":
                            continue;
                        case "vendor":
                            insert.append("'").append(product.getVendor_id()).append("',");
                            break;
                        case "vendor_code":
                            insert.append("'").append(product.getVendor_code()).append("',");
                            break;
                        case "frequency":
                            insert.append("'").append((product.getFrequency() != null?product.getFrequency():'0')).append("',");
                            break;
                        case "usb":
                            insert.append(product.getUsb() != null ?(product.getUsb()?1:0):0).append(",");
                            break;
                        case "vxi":
                            insert.append(product.getVxi() != null ?(product.getVxi()?1:0):0).append(",");
                            break;
                        case "portable":
                            insert.append(product.getPortable() != null ?(product.getPortable()?1:0):0).append(",");
                            break;
                        case "channel":
                            insert.append("'").append(product.getChannel()!= null?product.getChannel():'0').append("',");
                            break;
                        case "port":
                            insert.append("'").append(product.getPort()!= null?product.getPort():'0').append("',");
                            break;
                    }
                }
                insert = new StringBuilder(insert.substring(0, insert.length() - 1) + ")");
                tableMapper.InsertProduct(insert.toString());
                id =  tableMapper.findOneProduct(searchAtribut.createSelectProductCategory(categorySecond) + " where pr.vendor_code ='" + product.getVendor_code()+"'").getId();
            }
            else {
               id =  tableMapper.findOneProduct(searchAtribut.createSelectProductCategory(categorySecond) + " where pr.vendor_code ='" + product.getVendor_code()+"'").getId();
            }
            List<Long> orders = tableMapper.findAllOrdersIdbyProduct(product.getId(),categoryFirst);
            for (Long order:orders){
                tableMapper.ChangeProduct(order,id,categorySecond);
            }
        }

        return "good";
    }

    @PostMapping("/ChangeCategory")
    @ResponseBody
    Map<String,String> ChangeCategory(@RequestBody ChangeCategory changeCategory){
        List<Long> firstProduct = tableMapper.findAllOrdersIdbyProduct(changeCategory.getVendor_code(),changeCategory.getCategory());
        for(Long id: firstProduct){
            tableMapper.ChangeProduct(id,changeCategory.getNewVendor_code(),changeCategory.getNewCategory());
        }

        HashMap<String,String> a = new HashMap<>();
        a.put("name","Заменил");
        return a;
    }

    @PostMapping( path = "/Test")
    @ResponseBody
    Map<String,String> Test (@RequestBody ReceivedJSON json) throws  JSONException {
        List<ProductCategory> productCategories = tableMapper.findAllProductCategory();
        for (ProductCategory productCategory : productCategories){
            if (!productCategory.getCategory().equals("Продукты")){
                ArrayList<ReportQuarter> reportVendorQuarter = this.reportService.getQuartalTenderReport(productCategory.getId(),json);
                if(reportVendorQuarter.size() != 0){
                    System.out.println(reportVendorQuarter.get(0).toString());
                }
            }
        }
        HashMap<String,String> a = new HashMap<>();

        a.put("name", "good");
        return a;
    }
    @GetMapping(path = "/Log")
    @ResponseBody
    Map<String,String> Log (){
        HashMap<String,String> a = new HashMap<>();
        String answear = "";
        try(FileInputStream fin=new FileInputStream(log))
        {
            int i=-1;
            while((i=fin.read())!=-1){
                answear = answear + (char)i;

            }
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
        a.put("name", answear);
        return a;
    }
}

