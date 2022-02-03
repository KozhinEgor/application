package com.application_tender.tender.controller;


import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.*;
import com.application_tender.tender.models.Comment;
import com.application_tender.tender.service.Bicotender;
import com.application_tender.tender.service.FileService;
import com.application_tender.tender.service.MailSender;
import com.application_tender.tender.service.ReportService;
import com.application_tender.tender.subsidiaryModels.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
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
    private final DateTimeFormatter format_date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
    private final DateTimeFormatter format_dateFile = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter format_API_Bico = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private final DateTimeFormatter format_Dublicate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TableMapper tableMapper;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private SearchAtribut searchAtribut;
    @Autowired
    private GetCurrency getCurrency;
    @Value("${file.pathname}")
    private String pathname;
    @Value("${file.log}")
    private String log;
    @Value("${site.url}")
    private String url;
    private final FileService fileService;
    private final ReportService reportService;
    private final Bicotender bicotender;

    public ApiController(TableMapper tableMapper, FileService fileService, ReportService reportService, Bicotender bicotender) {
        this.tableMapper = tableMapper;
        this.fileService = fileService;
        this.reportService = reportService;
        this.bicotender = bicotender;
    }

    @ApiOperation(value = "Возвращает список всех пользователей",  notes = "Производится запрос к БД и из таблицы Usr выбираются поля username,role,activationCode,nickname ")
    @GetMapping("/AllUsers")
    @ResponseBody
    List<User> allUsers() {
        return tableMapper.findAllUsers();
    }
    @ApiOperation(value = "Возвращает список всех типов тендеров",  notes = "Производится запрос к БД и из таблицы typetender выбираются все поля ")
    @GetMapping("/TypeTender")
    @ResponseBody
    List<TypeTender> typeTender() {
        return tableMapper.findAllType();
    }

    @ApiOperation(value = "Возвращает список всех Заказчиков",  notes = "Производится запрос к БД и из таблицы customer выбираются все поля, подтягивая название страны из таблицы country")
    @GetMapping("/Customer")
    @ResponseBody
    List<Company> customer() {
        return tableMapper.findAllCustomer();
    }

    @ApiOperation(value = "Возвращает список всех Заказчиков которые не упоминаются в основных тендерах и в смежных",  notes = "Производится запрос к БД и из таблицы customer выбираются все поля, подтягивая название страны из таблицы country" +
            "При условии того, что данный заказчик не упоминался в основных тендерах и не упоминался в смежных тендерах ")
    @GetMapping("/CustomerNoUses")
    @ResponseBody
    List<Company> customerNoUses() {
        return tableMapper.findAllCustomerNoUses();
    }

    @ApiOperation(value = "Возвращает список всех Заказчиков, которые не используются, после удаления тех которые не используются",  notes = "Вначале производит поиск неиспользующихся заказчиков, затем проходя по данному массиву удаляет каждый элемент, после чего возвращает списко заказчиков которые не используются")
    @GetMapping("/DeleteCustomerNoUses")
    @ResponseBody
    List<Company> deleteCustomerNoUses() {

        List<Company> companies = tableMapper.findAllCustomerNoUses();
        for (Company company : companies) {
            tableMapper.deleteCustomer(company.getId());
        }
        return tableMapper.findAllCustomerNoUses();
    }

    @ApiOperation(value = "Добавляет нового заказчика или обнавляет существующий в БД, и возвращает код 200", notes = "Проверяет наличие id в Компании которая пришла, при наличии ID в компании производит обновление данных," +
            "при отсутсвии ID, добавляет нового заказчика в БД")
    @PostMapping("/insertCustomer")
    @ResponseBody
    ResponseEntity insertCustomer(@RequestBody Company customer) {
        if (customer.getId() == null) {
            tableMapper.insertCustomer(customer.getInn(), customer.getName(), Long.valueOf(customer.getCountry()));
        } else {
            tableMapper.updateCustomer(customer.getId(), customer.getInn(), customer.getName(), Long.valueOf(customer.getCountry()));
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Изменяет заказчика в тендерах и возвращает какого заказчика на кого заменил", notes = "Вначале находит все тендеры со \"старым\" заказчиком и запускает циклы обновляя информацию о тендере," +
            " добавляя \"нового\" заказчика в основных и смежных тендерах, После чего формирует строку для врзвращение")
    @PostMapping("/ChangeCustomer")
    @ResponseBody
    Map<String, String> changeCustomer(@RequestBody ChangeCompany changeCompany) {
        List<Long> tender = tableMapper.findTenderByCustomer(changeCompany.getCompany());
        for (Long t : tender) {
            tableMapper.changeCustomerTender(t, changeCompany.getNewCompany());
        }
        tender = tableMapper.findAdjacentTenderByCustomer(changeCompany.getCompany());
        for (Long t : tender) {
            tableMapper.changeCustomerAdjacentTender(t, changeCompany.getNewCompany());
        }
        tender = tableMapper.findPlanTenderByCustomer(changeCompany.getCompany());
        for (Long t : tender) {
            tableMapper.changeCustomerPlanTender(t, changeCompany.getNewCompany());
        }
        HashMap<String, String> a = new HashMap<>();
        a.put("name", "Заменил " + tableMapper.findCustomerNameById(changeCompany.getCompany()) + " на " + tableMapper.findCustomerNameById(changeCompany.getNewCompany()));
        return a;
    }

    @ApiOperation(value = "Возвращает excel файл со списком всех заказчиков",  notes = "Выводится информация о всех заказчиков в excel файл")
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
        body.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        body.setBorderRight(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        body.setBorderBottom(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        body.setBorderLeft(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        body.setWrapText(true);

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 102, 204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
        header.setFont(headerFont);
        header.setWrapText(true);

        XSSFCellStyle dublicate = workbook.createCellStyle();
        dublicate.setBorderTop(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        dublicate.setBorderRight(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        dublicate.setBorderBottom(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        dublicate.setBorderLeft(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        dublicate.setWrapText(true);
        dublicate.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 215, 64)));
        dublicate.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0, 5 * 256);
        sheet.setColumnWidth(1, 39 * 256);
        sheet.setColumnWidth(2, 14 * 256);
        sheet.setColumnWidth(3, 14 * 256);

        row.createCell(0).setCellValue("Id");
        row.getCell(0).setCellStyle(header);
        row.createCell(1).setCellValue("Заказчик");
        row.getCell(1).setCellStyle(header);
        row.createCell(2).setCellValue("ИНН/БИН/УНП");
        row.getCell(2).setCellStyle(header);
        row.createCell(3).setCellValue("Страна");
        row.getCell(3).setCellStyle(header);

        numberRow++;
        for (Company company : customer) {
            row = sheet.createRow(numberRow);
            row.createCell(0).setCellValue(company.getId());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(company.getName());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(company.getInn());
            row.getCell(2).setCellStyle(body);
            row.createCell(3).setCellValue(company.getCountry());
            row.getCell(3).setCellStyle(body);
            if (customerNoUses.contains(company)) {
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

    @ApiOperation(value = "Возвращает список всех Победителей",  notes = "Производится запрос к БД и из таблицы winner выбираются все поля, подтягивая название страны из таблицы country")
    @GetMapping("/Winner")
    @ResponseBody
    List<Company> winner() {
        return tableMapper.findAllWinner();
    }

    @ApiOperation(value = "Возвращает список всех Победителей которые не упоминаются в основных тендерах",  notes = "Производится запрос к БД и из таблицы winner выбираются все поля, подтягивая название страны из таблицы country" +
            "При условии того, что данный победитель не упоминался в основных тендерах ")
    @GetMapping("/WinnerNoUses")
    @ResponseBody
    List<Company> winnerNoUses() {
        return tableMapper.findAllWinnerNoUses();
    }

    @ApiOperation(value = "Возвращает список всех Победителей, которые не используются, после удаления тех которые не используются",  notes = "Вначале производит поиск неиспользующихся победителей, затем проходя по данному массиву удаляет каждый элемент, после чего возвращает списко победителей которые не используются")
    @GetMapping("/DeleteWinnerNoUses")
    @ResponseBody
    List<Company> deleteWinnerNoUses() {
        List<Company> companies = tableMapper.findAllWinnerNoUses();
        for (Company company : companies) {
            tableMapper.deleteWinner(company.getId());
        }
        return tableMapper.findAllWinnerNoUses();
    }

    @ApiOperation(value = "Добавляет нового победителя или обнавляет существующий в БД, и возвращает код 200", notes = "Проверяет наличие id в Компании которая пришла, при наличии ID в компании производит обновление данных," +
            "при отсутсвии ID, добавляет нового победителя в БД")
    @PostMapping("/insertWinner")
    @ResponseBody
    ResponseEntity insertWinner(@RequestBody Company winner) {

        if (winner.getId() == null) {
            tableMapper.insertWinner(winner.getInn(), winner.getName(), Long.valueOf(winner.getCountry()));
        } else {
            tableMapper.updateWinner(winner.getId(), winner.getInn(), winner.getName(), Long.valueOf(winner.getCountry()));
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Изменяет Победителя в тендерах и возвращает какого победителя на кого заменил",notes = "Вначале находит все тендеры со \"старым\" победителем и запускает цикл обновляя информацию о тендере," +
            " добавляя \"нового\" победителя в основных тендерах, После чего формирует строку для врзвращение")
    @PostMapping("/ChangeWinner")
    @ResponseBody
    Map<String, String> changeWinner(@RequestBody ChangeCompany changeCompany) {
        List<Long> tender = tableMapper.findTenderByWinner(changeCompany.getCompany());
        for (Long t : tender) {
            tableMapper.changeWinner(t, changeCompany.getNewCompany());
        }
        HashMap<String, String> a = new HashMap<>();
        a.put("name", "Заменил " + tableMapper.findWinnerNameById(changeCompany.getCompany()) + " на " + tableMapper.findWinnerNameById(changeCompany.getNewCompany()));
        return a;
    }

    @ApiOperation(value = "Возвращает excel файл со списком всех победителей",  notes = "Выводится информация о всех победителях в excel файл")
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
        body.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        body.setBorderRight(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        body.setBorderBottom(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        body.setBorderLeft(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        body.setWrapText(true);

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 102, 204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
        header.setFont(headerFont);
        header.setWrapText(true);

        XSSFCellStyle dublicate = workbook.createCellStyle();
        dublicate.setBorderTop(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        dublicate.setBorderRight(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        dublicate.setBorderBottom(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        dublicate.setBorderLeft(BorderStyle.THIN);
        dublicate.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        dublicate.setWrapText(true);
        dublicate.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 215, 64)));
        dublicate.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0, 5 * 256);
        sheet.setColumnWidth(1, 39 * 256);
        sheet.setColumnWidth(2, 14 * 256);
        sheet.setColumnWidth(3, 14 * 256);

        row.createCell(0).setCellValue("Id");
        row.getCell(0).setCellStyle(header);
        row.createCell(1).setCellValue("Победитель");
        row.getCell(1).setCellStyle(header);
        row.createCell(2).setCellValue("ИНН/БИН/УНП");
        row.getCell(2).setCellStyle(header);
        row.createCell(3).setCellValue("Страна");
        row.getCell(3).setCellStyle(header);

        numberRow++;
        for (Company company : winner) {
            row = sheet.createRow(numberRow);
            row.createCell(0).setCellValue(company.getId());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(company.getName());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(company.getInn());
            row.getCell(2).setCellStyle(body);
            row.createCell(3).setCellValue(company.getCountry());
            row.getCell(3).setCellStyle(body);
            if (winnerNoUses.contains(company)) {
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

    @ApiOperation(value = "Возвращает Информацию об основных тендерах по заданным условиям", notes = "Вначале закпускается функция формирующая условия поиска тендеров, после чего сформированная строка используется в запросе к БД")
    @PostMapping("/Tender")
    @ResponseBody
    List<Tender> Tender(@RequestBody SearchParameters json) {
        if(json.isAdjacent_tender()){
            return tableMapper.findAllAdjacentTenderTerms(searchAtribut.findTenderByTerms(json));
        }
        else if(json.isPlan_schedule()){
            return tableMapper.findAllPlanTenderTerms(searchAtribut.findTenderByTerms(json));
        }
        else{
            return tableMapper.findAllTenderTerms(searchAtribut.findTenderByTerms(json));
        }
        
    }

//    @ApiOperation(value = "Возвращает Информацию о смежных тендерах по заданным условиям", notes = "Вначале закпускается функция формирующая условия поиска тендеров, после чего сформированная строка используется в запросе к БД")
//    @PostMapping("/AdjacentTender")
//    @ResponseBody
//    List<Tender> AdjacentTender(@RequestBody SearchParameters json) {
//        
//    }
//
//    @ApiOperation(value = "Возвращает Информацию о планах графиков тендеров по заданным условиям", notes = "Вначале закпускается функция формирующая условия поиска тендеров, после чего сформированная строка используется в запросе к БД")
//    @PostMapping("/PlanTender")
//    @ResponseBody
//    List<Tender> PlanTender(@RequestBody SearchParameters json) {
//        
//    }

    @ApiOperation(value = "НЕ ИСПОЛЬЗУЕТСЯ Возвращает Информацию о продуктах из Категории Продукты", notes = "Возвращает список всех продуктов из категории Продукты")
    @GetMapping("/AnotherProduct")
    @ResponseBody
    List<AnotherProduct> AnotherProduct() {
        return tableMapper.findAllAnotherProduct();
    }

    @ApiOperation(value = "Возвращает список всех категорий продуктов", notes = "Выполняет запрос к БД и возвращает навзания и id всех категорий")
    @GetMapping("/ProductCategory")
    @ResponseBody
    List<ProductCategory> ProductCategory() {
        return tableMapper.findAllProductCategory();
    }

    @ApiOperation(value = "Возвращает список продуктов из категории", notes = "Запускает функцию формирования запроса для выборки всех продуктов из категории по id ее в таблице product_category")
    @GetMapping("/VendorCode/{id}")
    @ResponseBody
    List<Product> Product(@PathVariable Long id) {
        if (id == 0L) {
            return null;
        } else {
            return tableMapper.findListProduct(searchAtribut.createSelectProductCategory(id));
        }
    }

    @ApiOperation(value = "Возвращает список продуктов из категории, которые не используются", notes = "Запускает функцию формирования запроса для выборки всех продуктов из категории по id ее в таблице product_category, при условии что данный продукт не упоминается в таблице orders")
    @GetMapping("/VendorCodeNoUses/{id}")
    @ResponseBody
    List<Product> ProductNoUses(@PathVariable Long id) {

        return tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(id));

    }

    @ApiOperation(value = "Удаляет продукты из категории, которые не используются", notes = "Запускает функцию формирования запроса для выборки всех продуктов из категории по id ее в таблице product_category, при условии что данный продукт не упоминается в таблице orders, после чего проходит по данному массиву удаляя каждый элемент из БД")
    @GetMapping("/DeleteVendorCodeNoUses/{id}")
    @ResponseBody
    List<Product> DeleteProductNoUses(@PathVariable Long id) {
        List<Product> products = tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(id));
        for (Product product : products) {
            tableMapper.DeleteProduct(tableMapper.findNameCategoryById(id), product.getId());
        }
        return tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(id));

    }

    @ApiOperation(value = "Возвращает информацию о продукте по Id его в категории И id категории", notes = "Функция которая возвращает информацию об одном продукте из определенной категории")
    @GetMapping("/VendorCodeById/{id}/{id_product}")
    @ResponseBody
    Product ProductById(@PathVariable Long id, @PathVariable Long id_product) {
        String select = searchAtribut.createSelectProductCategory(id);
        int index = select.indexOf("order by");
        select = select.substring(0, index - 1) + " where pr.id =" + String.valueOf(id_product) + " " + select.substring(index);
        return tableMapper.findOneProduct(select);

    }

    @ApiOperation(value = "Список продуктов в тендере по его id", notes = "Возвращает список продуктов в тендере, понятном пользователю")
    @GetMapping("/OrdersByTender/{tender}")
    @ResponseBody
    OrdersReceived OrdersByTender(@PathVariable Long tender) {
        List<OrdersDB> ordersDB = tableMapper.findAllOrdersBDbyTender(tender);
        List<Orders> orders = new LinkedList<>();
        for (OrdersDB orderDB : ordersDB) {

            Product product_id = searchAtribut.ProductToOrders(orderDB.getProduct_category(), orderDB.getId_product());
            String comment = "";
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
                    tableMapper.findOneCategoryById(orderDB.getProduct_category()),
                    (
                        (this.searchAtribut.subcategoryProduct(orderDB.getProduct_category(), orderDB.getId_product()))
                        +(searchAtribut.VendorToOrders(orderDB.getProduct_category(), orderDB.getId_product()) == null ? "" :  searchAtribut.VendorToOrders(orderDB.getProduct_category(), orderDB.getId_product()) + ' ')
                        + (product_id == null ? "" : product_id.getVendor_code() + " ")
                    ),
                    product_id == null ? tableMapper.findOneVendorById(1L) : tableMapper.findOneVendorById(product_id.getVendor_id()),
                    comment,
                    orderDB.getNumber(),
                    orderDB.getPrice(),
                    orderDB.getWinprice()));
        }
        return new OrdersReceived(orders, ordersDB);
    }

    @ApiOperation(value = "Список продуктов в тендере по его id", notes = "Возвращает список продуктов в тендере, использая только id продуктов")
    @GetMapping("/OrdersBDByTender/{tender}")
    @ResponseBody
    List<OrdersDB> OrdersBDByTender(@PathVariable Long tender) {
        return tableMapper.findAllOrdersBDbyTender(tender);
    }

    @ApiOperation(value = "Добавление основных тендеров через excel файл", notes = "Получает на вход файл в определенном формате, после чего проходится по всем строкам добавляя, новые тендеры в систему")
    @RequestMapping(value = "/addTender", method = RequestMethod.POST, consumes = {"multipart/form-data"})
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
        Map<String, Double> currency = getCurrency.currency(dateCurrency.format(formatCurrency));
        int count = 1;
        while (sheet.getRow(count).getCell(0) != null) {
            XSSFRow row = sheet.getRow(count);
            String numberTender = new DataFormatter().formatCellValue(row.getCell(7));
            if (numberTender.equals("")) {
                break;
            }
            Long id;
            if (tableMapper.findTenderByNumber_tender(numberTender) != null) {

                id = tableMapper.findTenderByNumber_tender(numberTender);

            } else {

                String INNCustomer = new DataFormatter().formatCellValue(row.getCell(3)).trim();

                ZonedDateTime dateStart = ZonedDateTime.parse(row.getCell(8).getStringCellValue() + " 00:00:00 Z", format_date).plusDays(1);
                currency = getCurrency.currency(dateStart.format(formatCurrency));
                double rate = row.getCell(5).getStringCellValue().equals("RUB") ? 1 : currency.get(row.getCell(5).getStringCellValue());
                tableMapper.insertTender(row.getCell(0).getStringCellValue(),
                        "https://www.bicotender.ru/tc/tender/show/tender_id/" + numberTender,
                        row.getCell(1).getStringCellValue(),
                        ZonedDateTime.parse(row.getCell(8).getStringCellValue() + " 00:00:00 Z", format_date),
                        row.getCell(9).getStringCellValue().length() == 10 ? ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " 00:00:00 Z", format_date) :
                                ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " Z", format_date),
                        row.getCell(10).getCellType() != CellType.BLANK ?
                                row.getCell(9).getStringCellValue().length() == 10 ? ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " 00:00:00 Z", format_date) :
                                        ZonedDateTime.parse(row.getCell(9).getStringCellValue() + " Z", format_date) :
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

    @ApiOperation(value = "Добавляет продукты в категорию из excel файла", notes = "Данная функция нужна для выделение категорий из Прдуктов, помогает быстро создать и перенести продукты в новую категорию")
    @RequestMapping(value = "/addProduct/{category}", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    @ResponseBody
    Map<String, String> addProduct(@PathVariable Long category,MultipartFile excel) throws IOException, InvalidFormatException {


        File temp = new File(pathname);

        excel.transferTo(temp);
        //InputStream ExcelFileToRead= new InputStreamReader(new FileInputStream(temp), "UTF-8");
        XSSFWorkbook workbook = new XSSFWorkbook(temp);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map<String, String> a = new HashMap<>();
        a.put("name","Загрузил");
        ProductCategory productCategory = tableMapper.findCategoryById(category);

        int count = 1;
        while ( sheet.getRow(count) != null && sheet.getRow(count).getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null ) {
            XSSFRow row = sheet.getRow(count);

            if(row.getCell(0).getNumericCellValue() != 1){

                String vendor_code = row.getCell(2).toString().substring(row.getCell(2).toString().lastIndexOf(" ")).trim();
                if(tableMapper.findIdProduct("Select id from " + productCategory.getCategory_en() + " where vendor_code ='" + vendor_code + "' and vendor = '"+searchAtribut.findVendor(row.getCell(2).toString().substring(0,row.getCell(2).toString().lastIndexOf(" ")))+"'") == null){
                    tableMapper.InsertProduct("Insert into " + productCategory.getCategory_en() + "(vendor_code,vendor) values ('" + vendor_code + "'" + ",'" + searchAtribut.findVendor(row.getCell(2).toString().substring(0,row.getCell(2).toString().lastIndexOf(" "))) + "')");
                }
                Long id = tableMapper.findIdProduct("Select id from " + productCategory.getCategory_en() + " where vendor_code ='" + vendor_code + "' and vendor = '"+searchAtribut.findVendor(row.getCell(2).toString().substring(0,row.getCell(2).toString().lastIndexOf(" ")))+"'");
                for(String id_order_string : row.getCell(3).getStringCellValue().trim().split(" ")){
                    Long id_order;
                    if(!id_order_string.trim().equals("")){
                         id_order = Long.valueOf(id_order_string.trim());
//                        if(id_order>21603){
//                            continue;
//                        }
                    }
                    else{
                        continue;
                    }
                    tableMapper.ChangeProductFromFile(id_order,id,category,"");
                    try {
                        searchAtribut.UpdateProductTender(tableMapper.findTenderIdbyId(id_order));
                    }
                    catch (NullPointerException e){
                    }
                }
            }
            else{
                Long id_order = Long.valueOf(row.getCell(3).getStringCellValue().trim());
//                if(id_order>21603){
//                    count++;
//                    continue;
//                }
                tableMapper.ChangeProduct(id_order,1L,category);
                searchAtribut.UpdateProductTender(tableMapper.findTenderIdbyId(id_order));
            }

            count++;
        }

        //ExcelFileToRead.close();

        return a;
    }

    @ApiOperation(value = "Добавление основных тендеров через Api Bicotender", notes = "Получает на вход список номеров тендоров в системе Bicotender. После чего делает запрос к Bicotender и получает всю информацию о данном тендере")
    @PostMapping("/loadTender")
    @ResponseBody
    List<List<Tender>> loadTender(@RequestBody Long[] number) throws JSONException {
        List<List<Tender>> tenders = new ArrayList<>();
        String buf = "";
        for(Long num : number){
            buf = buf+num.toString() + " ";
        }
        tableMapper.upadateBuffer(buf.trim(),1L);
        for (Long num : number) {
            Long id;
            DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (tableMapper.findTenderByNumber_tender(num.toString()) != null) {

                id = tableMapper.findTenderByNumber_tender(num.toString());

            } else {
                JSONObject tender = bicotender.loadTender(num);
                if(tender == null){
                    return null;
                }
                ZonedDateTime dateStart = ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico).plusDays(1);
                Map<String, Double> currency = new HashMap<>();
                currency = getCurrency.currency(dateStart.format(formatCurrency));
                double rate = tender.get("valuta").toString().equals("RUB") || tender.get("valuta").toString().equals("null")  ? 1 : currency.get(tender.get("valuta").toString());
                JSONObject company = new JSONObject(tender.get("company").toString());
                String cost = tender.get("cost").toString().equals("null") ? "0" : tender.get("cost").toString();
                tableMapper.insertTender(tender.get("name").toString(),
                        "https://www.bicotender.ru/tc/tender/show/tender_id/" + tender.get("tender_id"),
                        tender.get("sourceUrl").toString(),
                        ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico),
                        tender.get("finishDate").toString().equals("null") ? null :  ZonedDateTime.parse(tender.get("finishDate").toString() + " Z", format_API_Bico),
                        tender.get("openingDate").toString().equals("null") ? null : ZonedDateTime.parse(tender.get("openingDate").toString() + " Z", format_API_Bico),
                        tender.get("tender_id").toString(),
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING),
                        new BigDecimal(0),
                        tender.get("valuta").toString().equals("null") ? null : tender.get("valuta").toString(),
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING),
                        rate,
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING).multiply(new BigDecimal(rate)),
                        searchAtribut.findCustomer(company.get("inn").toString(), company.get("name").toString()),
                        searchAtribut.findTypetender(tender.get("typeName").toString()),
                        1L
                );
                id = tableMapper.findTenderByNumber_tender(tender.get("tender_id").toString());
            }
            List<Tender> tenderList = new ArrayList<>();
            tenderList.add(tableMapper.findTenderbyId(id));
            if(tableMapper.SelectNameDublicate(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate), tenderList.get(0).getId()).size() != 0){
                tenderList.addAll(tableMapper.SelectNameDublicate(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate), tenderList.get(0).getId()));
            }
            if(tableMapper.SelectNameDublicatePlan(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate)).size() != 0){
                tenderList.addAll(tableMapper.SelectNameDublicatePlan(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate)));
            }
            tenders.add(tenderList);
        }
        tableMapper.upadateBuffer(null,1L);
        return tenders;
    }

    @ApiOperation(value = "Возвращение номеров тендоров, которые загрузились не до конца во время загрузки тендоров", notes = "Возвращает список номеров основных и смежных тендеров через пробел")
    @GetMapping("/numberFromBuffer/{id}")
    @ResponseBody
    Map<String,String> numberFromBuffer(@PathVariable Long id){
    Map<String,String> a = new HashMap<>();
    a.put("name",tableMapper.SelectBuf(id));
    return a;
    }

    @ApiOperation(value = "Добавление смежных тендеров через Api Bicotender", notes = "Получает на вход список номеров тендоров в системе Bicotender. После чего делает запрос к Bicotender и получает всю информацию о данном тендере")
    @PostMapping("/loadTenderAdjacent")
    @ResponseBody
    List<Tender> loadTenderAdjacentr(@RequestBody Long[] number) throws JSONException {
        LinkedList<Tender> tenders = new LinkedList<>();
        String buf = "";
        for(Long num : number){
            buf = buf+num.toString() + " ";
        }
        tableMapper.upadateBuffer(buf.trim(),2L);
        for (Long num : number) {
            Long id;
            DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (tableMapper.findAdjacentTenderByNumber_tender(num.toString()) != null) {
                id = tableMapper.findAdjacentTenderByNumber_tender(num.toString());

            } else {
                JSONObject tender = bicotender.loadTender(num);
                if(tender == null){
                    return null;
                }
                ZonedDateTime dateStart = ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico).plusDays(1);
                Map<String, Double> currency = new HashMap<>();
                currency = getCurrency.currency(dateStart.format(formatCurrency));
                double rate = tender.get("valuta").toString().equals("RUB") ? 1 : currency.get(tender.get("valuta").toString());
                JSONObject company = new JSONObject(tender.get("company").toString());
                String cost = tender.get("cost").toString().equals("null") ? "0" : tender.get("cost").toString();
                tableMapper.insertAdjacentTender(tender.get("name").toString(),
                        "https://www.bicotender.ru/tc/tender/show/tender_id/" + tender.get("tender_id"),
                        tender.get("sourceUrl").toString(),
                        ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico),
                        ZonedDateTime.parse(tender.get("finishDate").toString() + " Z", format_API_Bico),
                        tender.get("openingDate").toString().equals("null") ? null : ZonedDateTime.parse(tender.get("openingDate").toString() + " Z", format_API_Bico),
                        tender.get("tender_id").toString(),
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING),

                        tender.get("valuta").toString(),
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING),
                        rate,
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING).multiply(new BigDecimal(rate)),
                        searchAtribut.findCustomer(company.get("inn").toString(), company.get("name").toString()),
                        searchAtribut.findTypetender(tender.get("typeName").toString())
                );
                id = tableMapper.findAdjacentTenderByNumber_tender(tender.get("tender_id").toString());
            }
            tenders.add(tableMapper.findAdjacentTenderbyId(id));
        }
        tableMapper.upadateBuffer(null,2L);
        return tenders;
    }

    @ApiOperation(value = "Добавление планов графиков тендеров через Api Bicotender", notes = "Получает на вход список номеров тендоров в системе Bicotender. После чего делает запрос к Bicotender и получает всю информацию о данном тендере")
    @PostMapping("/loadTenderPlan")
    @ResponseBody
    List<Tender> loadTenderPlan(@RequestBody Long[] number) throws JSONException {

        LinkedList<Tender> tenders = new LinkedList<>();
        String buf = "";
        for(Long num : number){
            buf = buf+num.toString() + " ";
        }
        tableMapper.upadateBuffer(buf.trim(),3L);
        for (Long num : number) {
            Long id;
            DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (tableMapper.findPlanTenderByNumber_tender(num.toString()) != null) {
                id = tableMapper.findPlanTenderByNumber_tender(num.toString());

            } else {
                JSONObject tender = bicotender.loadTender(num);
                if(tender == null){
                    return null;
                }
                ZonedDateTime dateStart = ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico).plusDays(1);
                Map<String, Double> currency = new HashMap<>();
                currency = getCurrency.currency(dateStart.format(formatCurrency));
                double rate = tender.get("valuta").toString().equals("RUB") ? 1 : currency.get(tender.get("valuta").toString());
                JSONObject company = new JSONObject(tender.get("company").toString());
                String cost = tender.get("cost").toString().equals("null") ? "0" : tender.get("cost").toString();
                tableMapper.insertPlanTender(tender.get("name").toString(),
                        "https://www.bicotender.ru/tc/tender/show/tender_id/" + tender.get("tender_id"),
                        tender.get("sourceUrl").toString(),
                        ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico),
                        ZonedDateTime.parse(tender.get("finishDate").toString() + " Z", format_API_Bico),
                        tender.get("openingDate").toString().equals("null") ? null : ZonedDateTime.parse(tender.get("openingDate").toString() + " Z", format_API_Bico),
                        tender.get("tender_id").toString(),
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING),

                        tender.get("valuta").toString(),
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING),
                        rate,
                        new BigDecimal(cost).setScale(2, BigDecimal.ROUND_CEILING).multiply(new BigDecimal(rate)),
                        searchAtribut.findCustomer(company.get("inn").toString(), company.get("name").toString()),
                        searchAtribut.findTypetender(tender.get("typeName").toString())
                );
                id = tableMapper.findPlanTenderByNumber_tender(tender.get("tender_id").toString());
            }
            tenders.add(tableMapper.findPlanTenderbyId(id));
        }
        tableMapper.upadateBuffer(null,3L);
        return tenders;
    }

    @ApiOperation(value = "Возвращает список вендоров в данной категории", notes = "по id категории выбирает список вендоров в данной категории")
    @GetMapping("/Vendor/{category}")
    @ResponseBody
    List<Vendor> Vendor(@PathVariable Long category) {
        if (category == 0L) {
            return tableMapper.findAllVendor();
        } else {
            return tableMapper.findAllVendorByCategory(tableMapper.findNameCategoryById(category));
        }
    }

    @ApiOperation(value = "Добавляет или изменяет информацию о продуктах в тендере", notes = "Сравнивает список продуктов который пришел и который есть в БД и изменяет если есть изменения, удаляет лишние из БД и Добавляет нужные в БД")
    @PostMapping("/addOrders")
    @ResponseBody
    HashMap<String, String> Tender(@RequestBody List<OrdersDB> json) {
        List<Long> ordersINDB = tableMapper.findAllOrdersIdbyTender(json.get(0).getTender());
        if (json.get(0).getId_product() != null) {
            for (OrdersDB ordersDB : json) {
                if (ordersINDB.contains(ordersDB.getId())) {
                    ordersINDB.remove(ordersDB.getId());
                }
                if (ordersDB.getId() == null) {

                    tableMapper.insertOrder(
                           ordersDB
                    );
                    Long id = tableMapper.checkId();
                    if(ordersDB.getOption() != null){
                        for(Option option:ordersDB.getOption()){
                            tableMapper.insertOptionsOrders(id,option.getId());
                        }
                        tableMapper.updateOrdersOptions(tableMapper.SelectOptionsForOrdes(id), id);
                    }
                    else{
                        tableMapper.updateOrdersOptions(null,id);
                    }
                }
                else {
                    tableMapper.updateOrder(
                            ordersDB.getId(),
                            ordersDB.getComment(),
                            ordersDB.getId_product(),
                            ordersDB.getProduct_category(),
                            ordersDB.getTender(),
                            ordersDB.getNumber(),
                            ordersDB.getPrice() == null ? new BigDecimal(0) : ordersDB.getPrice(),
                            ordersDB.getWinprice() == null ? new BigDecimal(0) : ordersDB.getWinprice(),
                            ordersDB.getFrequency(),
                            ordersDB.getUsb(),
                            ordersDB.getVxi(),
                            ordersDB.getPortable(),
                            ordersDB.getChannel(),
                            ordersDB.getPort(),
                            ordersDB.getForm_factor(),
                            ordersDB.getPurpose(),
                            ordersDB.getVoltage(),
                            ordersDB.getCurrent()
                    );
                    if(ordersDB.getOption() != null){
                        List<Long> options_products = tableMapper.getAllOptionsByOrder(ordersDB.getId());
                        for(Option option:ordersDB.getOption()){

                            if(options_products.contains(option.getId())){
                                options_products.remove(option.getId());
                            }
                            else {
                                tableMapper.insertOptionsOrders(ordersDB.getId(),option.getId());
                            }
                        }
                        for(Long id_option : options_products){
                            tableMapper.deleteOptionsOrder(id_option);
                        }
                        tableMapper.updateOrdersOptions(tableMapper.SelectOptionsForOrdes(ordersDB.getId()),ordersDB.getId());
                    }
                }

                if(ordersDB.getOptions() != null){
                    tableMapper.updateOrdersOptions(ordersDB.getOptions(),ordersDB.getId());
                }
                else{
                    tableMapper.updateOrdersOptions(null,ordersDB.getId());
                }
            }

        }
        for (Long id : ordersINDB) {
            tableMapper.deleteOrder(id);

        }
        String product = searchAtribut.UpdateProductTender(json.get(0).getTender());
        HashMap<String, String> answear = new HashMap<>();
        answear.put("name", product);
        return answear;
    }

    @ApiOperation(value = "Возвращает количество тендеров без продуктов", notes = "Подсчитывает количество тендеров, которые не упоминаются в таблице orders")
    @GetMapping("/CountTenderWithoutOrders")
    @ResponseBody
    Long findCountTenderWithoutOrders() {
        return tableMapper.findCountTenderWithoutOrders();
    }

    @ApiOperation(value = "Возвращает тендеры без продуктов", notes = "Возвращает тендеры, которые не упоминаются в таблице orders")
    @GetMapping("/TenderWithoutOrders")
    @ResponseBody
    List<Tender> findTenderWithoutOrders() {
        return tableMapper.findTenderWithoutOrders();
    }

    @ApiOperation(value = "Возвращает тендеры в которых есть продукт, нет документации", notes = "Возвращает тендеры в которых есть продукт, нет документации")
    @GetMapping("/TendernoDocumentation")
    @ResponseBody
    List<Tender> findTendernoDocumentation() {
        return tableMapper.findTendernoDocumentation();
    }

    @ApiOperation(value = "Возвращает количество тендеров по кварталам и сумму данных тендеров в категории")
    @RequestMapping(path = "/quarterTender/{category}")
    @ResponseBody
    public ArrayList<ReportQuarter> getQuartalTenderReport(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalTenderReport(category, json);
    }

    @ApiOperation(value = "Возвращает количество тендеров по кварталам и сумму данных тендеров в большой категории")
    @RequestMapping(path = "/quarterTenderBigCategory/{category}")
    @ResponseBody
    public ArrayList<ReportQuarter> getQuartalTenderReportBigCategory(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalTenderReportBigCategory(category, json);
    }

    @ApiOperation(value = "Возвращает количество упоминаний продукта в тендерах по кварталам у определеного вендора")
    @RequestMapping(path = "/quarterVendor/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalVendorReport(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalVendorReport(category, json);
    }

    @ApiOperation(value = "Возвращает количество упоминаний продукта в тендерах по кварталам у определеного вендора")
    @RequestMapping(path = "/quarterVendorBigCategory/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalVendorReportBigCategory(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalVendorReportBigCategory(category, json);
    }

    @ApiOperation(value = "Возвращает количество упоминаний комментария к продуктам у которых артикул \"Без артикула\"")
    @RequestMapping(path = "/quarterNoVendor/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalNoVendorReport(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalNoVendorReport(category, json);
    }

    @ApiOperation(value = "Возвращает количество упоминаний комментария к продуктам у которых артикул \"Без артикула\"")
    @RequestMapping(path = "/quarterNoVendorBigCategory/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalNoVendorReportBigCategory(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalNoVendorReportBigCategory(category, json);
    }

    @ApiOperation(value = "Возвращает количество упоминаний компании в тендерах по кварталам ")
    @RequestMapping(path = "/quarterCustomer/{company}")
    @ResponseBody
    public Report getQuartalCustomerReport(@PathVariable Long company, @RequestBody ReportCriteria reportCriteria) {
        if(reportCriteria.getSearchParameters().getDateStart() == null){
            reportCriteria.getSearchParameters().setDateStart(ZonedDateTime.parse("01.01.2018 00:00:00 Z",format_date));
        }
        if(reportCriteria.getSearchParameters().getDateFinish() == null){
            reportCriteria.getSearchParameters().setDateFinish(ZonedDateTime.now());
        }
        String tender = searchAtribut.WhereWithoutProduct(reportCriteria.getSearchParameters()).substring(5);
        String product = searchAtribut.searchTenderByProduct(reportCriteria.getSearchParameters().getProduct());
        String select = "";
        if(company == 0L){
            select = "Select c.name as 'Компания', convert(sum(round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                    " left join product_category on orders.product_category = product_category.id" +
                    " left join subcategory on subcategory = subcategory.id"+
                    " left join vendor on pr.vendor = vendor.id "+
                    " where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)),char) as 'Сумма', convert(count(distinct orders.tender),char) as 'Количество тендеров',";

        }
        else{
            select = "Select w.name as 'Компания', convert(sum(round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                    " left join product_category on orders.product_category = product_category.id" +
                    " left join subcategory on subcategory = subcategory.id"+
                    " left join vendor on pr.vendor = vendor.id "+
                    " where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)),char) as 'Сумма', convert(count(distinct orders.tender),char) as 'Количество тендеров',";

        }

        List<String> column = new ArrayList<String>();
        column.add("Компания");
        column.add("Сумма");
        column.add("Количество тендеров");

        switch (reportCriteria.getInterval()){
            case "Год":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    select= select+ "convert(sum(case" +
                            " when year(date_start) = " + year +
                            " then"+" round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                            " left join product_category on orders.product_category = product_category.id" +
                            " left join subcategory on subcategory = subcategory.id"+
                            " left join vendor on pr.vendor = vendor.id"+
                            "  where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)" +
                            " else null end),char) as 'Сумма в " + year + "', ";
                  select= select+ "convert(count(distinct (" +
                            " select(if( year(date_start) =" + year +
                            " ,tender.id" +
                            " ,null)))),char) as ' Количество в " + year + "',";
                    column.add("Сумма в " + year);
                    column.add("Количество в " + year);
                }


                break;
            case "Финансовый год":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    select= select+ "convert(sum(case" +
                            " when YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0) = " + year +
                            " then"+" round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                            " left join tender on orders.tender = tender.id" +
                            " left join product_category on orders.product_category = product_category.id" +
                            " left join subcategory on subcategory = subcategory.id"+
                            " left join vendor on pr.vendor = vendor.id "+" where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)" +
                            " else null end),char) as 'Сумма в " + year + "',";
                    select= select+ "convert(count(distinct (" +
                            " select(if( YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0) = " + year +
                            " ,tender.id" +
                            " ,null)))),char) as ' Количество в " + year + "',";
                    column.add("Сумма в " + year);
                    column.add("Количество в " + year);
                }
                break;
            case "Неделя":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int week = 1; week <= 52; week++) {
                        select= select+ "convert(sum(case" +
                                " when year(date_start) = " + year + " and week(date_start) = " + week +
                                " then"+" round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                                " left join tender on orders.tender = tender.id" +
                                " left join product_category on orders.product_category = product_category.id" +
                                " left join subcategory on subcategory = subcategory.id"+
                                " left join vendor on pr.vendor = vendor.id "+" where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)" +
                                " else null end),char) as 'Сумма в " + year + "W" + week + "',";
                        select= select+ "convert(count(distinct (" +
                                " select(if( year(date_start) = " + year + " and week(date_start) = " + week +
                                " ,tender.id" +
                                " ,null)))),char) as 'Количество в " + year + "W" + week + "',";
                        column.add("Сумма в " + year + "W" + week);
                        column.add("Количество в " + year + "W" + week);
                    }
                }
                break;
            case "Квартал":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int quarter = 1; quarter <= 4; quarter++) {
                        select= select+ "convert(sum(case" +
                                " when year(date_start) = " + year + " and quarter(date_start) = " + quarter +
                                " then"+" round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                                " left join tender on orders.tender = tender.id" +
                                " left join product_category on orders.product_category = product_category.id" +
                                " left join subcategory on subcategory = subcategory.id"+
                                " left join vendor on pr.vendor = vendor.id "+" where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)" +
                                " else null end),char) as 'Сумма в " + year + "Q" + quarter + "',";
                        select= select+ "convert(count(distinct (" +
                                " select(if( year(date_start) = " + year + " and quarter(date_start) = " + quarter +
                                " ,tender.id" +
                                " ,null)))),char) as 'Количество в " + year + "Q" + quarter + "',";
                        column.add("Сумма в " + year + "Q" + quarter);
                        column.add("Количество в " + year + "Q" + quarter);
                    }
                }
                break;
            case"Финансовый квартал":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int quarter = 1; quarter <= 4; quarter++) {
                        select= select+ "convert(sum(case" +
                                " when YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0) = " + year + " and IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3)) = " + quarter +
                                " then"+" round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                                " left join tender on orders.tender = tender.id" +
                                " left join product_category on orders.product_category = product_category.id" +
                                " left join subcategory on subcategory = subcategory.id"+
                                " left join vendor on pr.vendor = vendor.id "+
                                " where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)" +
                                " else null end),char) as 'Сумма в " + year + "FQ" + quarter + "',";
                        select= select+ "convert(count(distinct (" +
                                " select(if( YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0) = " + year + " and IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3)) = " + quarter +
                                " ,tender.id" +
                                " ,null)))),char) as 'Количество в " + year + "FQ" + quarter + "',";;
                        column.add("Сумма в " + year + "FQ" + quarter);
                        column.add("Количество в " + year + "FQ" + quarter);
                    }
                }
                break;
            case"Месяц":
                for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                    for (int month = 1; month <= 12; month++) {
                        select= select+ "convert(sum(case" +
                                " when year(date_start) = " + year + " and month(date_start) = " + month +
                                " then"+" round(tender.sum/(select count(tender) from orders  left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                                " left join tender on orders.tender = tender.id" +
                                " left join product_category on orders.product_category = product_category.id" +
                                " left join subcategory on subcategory = subcategory.id"+
                                " left join vendor on pr.vendor = vendor.id"+
                                "  where tender = tender.id "+(!product.equals("")?"and ("+ product + ")":"")+ "),2)" +
                                " else null end),char) as 'Сумма в " + year + "M" + month + "',";
                        select= select+ "convert(count(distinct (" +
                                " select(if( year(date_start) = " + year + " and month(date_start) = " + month +
                                " ,tender.id" +
                                " ,null)))),char) as 'Количество в " + year + "M" + month + "',";

                        column.add("Сумма в " + year + "M" + month);
                        column.add("Количество в " + year + "M" + month);
                    }
                }

                break;
        }



        select= select.substring(0, select.length() - 1) + " from orders " +
                " left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                " left join tender on orders.tender = tender.id" +
                " left join product_category on orders.product_category = product_category.id" +
                " left join subcategory on subcategory = subcategory.id"+
                " left join vendor on pr.vendor = vendor.id" +
                " left join customer c on c.id = tender.customer" +
                " left join typetender t on t.id = tender.typetender" +
                " left join winner w on w.id = tender.winner" +
                " left join country on c.country = country.id" +
                " where " +
                (!tender.equals("") ?"("+tender+")":"") +
                (!product.equals("")?"and ("+ product + ")":"")+
                " group by " + (company == 0L?"c.name":"w.name");
        return new Report(null,tableMapper.Report(select),column,null);
    }

    @ApiOperation(value = "Возвращает список кварталов попадающих в переиод")
    @RequestMapping(path = "/getQuartal")
    @ResponseBody
    public List<String> getQuartal( @RequestBody SearchParameters json) {

        return reportService.getQuartal(json);
    }

    @ApiOperation(value = "Возвращает excel файл с отчетом по всем категориям продуктов")
    @PostMapping("/FileReport")
    @ResponseBody
    ResponseEntity<Resource> downloadFileReport(@RequestBody SearchParameters json) throws IOException {
        List<ProductCategory> productCategories = tableMapper.findAllProductCategory();
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

        XSSFCellStyle body = workbook.createCellStyle();
        body.setBorderTop(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        body.setBorderRight(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        body.setBorderBottom(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        body.setBorderLeft(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        body.setWrapText(true);

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 102, 204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
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
                    for (ReportQuarter reportQuarter : reportQuarters) {
                        row = sheet.createRow(rowNumber);
                        row.createCell(1).setCellValue((json.isQuarter() ? "FY" : "") + reportQuarter.getYear());
                        row.getCell(1).setCellStyle(body);
                        row.createCell(2).setCellValue("Q" + reportQuarter.getQuarter());
                        row.getCell(2).setCellStyle(body);
                        row.createCell(3).setCellValue(reportQuarter.getCount());
                        row.getCell(3).setCellStyle(body);
                        row.createCell(4).setCellValue(reportQuarter.getSum() != null ? reportQuarter.getSum().toString() : "");
                        row.getCell(4).setCellStyle(body);
                        colums.add(reportQuarter.getYear() + " " + reportQuarter.getQuarter());
                        rowNumber++;
                    }
                }
                rowNumber = rowNumber + 2;//Чтобы две строки пропустить
                ArrayList<ReportVendorQuarter> reportVendorQuarters = this.reportService.getQuartalVendorReport(productCategory.getId(), json);
                if (reportVendorQuarters.size() != 0) {
                    row = sheet.createRow(rowNumber);
                    row.createCell(1).setCellValue("Название");
                    row.getCell(1).setCellStyle(header);
                    for (int cell = 0; cell < colums.size(); cell++) {
                        row.createCell(cell + 2).setCellValue((json.isQuarter() ? "FY" : "") + colums.get(cell).replace(" ", "Q"));
                        row.getCell(cell + 2).setCellStyle(header);
                    }
                    rowNumber++;
                    for (ReportVendorQuarter reportVendorQuarter : reportVendorQuarters) {
                        row = sheet.createRow(rowNumber);
                        row.createCell(1).setCellValue(reportVendorQuarter.getVendor());
                        row.getCell(1).setCellStyle(body);
                        for (int cell = 0; cell < colums.size(); cell++) {
                            row.createCell(cell + 2).setCellStyle(body);
                            if (reportVendorQuarter.getQuarter().containsKey(colums.get(cell))) {
                                row.getCell(cell + 2).setCellValue(reportVendorQuarter.getQuarter().get(colums.get(cell)));
                            }
                        }
                        rowNumber++;
                    }
                }
                rowNumber = rowNumber + 2;//Чтобы две строки пропустить
                ArrayList<ReportVendorQuarter> reportNoVendorQuarters = this.reportService.getQuartalNoVendorReport(productCategory.getId(), json);
                if (reportVendorQuarters.size() != 0) {
                    row = sheet.createRow(rowNumber);
                    row.createCell(1).setCellValue("Название");
                    row.getCell(1).setCellStyle(header);
                    for (int cell = 0; cell < colums.size(); cell++) {
                        row.createCell(cell + 2).setCellValue((json.isQuarter() ? "FY" : "") + colums.get(cell).replace(" ", "Q"));
                        row.getCell(cell + 2).setCellStyle(header);
                    }
                    rowNumber++;
                    for (ReportVendorQuarter reportNoVendorQuarter : reportNoVendorQuarters) {
                        row = sheet.createRow(rowNumber);
                        row.createCell(1).setCellValue(reportNoVendorQuarter.getVendor());
                        row.getCell(1).setCellStyle(body);
                        for (int cell = 0; cell < colums.size(); cell++) {
                            row.createCell(cell + 2).setCellStyle(body);
                            if (reportNoVendorQuarter.getQuarter().containsKey(colums.get(cell))) {
                                row.getCell(cell + 2).setCellValue(reportNoVendorQuarter.getQuarter().get(colums.get(cell)));
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

    @ApiOperation(value = "Добавляет или изменяет продукт в категории в зависимости от наличия id")
    @PostMapping("/saveProduct/{id}")
    @ResponseBody
    List<Product> saveProduct(@RequestBody Product product, @PathVariable Long id) {
        String category = tableMapper.findNameCategoryById(id);
        String[] columns = tableMapper.findcolumnName(category);
        Long id_product;
        if (product.getId() == null) {
            StringBuilder insert = new StringBuilder("Insert into " + category + " (");
//            Insert into oscilloscope (vendor_code, frequency ,vendor, vxi, usb, channel) values(#{vendor_code}, #{frequency},#{vendor}, #{vxi},#{usb}, #{channel})
            for (String column : columns) {
                if (column.equals("id")) {
                    continue;
                }
                insert.append(" ").append(column).append(",");
            }
            insert = new StringBuilder(insert.substring(0, insert.length() - 1) + ") values (");
            for (String column : columns) {
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
                        if (product.getFrequency() == null) {
                            insert.append("").append(product.getFrequency()).append(",");
                        } else {
                            insert.append("'").append(product.getFrequency()).append("',");
                        }
                        break;
                    case "usb":
                        insert.append("").append(product.getUsb()).append(",");
                        break;
                    case "vxi":
                        insert.append("").append(product.getVxi() ).append(",");
                        break;
                    case "portable":
                        insert.append("").append(product.getPortable()).append(",");
                        break;
                    case "channel":
                        if (product.getChannel() == null) {
                            insert.append("").append(product.getChannel()).append(",");
                        } else {
                            insert.append("'").append(product.getChannel()).append("',");
                        }
                        break;
                    case "port":
                        if (product.getPort() == null) {
                            insert.append("").append(product.getPort()).append(",");
                        } else {
                            insert.append("'").append(product.getPort()).append("',");
                        }
                        break;
                    case "form_factor":
                        if (product.getForm_factor() == null) {
                            insert.append("").append(product.getForm_factor()).append(",");
                        } else {
                            insert.append("'").append(product.getForm_factor()).append("',");
                        }
                        break;
                    case "purpose":
                        if (product.getPurpose() == null) {
                            insert.append("").append(product.getPurpose()).append(",");
                        } else {
                            insert.append("'").append(product.getPurpose()).append("',");
                        }
                        break;
                    case "voltage":
                        if (product.getVoltage() == null) {
                            insert.append("").append(product.getVoltage()).append(",");
                        } else {
                            insert.append("'").append(product.getVoltage()).append("',");
                        }
                        break;
                    case "current":
                        if (product.getCurrent() == null) {
                            insert.append("").append(product.getCurrent()).append(",");
                        } else {
                            insert.append("'").append(product.getCurrent()).append("',");
                        }
                        break;
                    case "subcategory":
                        if(product.getSubcategory() == null || product.getSubcategory().equals("")){
                            insert.append("").append("null").append(",");
                        }
                        else{
                            insert.append("'").append(tableMapper.findIdSubcategory(product.getSubcategory())).append("',");
                        }
                        break;

                }
            }
            insert = new StringBuilder(insert.substring(0, insert.length() - 1) + ")");
            tableMapper.InsertProduct(insert.toString());
            id_product = tableMapper.findIdProduct("Select id from " + category + " where vendor_code ='" + product.getVendor_code() + (product.getVendor() != null?"' and vendor = '"+product.getVendor()+"'":"'"));
        }
        else {
            StringBuilder update = new StringBuilder("Update " + category + " set ");
//            Update oscilloscope set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor}, vxi = #{portable}, usb= #{usb}, channel =#{channel} where id = #{id}
            for (String column : columns) {
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
                        update.append(column).append("=");
                        if (product.getFrequency() == null) {
                            update.append("").append(product.getFrequency()).append(",");
                        } else {
                            update.append("'").append(product.getFrequency()).append("',");
                        }
                        break;
                    case "usb":
                        update.append(column).append("=");
                        if (product.getUsb() == null) {
                            update.append("").append(product.getUsb()).append(",");
                        } else {
                            update.append("").append(product.getUsb()).append(",");
                        }
                        break;
                    case "vxi":
                        update.append(column).append("=");
                        if (product.getVxi() == null) {
                            update.append("").append(product.getVxi()).append(",");
                        } else {
                            update.append("").append(product.getVxi()).append(",");
                        }

                        break;
                    case "portable":
                        update.append(column).append("=");
                        if (product.getPortable() == null) {
                            update.append("").append(product.getPortable()).append(",");
                        } else {
                            update.append("").append(product.getPortable()).append(",");
                        }
                        break;
                    case "channel":
                        update.append(column).append("=");
                        if (product.getChannel() == null) {
                            update.append("").append(product.getChannel()).append(",");
                        } else {
                            update.append("'").append(product.getChannel()).append("',");
                        }
                        break;
                    case "port":
                        update.append(column).append("=");
                        if (product.getPort() == null) {
                        update.append("").append(product.getPort()).append(",");
                    } else {
                        update.append("'").append(product.getPort()).append("',");
                    }
                        break;
                    case "form_factor":
                        update.append(column).append("=");
                        if (product.getForm_factor() == null) {
                            update.append("").append(product.getForm_factor()).append(",");
                        } else {
                            update.append("'").append(product.getForm_factor()).append("',");
                        }
                    case "purpose":
                        update.append(column).append("=");
                        if (product.getPurpose() == null) {
                            update.append("").append(product.getPurpose()).append(",");
                        } else {
                            update.append("'").append(product.getPurpose()).append("',");
                        }
                    case "voltage":
                        update.append(column).append("=");
                        if (product.getVoltage() == null) {
                            update.append("").append(product.getVoltage()).append(",");
                        } else {
                            update.append("'").append(product.getVoltage()).append("',");
                        }
                    case "current":
                        update.append(column).append("=");
                        if (product.getCurrent() == null) {
                            update.append("").append(product.getCurrent()).append(",");
                        } else {
                            update.append("'").append(product.getCurrent()).append("',");
                        }
                        break;
                    case "subcategory":
                        update.append(column).append("=");
                        if(product.getSubcategory() == null){
                            update.append("").append(product.getSubcategory()).append(",");
                        }
                        else{
                            update.append("'").append(tableMapper.findIdSubcategory(product.getSubcategory())).append("',");
                        }
                        break;
                }

            }
            update = new StringBuilder(update.substring(0, update.length() - 1) + " where id = '" + product.getId() + "'");
            tableMapper.UpdateProduct(update.toString());
            id_product = product.getId();
        }
        if(product.getOption() != null){
            List<Long> options_products = tableMapper.getAllOptionsByProduct(id,id_product);
            for(Option option:product.getOption()){
                if(options_products.contains(option.getId())){
                   options_products.remove(option.getId());
                }
                else {
                    tableMapper.insertOptionsByProduct(id,id_product,option.getId());
                }
            }
            for(Long id_option : options_products){
                tableMapper.deleteOptionsByProduct(id_option);
            }
        }
        return tableMapper.findListProduct(searchAtribut.createSelectProductCategory(id));

    }

    @ApiOperation(value = "Сохраняет информацию об основном тенедере")
    @PostMapping("/saveTender")
    @ResponseBody
    Tender saveTender(@RequestBody Tender tender) {
        try{
            Long.valueOf(tender.getCustomer());
        }
        catch (Exception e){
            tender.setCustomer(tableMapper.findCustomerByName(tender.getCustomer()).toString());
        }
        try{
            Long.valueOf(tender.getTypetender());
        }
        catch (Exception e){
            tender.setTypetender(tableMapper.findTypeTenderByType(tender.getTypetender()).toString());
        }
        tableMapper.UpdateTender(tender.getId(), tender.getName_tender(), tender.getBico_tender(), tender.getGos_zakupki(), tender.getDate_start(), tender.getDate_finish(), tender.getDate_tranding(), tender.getNumber_tender(), tender.getFull_sum(), tender.getWin_sum(), tender.getCurrency(), tender.getPrice(), tender.getRate(), tender.getPrice().multiply(BigDecimal.valueOf(tender.getRate())), Long.valueOf(tender.getCustomer()), Long.valueOf(tender.getTypetender()), Long.valueOf(tender.getWinner()), tender.isDublicate());
        return tableMapper.findTenderbyId(tender.getId());
    }

    @ApiOperation(value = "Сохраняет информацию об смежном тенедере")
    @PostMapping("/saveAdjacentTender")
    @ResponseBody
    Tender saveAdjacentTender(@RequestBody Tender tender) {
        tableMapper.UpdateAdjacentTender(tender.getId(), tender.getName_tender(), tender.getBico_tender(), tender.getGos_zakupki(), tender.getDate_start(), tender.getDate_finish(), tender.getDate_tranding(), tender.getNumber_tender(), tender.getFull_sum(), tender.getCurrency(), tender.getPrice(), tender.getRate(), tender.getPrice().multiply(BigDecimal.valueOf(tender.getRate())), Long.valueOf(tender.getCustomer()), Long.valueOf(tender.getTypetender()), tender.isDublicate());
        return tableMapper.findAdjacentTenderbyId(tender.getId());
    }

    @ApiOperation(value = "Сохраняет информацию об планах графиков тенедеров")
    @PostMapping("/savePlanTender")
    @ResponseBody
    Tender savePlanTender(@RequestBody Tender tender) {
        tableMapper.UpdatePlanTender(tender.getId(), tender.getName_tender(), tender.getBico_tender(), tender.getGos_zakupki(), tender.getDate_start(), tender.getDate_finish(), tender.getDate_tranding(), tender.getNumber_tender(), tender.getFull_sum(), tender.getCurrency(), tender.getPrice(), tender.getRate(), tender.getPrice().multiply(BigDecimal.valueOf(tender.getRate())), Long.valueOf(tender.getCustomer()), Long.valueOf(tender.getTypetender()), tender.isDublicate());
        return tableMapper.findPlanTenderbyId(tender.getId());
    }

    @ApiOperation(value = "Возвращает основной тенедер по id")
    @GetMapping("/TenderByID/{id}")
    @ResponseBody
    Tender TenderByID(@PathVariable Long id) {
        return tableMapper.findTenderbyId(id);
    }

    @ApiOperation(value = "Возвращает основной тенедер по id")
    @GetMapping("/TenderByIDForSetWinner/{id}")
    @ResponseBody
    Tender TenderByIDForSetWinner(@PathVariable Long id) throws JSONException {
        Tender tender = tableMapper.findTenderbyId(id);
        if (!tender.getWinner().equals("1")){
            return tender;
        }
        JSONObject t = bicotender.loadTender(Long.valueOf(tender.getNumber_tender()));

        if(t == null){
            return tender;
        }
        else{
            try {
                JSONObject jsonObject = new JSONObject(t.get("competitors").toString());
                Iterator iterator = jsonObject.keys();

                while (iterator.hasNext()) {
                    JSONObject json = new JSONObject(jsonObject.get(iterator.next().toString()).toString());

                    if (json.get("status").toString().equals("Победитель")) {

                        if (json.get("cost") != null) {
                            tender.setWin_sum(new BigDecimal(json.get("cost").toString()).setScale(2, BigDecimal.ROUND_CEILING));
                            if (json.get("inn") != null) {

                                if (tableMapper.findCompany(json.get("inn").toString()) != null) {
                                    Company company = tableMapper.findCompany(json.get("inn").toString());

                                    tender.setWinner(company.getId().toString());
                                    tender.setWinner_country(company.getCountry());
                                    tender.setWinner_inn(company.getInn());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e){
                return tender;
            }

            return tender;
        }

    }

    @ApiOperation(value = "Возвращает смежный тенедер по id")
    @GetMapping("/AdjacentTenderByID/{id}")
    @ResponseBody
    Tender AdjacentTenderByID(@PathVariable Long id) {
        return tableMapper.findAdjacentTenderbyId(id);
    }

    @ApiOperation(value = "Возвращает план график тенедера по id")
    @GetMapping("/PlanTenderByID/{id}")
    @ResponseBody
    Tender PlanTenderByID(@PathVariable Long id) {
        return tableMapper.findPlanTenderbyId(id);
    }

    @ApiOperation(value = "Возвращает список продуктов по id тендера")
    @PostMapping("/TenderOnProduct")
    @ResponseBody
    List<Tender> TenderOnProduct(@RequestBody TenderProduct json) {
        return tableMapper.TenderOnProduct(json.getProductCategory(), json.getDateStart(), json.getDateFinish(), json.getProduct());
    }

    @ApiOperation(value = "Возвращает список стран")
    @GetMapping("/Country")
    @ResponseBody
    List<Country> Country() {
        return tableMapper.findAllCountry();
    }

    @ApiOperation(value = "удаляет основной тендер по Id")
    @GetMapping("/DeleteTender/{tender}")
    @ResponseBody
    Map<String, String> DeleteTender(@PathVariable Long tender) {
        tableMapper.DeleteTender(tender);
        HashMap<String, String> a = new HashMap<>();
        a.put("name", "good");
        return a;
    }

    @ApiOperation(value = "удаляет смежный тендер по Id")
    @GetMapping("/DeleteAdjacentTender/{tender}")
    @ResponseBody
    Map<String, String> DeleteAdjacentTenderr(@PathVariable Long tender) {
        tableMapper.DeleteAdjacentTender(tender);
        HashMap<String, String> a = new HashMap<>();
        a.put("name", "good");
        return a;
    }

    @ApiOperation(value = "удаляет смежный тендер по Id")
    @GetMapping("/DeletePlanTender/{tender}")
    @ResponseBody
    Map<String, String> DeletePlanTenderr(@PathVariable Long tender) {
        tableMapper.DeletePlanTender(tender);
        HashMap<String, String> a = new HashMap<>();
        a.put("name", "good");
        return a;
    }

    @ApiOperation(value = "Выводит основные тендеры в excel файл с условиями поиска")
    @PostMapping("/FileTender")
    @ResponseBody
    ResponseEntity<Resource> downloadFile(@RequestBody SearchParameters json) throws IOException {
        List<Tender> tenders = tableMapper.findAllTenderTerms(searchAtribut.findTenderByTerms(json));
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet = workbook.createSheet("Станица");
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

        XSSFCellStyle hlinkstyle = workbook.createCellStyle();
        XSSFFont hlinkfont = workbook.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(new XSSFColor(new java.awt.Color(30, 144, 255)));
        hlinkstyle.setFont(hlinkfont);
        hlinkstyle.setWrapText(true);
        hlinkstyle.setBorderTop(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        hlinkstyle.setBorderRight(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        hlinkstyle.setBorderBottom(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        hlinkstyle.setBorderLeft(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);

        XSSFCellStyle body = workbook.createCellStyle();
        body.setBorderTop(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        body.setBorderRight(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        body.setBorderBottom(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        body.setBorderLeft(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        body.setWrapText(true);

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 102, 204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
        header.setFont(headerFont);

        XSSFCellStyle price = workbook.createCellStyle();
        price.setBorderTop(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        price.setBorderRight(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        price.setBorderBottom(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        price.setBorderLeft(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        price.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));


        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFDataFormat dateFormat = (XSSFDataFormat) workbook.createDataFormat();
        cellStyle.setDataFormat(dateFormat.getFormat("dd.MM.yyyy HH:mm:ss"));
//       cellStyle.setDataFormat(
//               createHelper.createDataFormat().getFormat("dd.MM.yyyy HH:mm:ss"));
        // cellStyle.setWrapText(true);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0, 39 * 256);
        sheet.setColumnWidth(1, 14 * 256);
        sheet.setColumnWidth(2, 14 * 256);
        sheet.setColumnWidth(3, 50 * 256);
        sheet.setColumnWidth(4, 39 * 256);
        sheet.setColumnWidth(5, 13 * 256);
        sheet.setColumnWidth(6, 14 * 256);
        sheet.setColumnWidth(7, 17 * 256);
        sheet.setColumnWidth(8, 5 * 256);
        sheet.setColumnWidth(9, 5 * 256);
        sheet.setColumnWidth(10, 20 * 256);
        sheet.setColumnWidth(11, 20 * 256);
        sheet.setColumnWidth(12, 12 * 256);
        sheet.setColumnWidth(13, 12 * 256);
        sheet.setColumnWidth(14, 12 * 256);
        sheet.setColumnWidth(15, 56 * 256);
        sheet.setColumnWidth(16, 39 * 256);
        sheet.setColumnWidth(17, 20 * 256);
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
        for (Tender tender : tenders) {
            numberRow += 1;
            row = sheet.createRow(numberRow);
            row.setHeight((short) -1);
            row.createCell(0).setCellValue(tender.getCustomer());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(tender.getInn());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(tender.getCountry());
            row.getCell(2).setCellStyle(body);

            row.createCell(3).setCellValue(tender.getName_tender());
            XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(tender.getBico_tender());
            row.getCell(0).setCellStyle(body);
            row.getCell(3).setHyperlink((XSSFHyperlink) link);
            row.getCell(3).setCellStyle(hlinkstyle);
            XSSFHyperlink linkGos = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
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

            if (tender.getDate_tranding() != null) {
                row.createCell(14).setCellValue(tender.getDate_tranding().toLocalDateTime().format(format_dateFile));
                row.getCell(14).setCellStyle(body);

            } else {
                row.createCell(14).setCellValue("");
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

    @ApiOperation(value = "Выводит смежные тендеры в excel файл с условиями поиска")
    @PostMapping("/FileAdjacentTender")
    @ResponseBody
    ResponseEntity<Resource> downloadFileAdjacentTender(@RequestBody SearchParameters json) throws IOException {
        List<Tender> tenders = tableMapper.findAllAdjacentTenderTerms(searchAtribut.findTenderByTerms(json));
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet = workbook.createSheet("Станица");
        XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

        XSSFCellStyle hlinkstyle = workbook.createCellStyle();
        XSSFFont hlinkfont = workbook.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(new XSSFColor(new java.awt.Color(30, 144, 255)));
        hlinkstyle.setFont(hlinkfont);
        hlinkstyle.setWrapText(true);
        hlinkstyle.setBorderTop(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        hlinkstyle.setBorderRight(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        hlinkstyle.setBorderBottom(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        hlinkstyle.setBorderLeft(BorderStyle.THIN);
        hlinkstyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);

        XSSFCellStyle body = workbook.createCellStyle();
        body.setBorderTop(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        body.setBorderRight(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        body.setBorderBottom(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        body.setBorderLeft(BorderStyle.THIN);
        body.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        body.setWrapText(true);

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 102, 204)));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
        header.setFont(headerFont);

        XSSFCellStyle price = workbook.createCellStyle();
        price.setBorderTop(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        price.setBorderRight(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        price.setBorderBottom(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        price.setBorderLeft(BorderStyle.THIN);
        price.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        price.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));


        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFDataFormat dateFormat = (XSSFDataFormat) workbook.createDataFormat();
        cellStyle.setDataFormat(dateFormat.getFormat("dd.MM.yyyy HH:mm:ss"));
//       cellStyle.setDataFormat(
//               createHelper.createDataFormat().getFormat("dd.MM.yyyy HH:mm:ss"));
        // cellStyle.setWrapText(true);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
        int numberRow = 0;
        XSSFRow row = sheet.createRow(numberRow);
        sheet.setColumnWidth(0, 39 * 256);
        sheet.setColumnWidth(1, 14 * 256);
        sheet.setColumnWidth(2, 14 * 256);
        sheet.setColumnWidth(3, 50 * 256);
        sheet.setColumnWidth(4, 39 * 256);
        sheet.setColumnWidth(5, 13 * 256);
        sheet.setColumnWidth(6, 14 * 256);
        sheet.setColumnWidth(7, 17 * 256);
        sheet.setColumnWidth(8, 5 * 256);
        sheet.setColumnWidth(9, 5 * 256);
        sheet.setColumnWidth(10, 20 * 256);
        sheet.setColumnWidth(11, 20 * 256);
        sheet.setColumnWidth(12, 12 * 256);
        sheet.setColumnWidth(13, 12 * 256);
        sheet.setColumnWidth(14, 12 * 256);
        sheet.setColumnWidth(15, 56 * 256);
        sheet.setColumnWidth(16, 39 * 256);
        sheet.setColumnWidth(17, 20 * 256);
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
        for (Tender tender : tenders) {
            numberRow += 1;
            row = sheet.createRow(numberRow);
            row.setHeight((short) -1);
            row.createCell(0).setCellValue(tender.getCustomer());
            row.getCell(0).setCellStyle(body);
            row.createCell(1).setCellValue(tender.getInn());
            row.getCell(1).setCellStyle(body);
            row.createCell(2).setCellValue(tender.getCountry());
            row.getCell(2).setCellStyle(body);

            row.createCell(3).setCellValue(tender.getName_tender());
            XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(tender.getBico_tender());
            row.getCell(0).setCellStyle(body);
            row.getCell(3).setHyperlink((XSSFHyperlink) link);
            row.getCell(3).setCellStyle(hlinkstyle);
            XSSFHyperlink linkGos = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
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

            if (tender.getDate_tranding() != null) {
                row.createCell(14).setCellValue(tender.getDate_tranding().toLocalDateTime().format(format_dateFile));
                row.getCell(14).setCellStyle(body);

            } else {
                row.createCell(14).setCellValue("");
                row.getCell(14).setCellStyle(body);
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

    @ApiOperation(value = "Выводит список продкутов в категории в excel файл ")
    @GetMapping("/ProductFile/{category}")
    @ResponseBody
    ResponseEntity<Resource> downloadProductFile(@PathVariable Long category) throws IOException {
        List<Product> products = tableMapper.findListProduct(searchAtribut.createSelectProductCategory(category));
        List<Product> productsNoUses = tableMapper.findListProduct(searchAtribut.createSelectProductNoUses(category));
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            String[] columns = tableMapper.findcolumnName(tableMapper.findNameCategoryById(category));

            XSSFWorkbook workbook = new XSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();
            XSSFSheet sheet = workbook.createSheet("Страница");
            XSSFColor colorborder = new XSSFColor(new java.awt.Color(0, 90, 170));

            XSSFCellStyle body = workbook.createCellStyle();
            body.setBorderTop(BorderStyle.THIN);
            body.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
            body.setBorderRight(BorderStyle.THIN);
            body.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
            body.setBorderBottom(BorderStyle.THIN);
            body.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
            body.setBorderLeft(BorderStyle.THIN);
            body.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
            body.setWrapText(true);

            XSSFCellStyle dublicate = workbook.createCellStyle();
            dublicate.setBorderTop(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.TOP, colorborder);
            dublicate.setBorderRight(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, colorborder);
            dublicate.setBorderBottom(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, colorborder);
            dublicate.setBorderLeft(BorderStyle.THIN);
            dublicate.setBorderColor(XSSFCellBorder.BorderSide.LEFT, colorborder);
            dublicate.setWrapText(true);
            dublicate.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 215, 64)));
            dublicate.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle header = workbook.createCellStyle();
            header.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 102, 204)));
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
            header.setFont(headerFont);
            header.setWrapText(true);


            int numberRow = 0;
            XSSFRow row = sheet.createRow(numberRow);

            int numberColumn = 0;
            HashMap<String, Integer> column = new HashMap<String, Integer>();
            if (products.get(0).getId() != null) {
                sheet.setColumnWidth(numberColumn, 5 * 256);
                row.createCell(numberColumn).setCellValue("ID");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("id", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getPortable() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("Подкатегория");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("subcategory", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getVendor() != null) {
                sheet.setColumnWidth(numberColumn, 14 * 256);
                row.createCell(numberColumn).setCellValue("Вендор");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("vendor", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getVendor_code() != null) {
                sheet.setColumnWidth(numberColumn, 50 * 256);
                row.createCell(numberColumn).setCellValue("Артикул/Название");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("vendor_code", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getFrequency() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("Частота/Полоса пропускания");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("frequency", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getChannel() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("Каналы");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("channel", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getPort() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("Порты");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("port", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getUsb() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("USB");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("usb", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getVxi() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("VXI");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("vxi", numberColumn);
                numberColumn++;
            }
            if (products.get(0).getPortable() != null) {
                sheet.setColumnWidth(numberColumn, 17 * 256);
                row.createCell(numberColumn).setCellValue("Портативный");
                row.getCell(numberColumn).setCellStyle(header);
                column.put("portable", numberColumn);
            }

            for (Product product : products) {

                numberRow += 1;
                row = sheet.createRow(numberRow);
                row.setHeight((short) -1);
                boolean flag = false;
                for (String col : columns) {
                    if (column.containsKey(col)) {

                        XSSFCell cell = row.createCell(column.get(col));
                        switch (col) {
                            case "id":
                                if (productsNoUses.contains(product)) {
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
                                if(product.getFrequency()!= null){
                                    cell.setCellValue(product.getFrequency());
                                }
                                ;
                                break;
                            case "usb":
                                if(product.getUsb() != null){
                                    cell.setCellValue(product.getUsb());
                                }
                                break;
                            case "vxi":
                                if(product.getVxi() != null){
                                    cell.setCellValue(product.getVxi());
                                }

                                break;
                            case "portable":
                                if(product.getPortable() != null){
                                    cell.setCellValue(product.getPortable());
                                }
                                break;
                            case "channel":
                                if(product.getChannel() != null){
                                    cell.setCellValue(product.getChannel());
                                }

                                break;
                            case "port":
                                if(product.getPort() != null){
                                    cell.setCellValue(product.getPort());
                                }
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

    @ApiOperation(value = "Создает новую таблицу с указанными параметрами",  notes = "Добавляет запись в таблицу \"Категории пролдуктов\" после чего создает новую таблицу с параметрами которые были заданны" +
            "в ситеме и добавляет первоначальную запись \"Без артикула\"")
    @PostMapping("/CreateTable")
    @ResponseBody
    Map<String, String> CreateTable(@RequestBody NewTable table) throws JSONException {
        tableMapper.InsertCategory(table.getName(), table.getName_en(), table.getCategory());
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
        String create = "Create table " + table.getName_en() + " (`id` BIGINT NOT NULL AUTO_INCREMENT, ";
        if(table.getSubcategory() != null){
            create = create + "`subcategory` BIGINT,";
        }
        if (table.isVendor()) {
            create = create + "`vendor` BIGINT NOT NULL DEFAULT 1,INDEX `vendor_idx` (`vendor` ASC), CONSTRAINT `vendor_" + table.getName_en() + "` FOREIGN KEY (`vendor`) REFERENCES `keysight`.`vendor` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,";
        }
        create = create + "`vendor_code` VARCHAR(225) NOT NULL, ";
        if (table.isFrequency()) {
            create = create + "`frequency` DOUBLE ,";
        }
        if (table.isVoltage()) {
            create = create + "`voltage` DOUBLE ,";
        }
        if (table.isCurrent()) {
            create = create + "`current` DOUBLE ,";
        }
        if (table.isChannel()) {
            create = create + "`channel` INT  ,";
        }
        if (table.isPort()) {
            create = create + "`port` INT ,";
        }
        if (table.isUsb()) {
            create = create + "`usb` TINYINT ,";
        }
        if (table.isVxi()) {
            create = create + "`vxi` TINYINT ,";
        }
        if (table.isPortable()) {
            create = create + "`portable` TINYINT,";
        }
        if(table.isForm_factor()){
            create= create + "`form_factor` VARCHAR(225), ";
        }
        if(table.isPurpose()){
            create= create + "`purpose` VARCHAR(225), ";
        }

        create = create + " PRIMARY KEY (`id`));";
        tableMapper.CreateTable(create);
        tableMapper.InsertProduct("Insert into " + table.getName_en() + "(vendor_code" + (table.isVendor() ? ",vendor)" : ")") + " values ('Без артикула'" + (table.isVendor() ? ",'1')" : ")"));
        if(table.getSubcategory() != null){
            for(String sub : table.getSubcategory()){
                tableMapper.InsertProduct("Insert into " + table.getName_en() + "(vendor_code,subcategory" + (table.isVendor() ? ",vendor)" : ")") + " values ('Без артикула','" +tableMapper.findIdSubcategory(sub)+"'"+ (table.isVendor() ? ",'1')" : ")"));
            }
        }


        HashMap<String, String> answear = new HashMap<>();
        answear.put("name", table.getName());
        return answear;
    }

    @ApiOperation(value = "НЕ ИСПОЛЬЗУЕТСЯ!!!!!!")
    @GetMapping("/ChangeAnalizator")
    @ResponseBody
    String RemoveProduct() {
        List<Product> FirstProduct = tableMapper.findListProduct("Select * from spectrum_analyser");
        for (Product product : FirstProduct) {
            Long id;

            if(!product.getPortable()){
                if (tableMapper.findIdProduct( "Select id from signal_analyzer as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"") == null) {
                    tableMapper.InsertProduct("insert into signal_analyzer (vendor,vendor_code,subcategory,frequency) values ('"+product.getVendor()+"','"+product.getVendor_code()+"',"+(product.getSubcategory() != null?"'"+product.getSubcategory()+"'":"null")+","+(product.getFrequency() != null?"'"+product.getFrequency()+"'":"null")+")");
                    id = tableMapper.findIdProduct("Select id from signal_analyzer as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor= " + product.getVendor()+"");
                } else {
                    id = tableMapper.findIdProduct("Select id from signal_analyzer as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"");
                }
            }
            else{
                if (tableMapper.findIdProduct( "Select id from portable_analyzers as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"") == null) {
                    tableMapper.InsertProduct("insert into portable_analyzers (vendor,vendor_code,subcategory,frequency) values ('"+product.getVendor()+"','"+product.getVendor_code()+"',"+(product.getSubcategory() != null?"'"+product.getSubcategory()+"'":"null")+","+(product.getFrequency() != null?"'"+product.getFrequency()+"'":"null")+")");
                    id = tableMapper.findIdProduct("Select id from portable_analyzers as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"");
                } else {
                    id = tableMapper.findIdProduct("Select id from portable_analyzers as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"");
                }

            }
//            Insert into oscilloscope (vendor_code, frequency ,vendor, vxi, usb, channel) values(#{vendor_code}, #{frequency},#{vendor}, #{vxi},#{usb}, #{channel})

            List<Long> orders = tableMapper.findAllOrdersIdbyProduct(product.getId(), 1L);
            for (Long order : orders) {
                if(!product.getPortable()){
                    tableMapper.ChangeProduct(order, id, 4L);
                }
                else{
                    tableMapper.ChangeProduct(order, id, 11L);
                }
            }
        }
        return "good";
    }

    @ApiOperation(value = "Заменяет один продукт на другой")
    @PostMapping("/ChangeCategory")
    @ResponseBody
    Map<String, String> ChangeCategory(@RequestBody ChangeCategory changeCategory) {
        List<Long> firstProduct = tableMapper.findAllOrdersIdbyProduct(changeCategory.getVendor_code(), changeCategory.getCategory());
        for (Long id : firstProduct) {
            tableMapper.ChangeProduct(id, changeCategory.getNewVendor_code(), changeCategory.getNewCategory());
            searchAtribut.UpdateProductTender(tableMapper.findTenderIdbyId(id));
        }

        HashMap<String, String> a = new HashMap<>();
        a.put("name", "Заменил");
        return a;
    }

    @ApiOperation(value = "Функция для тестирования каких-то функций и возможностей")
    @GetMapping(path = "/Test")
    @ResponseBody
    Long Test() throws JSONException {
        tableMapper.insertOrder(new OrdersDB(null, 1L, 7L, 7L, "", 1, new BigDecimal(0), new BigDecimal(0), null, null,null,null,null,null,null,null,null,null,null,null,null,null,null));
        return tableMapper.checkId();
    }

    @ApiOperation(value = "Выводит всю информацию из фала log.txt")
    @GetMapping(path = "/Log")
    @ResponseBody
    Map<String, String> Log() {
        HashMap<String, String> a = new HashMap<>();
        String answear = "";
        try (FileInputStream fin = new FileInputStream(log)) {
            int i = -1;
            while ((i = fin.read()) != -1) {
                answear = answear + (char) i;

            }
        } catch (IOException ex) {


        }
        a.put("name", answear);
        return a;
    }

    @ApiOperation(value = "Временная функция которая использовалась для дополнения заказчикам инн")
    @GetMapping(path = "/ADDINN")
    @ResponseBody
    Map<String, String> ADDINN() throws JSONException {
        HashMap<String, String> a = new HashMap<>();

        List<Long> customers = tableMapper.CustomersZeroINN();
        for (Long customer : customers) {
            String Bico = tableMapper.BicoNumberbyCustomer(customer);
            try{
                JSONObject tender = bicotender.loadTender(Long.valueOf(Bico.trim()));
                try{JSONObject company = new JSONObject(tender.get("company").toString());
                    if (!company.get("inn").toString().equals("null")) {
                        tableMapper.updateCustomerInnAndCountry(company.get("inn").toString(), customer);
                        a.put(customer.toString(),company.get("inn").toString());
                    }
                }
                catch (JSONException e ){
                    a.put(customer.toString(),e.getMessage()+"  company");
                }


            }
            catch (Exception e) {
                a.put(customer.toString(),e.getMessage());
            }

        }
        return a;
}

    @ApiOperation(value = "Позваляет выполнить select запрос к бд и получить ответ")
    @PostMapping(path = "/SelectQuery")
    @ResponseBody
    List<Map<Object,Object>> SelectQuery(@RequestBody String query) throws JSONException {
        return tableMapper.selectQuery(query);
    }

    @ApiOperation(value = "Возвращает список синонимов к категориям")
    @GetMapping(path = "/SynonymsProduct")
    @ResponseBody
    List<SynonymsProduct> SynonymsProduct() {
        return tableMapper.findAllSynonymsProduct();
    }

    @ApiOperation(value = "Изменяет или добавляет новые синонимы к категории")
    @PostMapping(path = "/ChangeSynonymsProduct")
    @ResponseBody
    List<SynonymsProduct> ChangeSynonymsProduct(@RequestBody SynonymsProduct synonymsProduct){
        if(synonymsProduct.getId() != null){
            tableMapper.UpdateSynonymsProduct(synonymsProduct.getId(),synonymsProduct.getId_category(),synonymsProduct.getSynonyms());
        }
        else{
            tableMapper.InsertSynonymsProduct(synonymsProduct.getId_category(),synonymsProduct.getSynonyms());
        }
        return tableMapper.findAllSynonymsProduct();
    }

    @ApiOperation(value = "Возвращает список больших категорий")
    @GetMapping(path = "/BigCategory")
    @ResponseBody
    List<BigCategory> BigCategory() {
        List<BigCategory> bigCategory = new LinkedList<>();
        List<Long> ids = tableMapper.findAllBigCategory();
        if(ids != null){
            for(Long id :ids){
                bigCategory.add(searchAtribut.makeBigCategory(id));
            }
        }

        return bigCategory;
    }

    @ApiOperation(value = "Изменяет или добавляет большую категорию")
    @PostMapping(path = "/ChangeBigCategory")
    @ResponseBody
    List<BigCategory> ChangeBigCategory(@RequestBody BigCategory big){

        if(big.getBig_category_id() != null){
            boolean flag = false;
            List<Long> productCategorys = tableMapper.findCategorybyBigCategory(big.getBig_category_id());
            List<Long> webIds = new LinkedList<>();
            for(ProductCategory productCategory : big.getCategory()){
                webIds.add(productCategory.getId());
                if(!productCategorys.contains(productCategory.getId())){
                    tableMapper.InsertBig_category_dependencies(big.getBig_category_id(),productCategory.getId());
                }
            }
            for(Long id : productCategorys){
                if(!webIds.contains(id)){
                tableMapper.DeleteBig_category_dependencies(big.getBig_category_id(),id);
                }
            }
        }
        else{
            tableMapper.InsertBigCategory(big.getBig_category());
            Long bigCategoryId = tableMapper.findBigCategorybyName(big.getBig_category());
            for(ProductCategory productCategory: big.getCategory()){
                tableMapper.InsertBig_category_dependencies(bigCategoryId,productCategory.getId());
            }
        }

        List<BigCategory> bigCategory = new LinkedList<>();
        List<Long> ids = tableMapper.findAllBigCategory();
        if(ids != null){
            for(Long id :ids){
                bigCategory.add(searchAtribut.makeBigCategory(id));
            }
        }

        return bigCategory;
    }

    @ApiOperation(value = "Временная функция, которая проверяет названия тендоров")
    @GetMapping("/changeNameTender")
    @ResponseBody
    Map<String,String> changeNameTender() throws JSONException {
        List<Tender> tenders = tableMapper.findNameTenderByDate();
        String result = "";
        for(Tender tender:tenders){

            JSONObject tender_bico = bicotender.loadTender(Long.valueOf(tender.getNumber_tender().trim()));

            if(!tender.getName_tender().equals(tender_bico.get("name").toString())){
                tableMapper.changeNameTender(tender.getId(),tender_bico.get("name").toString());
                result = result+ tender.getId()+" ";
            }
        }
        HashMap<String, String> a = new HashMap<>();
        a.put("change:",result);
        return a;
    }

    @ApiOperation(value = "Возвращает список пользвателей для отображения его в комментариях")
    @GetMapping("/getAllUsers")
    @ResponseBody
    List<User> getAllUsers() {

        return tableMapper.findUsers();
    }

    @ApiOperation(value = "Возвращает список комментариев к тендеру")
    @GetMapping("/getCommentsByTender/{tender}")
    @ResponseBody
    List<Comment> getCommentsByTender(@PathVariable Long tender) {

        return tableMapper.findAllCommentsByTender(tender);
    }

    @ApiOperation(value = "Добавляет комментарий к тендеру")
    @PostMapping(path = "/postComment")
    @ResponseBody
    List<Comment> PostComment(@RequestBody Comment comment){
        for(Long id: comment.getUsers()){
            String mail = tableMapper.findUserById(id);
            String message = "<div>Пользователь " +comment.getUser()+ " оставил вам комментарий к тендеру "+ comment.getTender()+ "</div>" +
                    "<div style=\"padding: 10px;" +
                    "  border-radius: 5px;" +
                    "  margin-bottom: 1px;" +
                    "  border: 1px solid darkgrey;width: fit-content;margin:20px;\">" +comment.getText()+"</div>" +
                    "<div><a href=\""+url.substring(1,url.length()-1)+"/tender/"+comment.getTender()+"\" target=\"_blank\">"+(url.substring(1,url.length()-1)+"/tender/"+comment.getTender())+"</a></div>";
            try {
                mailSender.send(mail, "Добавлен комментарий в приложении \"Application Tender\" к тендеру "+comment.getTender(), message);
                }
            catch (Exception e){
            }
            }
        comment.setDate(ZonedDateTime.now().plusHours(3));
        tableMapper.insertComment(comment.getText(),comment.getUsr(),comment.getDate(),comment.getTender());
        return tableMapper.findAllCommentsByTender(comment.getTender());
    }

    @ApiOperation(value = "Возвращает количество комментариев у тендера")
    @GetMapping("/CountCommentByTender/{tender}")
    @ResponseBody
    Long CountCommentByTender(@PathVariable Long tender) {

        return tableMapper.CountCommentByTender(tender);
    }

    @ApiOperation(value = "Возвращает подкатегории по категории в системе")
    @GetMapping("/subcategoryInCategory/{category}")
    @ResponseBody
    String[] subcategoryInCategory(@PathVariable Long category) {
        if (Arrays.asList(tableMapper.findcolumnName(tableMapper.findOneCategoryENById(category))).contains("subcategory")){
            return tableMapper.subcategoryInCategory(tableMapper.findOneCategoryENById(category));
        }
       else{
           return  null;
        }
    }

    @ApiOperation(value = "Возвращает подкатегории в системе")
    @GetMapping("/AllSubcategory")
    @ResponseBody
    String[] allSubcategory() {
       return tableMapper.findSubcategory();
    }

    @ApiOperation(value = "Возвращает название колонок в системе")
    @GetMapping("/ColumnCategory/{category}")
    @ResponseBody
    String[] ColumnCategory(@PathVariable Long category) {
        return tableMapper.findcolumnName(tableMapper.findNameCategoryById(category));
    }

    @ApiOperation(value = "Возвращает Список Опций в системе")
    @GetMapping("/getAllOptions")
    @ResponseBody
    List<Option> getAllOptions() {
        return tableMapper.getAllOptions();
    }

    @ApiOperation(value = "Добавляет Опцию в таблицу с опциями")
    @PostMapping(path = "/Saveoption")
    @ResponseBody
    List<Option> saveOption(@RequestBody Option option) {

        if(tableMapper.CheckOption(option.getName()) == null){
            tableMapper.insertOptions(option.getName());
            return tableMapper.getAllOptions();
        }
        else{
            return null;
        }

    }

    @ApiOperation(value = "Возвращает Список Опций по продукту")
    @GetMapping("/getAllOptionsByProduct/{product_category}/{id_product}")
    @ResponseBody
    List<Option> getAllOptionsByProduct(@PathVariable Long product_category,@PathVariable Long id_product) {
        return tableMapper.getAllOptionsByProductForOrders(product_category, id_product);
    }

    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
    @GetMapping("/changeGenerator")
    @ResponseBody
    List<String> ChangeGenerator() {
        List<String> answear = new ArrayList<>();
        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(269L,2L);
        for(OrdersDB order : ordersDBS){
        //boolean flag = false;
        String str = order.getComment().toLowerCase();
        int index = -1;
        if (str.contains("гц")) {
            boolean flag = str.charAt(str.lastIndexOf("гц") - 1) == 'м';
            boolean flagK = str.charAt(str.lastIndexOf("гц") - 1) == 'к';

            for (int x = str.lastIndexOf("гц") - 2; x >= 0; x--) {

                if (flagK ) {
                    break;
                } else if (Character.isLetter(str.charAt(x))) {
                    index = x;
                    break;
                } else if (!Character.isLetterOrDigit(str.charAt(x))) {
                    if (str.charAt(x) == ')' || str.charAt(x) == '(') {
                        index = x;
                        break;
                    } else if (x != 0 && !Character.isDigit(str.charAt(x - 1))) {
                        index = x;
                        break;
                    }

                }
            }
            try {
                if (flagK ) {
                    answear.add(order.getTender().toString());

                } else {
                    String a = (index != -1 ? str.substring(0, index).trim() : "") +
                            (str.lastIndexOf("гц") + 2 != str.length() ? str.substring(str.lastIndexOf("гц") + 2, str.length() - 1).trim() : "");
                    Double x = Double.parseDouble(str.substring(index + 1, str.lastIndexOf("гц") - 1).replace(',', '.'));
                    x = flag ? x / 1000 : x;
                    if (x < 1) {
                        tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,10L,a,x);
                    }
                    else{
                        tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),269L,2L,a,x);
                    }
                }

            } catch (Exception e) {
                answear.add(order.getTender().toString());
            }

        }
        }
    return answear;
    }

    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
    @GetMapping("/changeOscilloscope")
    @ResponseBody
    List<String> ChangeOscilloscope() {
        List<String> answear = new ArrayList<>();
        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(506L,6L);
        for(OrdersDB order : ordersDBS){
            //boolean flag = false;
            String str = order.getComment().toLowerCase();
            int index = -1;
            if (str.contains("гц")) {
                boolean flag = str.charAt(str.lastIndexOf("гц") - 1) == 'м';
                boolean flagK = str.charAt(str.lastIndexOf("гц") - 1) == 'к';

                for (int x = str.lastIndexOf("гц") - 2; x >= 0; x--) {

                    if (flagK ) {
                        break;
                    } else if (Character.isLetter(str.charAt(x))) {
                        index = x;
                        break;
                    } else if (!Character.isLetterOrDigit(str.charAt(x))) {
                        if (str.charAt(x) == ')' || str.charAt(x) == '(') {
                            index = x;
                            break;
                        } else if (x != 0 && !Character.isDigit(str.charAt(x - 1))) {
                            index = x;
                            break;
                        }

                    }
                }
                try {
                    if (flagK ) {
                        answear.add(order.getTender().toString());

                    }
                    else {
                        String a = (index != -1 ? str.substring(0, index).trim() : "") +
                                (str.lastIndexOf("гц") + 2 != str.length() ? str.substring(str.lastIndexOf("гц") + 2, str.length() - 1).trim() : "");
                        Double x = Double.parseDouble(str.substring(index + 1, str.lastIndexOf("гц") - 1).replace(',', '.'));
                        x = flag ? x / 1000 : x;
                        if (x <= 0.5) {
                            tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,9L,a,x);
                        }
                        else{
                            tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),506L,6L,a,x);
                        }
                    }

                } catch (Exception e) {
                    answear.add(order.getTender().toString());
                }

            }
        }
        return answear;
    }

    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
    @GetMapping("/changeOscilloscopeFrequency")
    @ResponseBody
    List<String> ChangeOscilloscopeFrequency() {
        List<String> answear = new ArrayList<>();
        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(506L,6L);
        for(OrdersDB order : ordersDBS){
            try{
            if(order.getFrequency() != null){
                if(order.getFrequency() <= 0.5 ){
                    tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,9L,order.getComment(),order.getFrequency());
                }
            }


                } catch (Exception e) {
                    answear.add(order.getTender().toString());
                }


        }
        return answear;
    }

    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
    @GetMapping("/changeGeneratorFrequency")
    @ResponseBody
    List<String> ChangeGeneratorFrequency() {
        List<String> answear = new ArrayList<>();
        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(269L,2L);
        for(OrdersDB order : ordersDBS){
            try{
                if(order.getFrequency() != null){
                    if(order.getFrequency() <= 1 ){
                        tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,10L,order.getComment(),order.getFrequency());
                    }
                }


            } catch (Exception e) {
                answear.add(order.getTender().toString());
            }


        }
        return answear;
    }

    @ApiOperation(value = "Добавляет продукты в категорию из excel файла", notes = "Данная функция перенеосит продукты в другую категорию")
    @RequestMapping(value = "/ChangeProduct/{category}/{oldcategory}", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    @ResponseBody
    Map<String, String> ChangeProduct(@PathVariable Long category,@PathVariable Long oldcategory,MultipartFile excel) throws IOException, InvalidFormatException {


        File temp = new File(pathname);
        String nameOldCategory = tableMapper.findNameCategoryById(oldcategory);
        String nameCategory = tableMapper.findNameCategoryById(category);
        excel.transferTo(temp);
        //InputStream ExcelFileToRead= new InputStreamReader(new FileInputStream(temp), "UTF-8");
        XSSFWorkbook workbook = new XSSFWorkbook(temp);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map<String, String> a = new HashMap<>();
        a.put("name","Загрузил");
        ProductCategory productCategory = tableMapper.findCategoryById(category);
        int count = 1;
        while ( sheet.getRow(count) != null && sheet.getRow(count).getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null ) {
            XSSFRow row = sheet.getRow(count);
            if(row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null && row.getCell(1).getStringCellValue().trim().equals("Общего назначения")) {

                Product product = tableMapper.findOneProduct("Select * from "+nameOldCategory+" where id = "+row.getCell(0).getStringCellValue()+" limit 1");
                if(product != null){
                    if(tableMapper.findIdProduct("Select id from "+nameCategory+" where vendor_code = '"+product.getVendor_code()+"' and vendor = "+product.getVendor()) == null){
                        tableMapper.InsertProduct("Insert into "+nameCategory+" (vendor_code,vendor) values ('"+product.getVendor_code()+"',"+product.getVendor()+")");
                    }
                    Long id = tableMapper.findIdProduct("Select id from "+nameCategory+" where vendor_code = '"+product.getVendor_code()+"' and vendor = "+product.getVendor());
                    tableMapper.UpdateProductAll("Update orders set id_product = "+id+", product_category = "+category + " where id_product= "+product.getId()+" and product_category = "+oldcategory);
                }

            }
            count++;
        }

        //ExcelFileToRead.close();

        return a;
    }

    @ApiOperation(value = "Собирает одно таблицу с продуктами")
    @GetMapping("/CreateProductTable")
    @ResponseBody
    List<String> CreateProductTable(){
        List<String> a = new ArrayList<>();
        List<ProductCategory> productCategories = tableMapper.findAllProductCategory();
        for(ProductCategory productCategory : productCategories){

            List<Product> products = tableMapper.findListProduct(searchAtribut.createSelectProductCategory(productCategory.getId()));
            for(Product product :products){
                product.setProduct_category_id(productCategory.getId());
                if(product.getVendor_id() == null){
                    product.setVendor_id(1L);
                }
                    if(tableMapper.CheckProduct(product) == null){
                        try {
                            tableMapper.InsertIntoProduct(product);
                        }
                        catch (Exception e){

                            a.add(product.getProduct_category()+" "+ product.getId());
                        }

                    }


            }
        }
        return a;
    }

    @ApiOperation(value ="Новая функция для вывода отчетов")
    @PostMapping("/ReportProduct")
    @ResponseBody
    Report ReportProduct(@RequestBody ReportCriteria reportCriteria){
        if(reportCriteria.getSearchParameters().getDateStart() == null){
            reportCriteria.getSearchParameters().setDateStart(ZonedDateTime.parse("01.01.2018 00:00:00 Z",format_date));
        }
        if(reportCriteria.getSearchParameters().getDateFinish() == null){
            reportCriteria.getSearchParameters().setDateFinish(ZonedDateTime.now());
        }
        Map<String,Double> kurs = new HashMap<>();
        kurs.put("2018",62.6906);
        kurs.put("2019",64.6625);
        kurs.put("2020",72.1260);
        kurs.put("2021",73.7123);
        String nameYear = "Год";
        String tender = searchAtribut.WhereWithoutProduct(reportCriteria.getSearchParameters()).substring(5);
        String tenderPeriod = searchAtribut.ParametrsWithoutProductAndDate(reportCriteria.getSearchParameters()).substring(5);
        String product = searchAtribut.searchTenderByProduct(reportCriteria.getSearchParameters().getProduct());
        String selectProduct = "Select vendor.name as 'Вендор', convert(sum(case" +
                                                           " when orders.product_category <> 13 "+
                                                           " then orders.number" +
                                                           " else null end),char) as 'Сумма', ";
        List<String> columnProduct = new ArrayList<String>();
        columnProduct.add("Вендор");
        columnProduct.add("Сумма");
        List<String> columnTender = new ArrayList<String>();
        String selectTenderForTable = selectTenderForTable = "Select convert(SUM(round(sum/(select count(tender)" +
                " from orders" +
                " left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                " left join product_category on orders.product_category = product_category.id" +
                " left join subcategory on subcategory = subcategory.id"+
                " left join vendor on pr.vendor = vendor.id" +
                " where orders.tender = tender.id " +
                (!product.equals("")?" and ("+ product + ")":"") + "),2)),char) as 'Сумма'," +
                " convert(count(distinct orders.tender),char) as 'Количество тендеров',";

        String groupByForTable = "";
            switch (reportCriteria.getInterval()){
                case "Год":
                    for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                        selectProduct= selectProduct+ "convert(sum(case" +
                                " when year(date_start) = " + year + " and orders.product_category <> 13" +
                                " then orders.number" +
                                " else null end),char) as '" + year + "',";
//                        selectTenderForChart = selectTenderForChart + "year(date_start) as year, "
                        columnProduct.add(String.valueOf(year));
                    }
                    nameYear = "Год";
                    selectTenderForTable = selectTenderForTable+"convert(year(date_start),char) as 'Год'";
                    columnTender.add("Год");
                    groupByForTable = "year(date_start)";
                    break;
                case "Финансовый год":
                    for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                        selectProduct= selectProduct+ "convert(sum(case" +
                                " when YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0) = " + year + " and orders.product_category <> 13" +
                                " then orders.number" +
                                " else null end),char) as '" + year + "',";
                        columnProduct.add(String.valueOf(year));
                    }
                    nameYear = "Финансовый год";
                    selectTenderForTable = selectTenderForTable + "convert(YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0),char) as 'Финансовый год'";
                    columnTender.add("Финансовый год");
                    groupByForTable = "YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0)";
                    break;
                case "Неделя":
                    for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                        for (int week = 1; week <= 52; week++) {
                            selectProduct= selectProduct+ "convert(sum(case" +
                                    " when year(date_start) = " + year + " and week(date_start) = " + week + " and orders.product_category <> 13" +
                                    " then orders.number" +
                                    " else null end),char) as '" + year + "W" + week + "',";
                            columnProduct.add(year + "W" + week);
                        }
                    }
                    nameYear = "Год";
                    selectTenderForTable = selectTenderForTable + "convert(year(date_start),char) as 'Год', convert(week(date_start),char) as 'Неделя'";
                    columnTender.add("Год");
                    columnTender.add("Неделя");
                    groupByForTable = "year(date_start), week(date_start)";
                    break;
                case "Квартал":
                    for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                        for (int quarter = 1; quarter <= 4; quarter++) {
                            selectProduct= selectProduct+ "convert(sum(case" +
                                    " when year(date_start) = " + year + " and quarter(date_start) = " + quarter + " and orders.product_category <> 13" +
                                    " then orders.number" +
                                    " else null end),char) as '" + year + "Q" + quarter + "',";
                            columnProduct.add(year + "Q" + quarter);
                        }
                    }
                    nameYear = "Год";
                    selectTenderForTable = selectTenderForTable + "convert(year(date_start),char) as 'Год', convert(quarter(date_start),char) as 'Квартал'";
                    columnTender.add("Год");
                    columnTender.add("Квартал");
                    groupByForTable = "year(date_start), quarter(date_start)";
                    break;
                case"Финансовый квартал":
                    for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                        for (int quarter = 1; quarter <= 4; quarter++) {
                            selectProduct= selectProduct+ "convert(sum(case" +
                                    " when YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0) = " + year + " and IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3)) = " + quarter + " and orders.product_category <> 13" +
                                    " then orders.number" +
                                    " else null end),char) as '" + year + "FQ" + quarter + "',";
                            columnProduct.add(year + "FQ" + quarter);
                        }
                    }
                    nameYear = "Финансовый год";
                    selectTenderForTable = selectTenderForTable + "convert(YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0),char) as 'Финансовый год', convert(IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3)),char) as 'Финансовый квартал'";
                    columnTender.add("Финансовый год");
                    columnTender.add("Финансовый квартал");
                    groupByForTable = "YEAR(date_start) + IF(MONTH(date_start)>10, 1, 0), IF(MONTH(date_start)>10, 1, CEIL((MONTH(date_start)+2)/3))";
                    break;
                case"Месяц":
                    for(int year = reportCriteria.getSearchParameters().getDateStart().getYear();year<=reportCriteria.getSearchParameters().getDateFinish().getYear();year++) {
                        for (int month = 1; month <= 12; month++) {
                            selectProduct= selectProduct+ "convert(sum(case" +
                                    " when year(date_start) = " + year + " and month(date_start) = " + month + " and orders.product_category <> 13" +
                                    " then orders.number" +
                                    " else null end),char) as '" + year + "M" + month + "',";
                            columnProduct.add(year + "M" + month);
                        }
                    }
                    nameYear = "Год";
                    selectTenderForTable = selectTenderForTable + "convert(year(date_start),char) as 'year', convert(month(date_start),char) as 'Месяц'";
                    columnTender.add("Год");
                    columnTender.add("Месяц");
                    groupByForTable = "year(date_start), month(date_start)";
                    break;
            }
        columnTender.add("Количество тендеров");
            columnTender.add("Сумма");
        columnTender.add("Сумма в долларах");



            selectProduct= selectProduct.substring(0, selectProduct.length() - 1) + " from orders " +
                    " left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                    " left join tender on orders.tender = tender.id" +
                    " left join product_category on orders.product_category = product_category.id" +
                    " left join subcategory on subcategory = subcategory.id"+
                    " left join vendor on pr.vendor = vendor.id" +
                    " left join customer c on c.id = tender.customer" +
                    " left join typetender t on t.id = tender.typetender" +
                    " left join winner w on w.id = tender.winner" +
                    " left join country on c.country = country.id" +
                    " where " +
                    (!tender.equals("") ?"("+tender+")":"") +
                    (!product.equals("")?" and ("+ product + ")":"") +
                    " group by pr.vendor";
            selectTenderForTable = selectTenderForTable + " from orders " +
        " left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
                " left join tender on orders.tender = tender.id" +
                " left join product_category on orders.product_category = product_category.id" +
                " left join subcategory on subcategory = subcategory.id"+
                " left join vendor on pr.vendor = vendor.id" +
                    " left join customer c on c.id = tender.customer" +
                    " left join typetender t on t.id = tender.typetender" +
                    " left join winner w on w.id = tender.winner" +
                    " left join country on c.country = country.id" +
                " where " +
                (!tender.equals("") ?"("+tender+")":"") +
                (!product.equals("")?" and ("+ product + ")":"") +" group by " +groupByForTable + " order by " + groupByForTable ;
        List<Map<String,String>> tenderTable = tableMapper.Report(selectTenderForTable);
        for(Map<String,String> a : tenderTable){
            try{
                BigDecimal b = BigDecimal.valueOf(Double.parseDouble(a.get("Сумма")) / kurs.get(a.get(nameYear))).setScale(2, BigDecimal.ROUND_CEILING) ;
                a.put("Сумма в долларах",b.toString());
            }catch (Exception e){
                a.put("Сумма в долларах","0");
            }

        }
        return new Report(tenderTable,tableMapper.Report(selectProduct),columnProduct,columnTender);
    }

    @ApiOperation(value = "Сохраненные посики")
    @GetMapping("/SaveParameters")
    @ResponseBody
    List<SearchParameters> AllSearchParametrs() throws JsonProcessingException {
        Jsonb jsonb = JsonbBuilder.create();
        List<SearchParameters> searchParametersList = new ArrayList<>();
        List<SearchParametersFromDB> searchParametersFromDBS = tableMapper.search_parameters();
        for(SearchParametersFromDB parameters : searchParametersFromDBS){
            searchParametersList.add(new SearchParameters(parameters.getId(),
                    parameters.getNickname(),
                    parameters.getName(),
                    parameters.getDateStart(),
                    parameters.getDateFinish(),
                    parameters.isDublicate(),
                    parameters.isQuarter(),
                    parameters.isTypeExclude(),
                    jsonb.fromJson(parameters.getType(), new ArrayList<TypeTender>(){}.getClass().getGenericSuperclass()),
                    parameters.isCustomExclude(),
                    jsonb.fromJson(parameters.getCustom(),new ArrayList<Company>(){}.getClass().getGenericSuperclass()),
                    parameters.getInnCustomer(),
                    parameters.getCountry(),
                    parameters.isWinnerExclude(),
                    jsonb.fromJson(parameters.getWinner(),new ArrayList<Company>(){}.getClass().getGenericSuperclass()),
                    parameters.getMinSum(),
                    parameters.getMaxSum(),
                    parameters.getIds(),
                    parameters.getBicotender(),
                    parameters.isNumberShow(),
                    jsonb.fromJson(parameters.getProduct(),new ArrayList<ProductReceived>(){}.getClass().getGenericSuperclass()),
                    parameters.getRegion() != null?jsonb.fromJson(parameters.getRegion(), new ArrayList<Region>(){}.getClass().getGenericSuperclass()):null,
                    parameters.getDistrict() != null? jsonb.fromJson(parameters.getDistrict(),new ArrayList<District>(){}.getClass().getGenericSuperclass()): null,
                    parameters.isPlan_schedule(),
                    parameters.isRealized(),
                    parameters.isAdjacent_tender(),
                    parameters.isPrivate_search()
                    )
            );
        }

        return searchParametersList;
    }


    @ApiOperation(value = "Сохранить поиск")
    @PostMapping("/save_SaveParameters")
    @ResponseBody
    List<SearchParameters> save_SaveParameters(@RequestBody SearchParameters searchParameters) throws JsonProcessingException {
        Jsonb jsonb = JsonbBuilder.create();
        Integer idSearchParameters = tableMapper.countSearchParametersByName(searchParameters.getName());
        if(idSearchParameters == null || idSearchParameters == 0){
            tableMapper.saveParameters(searchParameters.getNickname(),
                    searchParameters.getName(),
                    searchParameters.getDateStart(),
                    searchParameters.getDateFinish(),
                    searchParameters.isDublicate(),
                    searchParameters.isTypeExclude(),
                    jsonb.toJson(searchParameters.getType()),
                    searchParameters.isCustomExclude(),
                    jsonb.toJson(searchParameters.getCustom()),
                    searchParameters.getInnCustomer(),
                    searchParameters.getCountry(),
                    searchParameters.isWinnerExclude(),
                    jsonb.toJson(searchParameters.getWinner()),
                    searchParameters.getMinSum(),
                    searchParameters.getMaxSum(),
                    Arrays.toString(searchParameters.getIds()),
                    Arrays.toString(searchParameters.getBicotender()),
                    searchParameters.isNumberShow(),
                    jsonb.toJson(searchParameters.getProduct()),
                    searchParameters.getRegions() != null?jsonb.toJson(searchParameters.getRegions()):null,
                    searchParameters.getDistricts() != null? jsonb.toJson(searchParameters.getDistricts()): null,
                    searchParameters.isPlan_schedule(),
                    searchParameters.isRealized(),
                    searchParameters.isAdjacent_tender(),
                    searchParameters.isPrivate_search()
            );
        }
        else if (idSearchParameters == 1){
            tableMapper.updateParameters(tableMapper.idSearchParametersByName(searchParameters.getName()),
                    searchParameters.getNickname(),
                    searchParameters.getName(),
                    searchParameters.getDateStart(),
                    searchParameters.getDateFinish(),
                    searchParameters.isDublicate(),
                    searchParameters.isTypeExclude(),
                    jsonb.toJson(searchParameters.getType()),
                    searchParameters.isCustomExclude(),
                    jsonb.toJson(searchParameters.getCustom()),
                    searchParameters.getInnCustomer(),
                    searchParameters.getCountry(),
                    searchParameters.isWinnerExclude(),
                    jsonb.toJson(searchParameters.getWinner()),
                    searchParameters.getMinSum(),
                    searchParameters.getMaxSum(),
                    Arrays.toString(searchParameters.getIds()),
                    Arrays.toString(searchParameters.getBicotender()),
                    searchParameters.isNumberShow(),
                    jsonb.toJson(searchParameters.getProduct()),
                    searchParameters.getRegions() != null?jsonb.toJson(searchParameters.getRegions()):null,
                    searchParameters.getDistricts() != null? jsonb.toJson(searchParameters.getDistricts()): null,
                    searchParameters.isPlan_schedule(),
                    searchParameters.isRealized(),
                    searchParameters.isAdjacent_tender(),
                    searchParameters.isPrivate_search()
            );
        }


        List<SearchParameters> searchParametersList = new ArrayList<>();
        List<SearchParametersFromDB> searchParametersFromDBS = tableMapper.search_parameters();
        for(SearchParametersFromDB parameters : searchParametersFromDBS){
            searchParametersList.add(new SearchParameters(parameters.getId(),
                    parameters.getNickname(),
                    parameters.getName(),
                    parameters.getDateStart(),
                    parameters.getDateFinish(),
                    parameters.isDublicate(),
                    parameters.isQuarter(),
                    parameters.isTypeExclude(),
                    jsonb.fromJson(parameters.getType(), new ArrayList<TypeTender>(){}.getClass().getGenericSuperclass()),
                    parameters.isCustomExclude(),
                    jsonb.fromJson(parameters.getCustom(),new ArrayList<Company>(){}.getClass().getGenericSuperclass()),
                    parameters.getInnCustomer(),
                    parameters.getCountry(),
                    parameters.isWinnerExclude(),
                    jsonb.fromJson(parameters.getWinner(),new ArrayList<Company>(){}.getClass().getGenericSuperclass()),
                    parameters.getMinSum(),
                    parameters.getMaxSum(),
                    parameters.getIds(),
                    parameters.getBicotender(),
                    parameters.isNumberShow(),
                    jsonb.fromJson(parameters.getProduct(),new ArrayList<ProductReceived>(){}.getClass().getGenericSuperclass()),
                    parameters.getRegion() != null?jsonb.fromJson(parameters.getRegion(), new ArrayList<Region>(){}.getClass().getGenericSuperclass()):null,
                    parameters.getDistrict() != null? jsonb.fromJson(parameters.getDistrict(),new ArrayList<District>(){}.getClass().getGenericSuperclass()): null,
                    searchParameters.isPlan_schedule(),
                    searchParameters.isRealized(),
                    searchParameters.isAdjacent_tender(),
                    searchParameters.isPrivate_search()
                    )
            );
        }

        return searchParametersList;
    }

    @ApiOperation(value = "Удалить поиск")
    @PostMapping("/delete_SaveParameters")
    @ResponseBody
    List<SearchParameters> delete_SaveParameters(@RequestBody Long id) throws JsonProcessingException {
        Jsonb jsonb = JsonbBuilder.create();
        if(tableMapper.countSearchParametersById(id) > 0 ){
            tableMapper.deleteSearchParametersById(id);
        }
        List<SearchParameters> searchParametersList = new ArrayList<>();
        List<SearchParametersFromDB> searchParametersFromDBS = tableMapper.search_parameters();
        for(SearchParametersFromDB parameters : searchParametersFromDBS){
            searchParametersList.add(new SearchParameters(parameters.getId(),
                    parameters.getNickname(),
                    parameters.getName(),
                    parameters.getDateStart(),
                    parameters.getDateFinish(),
                    parameters.isDublicate(),
                    parameters.isQuarter(),
                    parameters.isTypeExclude(),
                    jsonb.fromJson(parameters.getType(), new ArrayList<TypeTender>(){}.getClass().getGenericSuperclass()),
                    parameters.isCustomExclude(),
                    jsonb.fromJson(parameters.getCustom(),new ArrayList<Company>(){}.getClass().getGenericSuperclass()),
                    parameters.getInnCustomer(),
                    parameters.getCountry(),
                    parameters.isWinnerExclude(),
                    jsonb.fromJson(parameters.getWinner(),new ArrayList<Company>(){}.getClass().getGenericSuperclass()),
                    parameters.getMinSum(),
                    parameters.getMaxSum(),
                    parameters.getIds(),
                    parameters.getBicotender(),
                    parameters.isNumberShow(),
                    jsonb.fromJson(parameters.getProduct(),new ArrayList<ProductReceived>(){}.getClass().getGenericSuperclass()),
                    parameters.getRegion() != null?jsonb.fromJson(parameters.getRegion(), new ArrayList<Region>(){}.getClass().getGenericSuperclass()):null,
                    parameters.getDistrict() != null? jsonb.fromJson(parameters.getDistrict(),new ArrayList<District>(){}.getClass().getGenericSuperclass()): null,
                    parameters.isPlan_schedule(),
                    parameters.isRealized(),
                    parameters.isAdjacent_tender(),
                    parameters.isPrivate_search()
                    )
            );
        }

        return searchParametersList;
    }

    @ApiOperation(value = "Возвращает список регионов")
    @GetMapping("/Region")
    @ResponseBody
    List<Region> Region() {

        return tableMapper.selectRegion();
    }

    @ApiOperation(value = "Возвращает список округов")
    @GetMapping("/District")
    @ResponseBody
    List<District> District() {
        return tableMapper.selectDistrict();
    }

    @ApiOperation(value = "Устанавливает связь тендера с дубликатом")
    @GetMapping("/setDublicate/{id}/{id_d}")
    @ResponseBody
    String setDublicate(@PathVariable Long id,@PathVariable Long id_d) {

        tableMapper.changeDublicate(id_d);
        if(tableMapper.CheckDublicate(id,id_d) == null){
            tableMapper.insertDublicate(id,id_d);
        }

        return "true";
    }

    @ApiOperation(value = "Устанавливает связь тендера с планом графика")
    @GetMapping("/setPlane/{id}/{id_d}")
    @ResponseBody
    String setPlane(@PathVariable Long id,@PathVariable Long id_d) {


        if(tableMapper.CheckPlane(id,id_d) == null){
            tableMapper.insertPlane(id,id_d);
        }

        return "true";
    }

    @ApiOperation(value = "Удаляет связь тендера с дубликатом")
    @GetMapping("/deleteDublicate/{id}")
    @ResponseBody
    String deleteDublicate(@PathVariable Long id) {
        tableMapper.deleteDublicate(id);
        tableMapper.delete_tender_Dublicate(id);
        return "true";
    }

    @ApiOperation(value = "Удаляет связь тендера с дубликатом")
    @GetMapping("/getDublicate/{id}")
    @ResponseBody
    List<Tender> getDublicate(@PathVariable Long id) {
        Tender tender = tableMapper.findTenderbyId(id);
        return tableMapper.SelectDublicate(tender.getFull_sum(),tender.getInn(),tender.getDate_start().format(format_Dublicate));
    }
}

