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
import javax.json.Json;
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

import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/demo")
public class ApiController {
    private final DateTimeFormatter format_date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
    private final DateTimeFormatter format_dateFile = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter format_API_Bico = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private final DateTimeFormatter format_Dublicate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
    @Autowired
    private Bicotender bicotender;

    public ApiController(TableMapper tableMapper, FileService fileService, ReportService reportService) {
        this.tableMapper = tableMapper;
        this.fileService = fileService;
        this.reportService = reportService;
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

    @ApiOperation(value = "Возвращает список всех категорий продуктов", notes = "Выполняет запрос к БД и возвращает названия и id всех категорий")
    @GetMapping("/ProductCategory")
    @ResponseBody
    List<ProductCategory> ProductCategory() {
        return tableMapper.findAllProductCategory();
    }

    @ApiOperation(value = "Возвращает список продуктов из категории", notes = "Запускает функцию формирования запроса для выборки всех продуктов из категории по id ее в таблице product_category")
    @GetMapping("/VendorCode/{id}")
    @ResponseBody
    List<Product> Product(@PathVariable Long id) {
            return tableMapper.findListProduct(searchAtribut.createSelectProductCategory(id));
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
            tableMapper.DeleteProduct(product.getId());
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
    List<Orders> OrdersByTender(@PathVariable Long tender) {

        return searchAtribut.generateOrders(tender);
    }

    @ApiOperation(value = "Список продуктов в тендере по его id", notes = "Возвращает список продуктов в тендере, использая только id продуктов")
    @GetMapping("/OrdersBDByTender/{tender}")
    @ResponseBody
    List<OrdersDB> OrdersBDByTender(@PathVariable Long tender) {
        return tableMapper.findAllOrdersBDbyTender(tender);
    }



    @ApiOperation(value = "Добавляет продукты в категорию из excel файла", notes = "Данная функция нужна для выделение категорий из Продуктов, помогает быстро создать и перенести продукты в новую категорию")
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

        int count = 1;
        while ( sheet.getRow(count) != null && sheet.getRow(count).getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null ) {
            XSSFRow row = sheet.getRow(count);

            if(row.getCell(0).getNumericCellValue() != 1){

                String vendor_code = row.getCell(2).toString().substring(row.getCell(2).toString().lastIndexOf(" ")).trim();
                if(tableMapper.findIdProduct("Select id from product where vendor_code ='" + vendor_code + "' and product_category = '"+category+"' and vendor = '"+searchAtribut.findVendor(row.getCell(2).toString().substring(0,row.getCell(2).toString().lastIndexOf(" ")))+"'") == null){
                    tableMapper.InsertProduct("Insert into product (vendor_code,vendor) values ('" + vendor_code + "'" + ",'" + searchAtribut.findVendor(row.getCell(2).toString().substring(0,row.getCell(2).toString().lastIndexOf(" "))) + "')");
                }
                Long id = tableMapper.findIdProduct("Select id from product where vendor_code ='" + vendor_code + "' and product_category = '"+category+"' and vendor = '"+searchAtribut.findVendor(row.getCell(2).toString().substring(0,row.getCell(2).toString().lastIndexOf(" ")))+"'");
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
                tableMapper.ChangeProduct(id_order, tableMapper.FindFirstProductInCategory(category));
                searchAtribut.UpdateProductTender(tableMapper.findTenderIdbyId(id_order));
            }

            count++;
        }

        //ExcelFileToRead.close();

        return a;
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
    List<Orders> Tender(@RequestBody OrdersDB json) {

        if(json.getId() == null){
            tableMapper.insertOrder(json);
        }
        else{
            tableMapper.updateOrder(json);
        }
        searchAtribut.UpdateProductTender(json.getTender());
        return searchAtribut.generateOrders(json.getTender());
    }

    @ApiOperation(value = "Возвращает продкты строкой", notes = "Переводит продукты в список продуктов")
    @PostMapping("/getOrdersString")
    @ResponseBody
    ResponseEntity retutnStringProductByTender(@RequestBody List<Orders> json) {
        return ResponseEntity.ok(Json.createObjectBuilder().add("name", searchAtribut.generateProductString(json)).build().toString());
    }

    @ApiOperation(value = "Разделение тендера", notes = "Разделение тендера на под тендеры")
    @PostMapping("/divdeTender")
    @ResponseBody
    ResponseEntity divdeTender(@RequestBody List<DivedeTenderDTO> divdeTender) {
        tableMapper.changeDublicate(divdeTender.get(0).getTender().getId());
        for(DivedeTenderDTO divedeTenderDTO : divdeTender){
            Tender tender = divedeTenderDTO.getTender();
            tableMapper.insertTender(tender.getName_tender(),
                    tender.getBico_tender(),
                    tender.getGos_zakupki(),
                    tender.getDate_start(),
                    tender.getDate_finish(),
                    tender.getDate_tranding(),
                    tender.getNumber_tender() + "_" + (divdeTender.indexOf(divedeTenderDTO)+1),
                    tender.getPrice(),
                    new BigDecimal(0),
                    tender.getCurrency(),
                    tender.getPrice(),
                    tender.getRate(),
                    tender.getPrice().multiply(BigDecimal.valueOf(tender.getRate())),
                    searchAtribut.findCustomer("null", tender.getCustomer()),
                    searchAtribut.findTypetender(tender.getTypetender()),
                    1L
            );
            Long id = tableMapper.findTenderByNumber_tender(tender.getNumber_tender() + "_" + (divdeTender.indexOf(divedeTenderDTO)+1));
            for(Orders orders : divedeTenderDTO.getOrders()){
//                Long id, Long tender, Long product, String comment, int number,
//                BigDecimal price, Long vendor, Double frequency, Boolean usb,
//                Boolean vxi, Boolean portable, Integer channel, Integer port,
//                String form_factor, String purpose, Double voltage, Double current,
//                String subcategory, Long subcategory_id, Option[] option, String options
                tableMapper.insertOrder(new OrdersDB(null, id, orders.getProduct_DB(), orders.getComment(), orders.getNumber(),
                        orders.getPrice(), orders.getVendor_DB(), orders.getFrequency(), orders.getUsb(),
                        orders.getVxi(), orders.getPortable(), orders.getChannel(), orders.getPort(),
                        orders.getForm_factor(), orders.getPurpose(), orders.getVoltage(), orders.getCurrent(),
                        orders.getSubcategory(), orders.getSubcategory_DB(), orders.getOption(), orders.getOptions()));

                searchAtribut.UpdateProductTender(id);
            }

        }
        return ResponseEntity.ok(Json.createObjectBuilder().add("name","ok").build().toString());
    }


    @ApiOperation(value = "Добавляет или изменяет информацию о продуктах в тендере", notes = "Сравнивает список продуктов который пришел и который есть в БД и изменяет если есть изменения, удаляет лишние из БД и Добавляет нужные в БД")
    @PostMapping("/deleteOrders")
    @ResponseBody
    List<Orders> deleteOrders(@RequestBody deleteOrder json) {
        System.out.println(json.toString());
        if(!json.getResult()){
            Orders deleteOrder = tableMapper.findProductbyId(json.getId());
            Orders anotherProduct = tableMapper.findAnotherProductbyTender(json.getTender());
            if(anotherProduct != null){

                anotherProduct.setComment_DB("(" + searchAtribut.returnItems(Integer.parseInt(anotherProduct.getComment_DB().replaceAll("\\D", ""))+1)+")");
                anotherProduct.setNumber(anotherProduct.getNumber()+deleteOrder.getNumber());
                tableMapper.UpdateAnotherProduct(anotherProduct.getComment_DB(),anotherProduct.getNumber(),anotherProduct.getId());
            }
            else{
                tableMapper.InsertNewAnother(json.getTender());
            }
        }
        tableMapper.deleteOrder(json.getId());
        searchAtribut.UpdateProductTender(json.getTender());
        return searchAtribut.generateOrders(json.getTender());
    }
    @ApiOperation(value = "Возвращает количество тендеров по кварталам и сумму данных тендеров в категории")
    @RequestMapping(path = "/quarterTender/{category}")
    @ResponseBody
    public ArrayList<ReportQuarter> getQuartalTenderReport(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalTenderReport(category, json);
    }


    @ApiOperation(value = "Возвращает количество упоминаний продукта в тендерах по кварталам у определеного вендора")
    @RequestMapping(path = "/quarterVendor/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalVendorReport(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalVendorReport(category, json);
    }


    @ApiOperation(value = "Возвращает количество упоминаний комментария к продуктам у которых артикул \"Без артикула\"")
    @RequestMapping(path = "/quarterNoVendor/{category}")
    @ResponseBody
    public ArrayList<ReportVendorQuarter> getQuartalNoVendorReport(@PathVariable Long category, @RequestBody SearchParameters json) {

        return reportService.getQuartalNoVendorReport(category, json);
    }


    @ApiOperation(value = "Возвращает количество упоминаний компании в тендерах по кварталам ")
    @RequestMapping(path = "/quarterCustomer/{company}")
    @ResponseBody
    public Report getQuartalCustomerReport(@PathVariable Long company, @RequestBody ReportCriteria reportCriteria) {
        Object[] data = reportService.selectForReportCompany(company, reportCriteria);
        return new Report(null,tableMapper.Report((String) data[0]) , (List<String>) data[1],null);
    }

    @ApiOperation(value = "Возвращает запрос для вывода упоминаний компании в тендерах по кварталам ")
    @RequestMapping(path = "/quarterCustomer/request/{company}")
    @ResponseBody
    public String getQuartalCustomerQueryReport(@PathVariable Long company, @RequestBody ReportCriteria reportCriteria) {
        Object[] data = reportService.selectForReportCompany(company, reportCriteria);
        return Json.createObjectBuilder().add("name", (String) data[0]).build().toString();
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
    @PostMapping("/saveProduct")
    @ResponseBody
    List<Product> saveProduct(@RequestBody Product product) {
        if (product.getId() == null) {
            tableMapper.InsertIntoProduct(product);
        }
        else {
            tableMapper.UpdateInProduct(product);
        }

        return tableMapper.findListProduct(searchAtribut.createSelectProductCategory(product.getProduct_category_id())
        );

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
        if(table.getSubcategory() != null){
            table.setSubcategory_boolean(true);
        }
        tableMapper.InsertCategory(table);
        Long category_id = tableMapper.FindIdCategoryByName_en(table.getName_en());
        tableMapper.InsertProduct("Insert into product (vendor_code,vendor,product_category) values ('Без артикула','1','"+category_id +"')");
        if(table.getSubcategory() != null){
            for(String sub : table.getSubcategory()){
                tableMapper.InsertProduct("Insert into product (vendor_code,subcategory,vendor,product_category) values ('Без артикула','" +tableMapper.findIdSubcategory(sub)+"','1','"+category_id+"')");
            }
        }


        HashMap<String, String> answear = new HashMap<>();
        answear.put("name", table.getName());
        return answear;
    }

//    @ApiOperation(value = "НЕ ИСПОЛЬЗУЕТСЯ!!!!!!")
//    @GetMapping("/ChangeAnalizator")
//    @ResponseBody
//    String RemoveProduct() {
//        List<Product> FirstProduct = tableMapper.findListProduct("Select * from spectrum_analyser");
//        for (Product product : FirstProduct) {
//            Long id;
//
//            if(!product.getPortable()){
//                if (tableMapper.findIdProduct( "Select id from signal_analyzer as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"") == null) {
//                    tableMapper.InsertProduct("insert into signal_analyzer (vendor,vendor_code,subcategory,frequency) values ('"+product.getVendor()+"','"+product.getVendor_code()+"',"+(product.getSubcategory() != null?"'"+product.getSubcategory()+"'":"null")+","+(product.getFrequency() != null?"'"+product.getFrequency()+"'":"null")+")");
//                    id = tableMapper.findIdProduct("Select id from signal_analyzer as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor= " + product.getVendor()+"");
//                } else {
//                    id = tableMapper.findIdProduct("Select id from signal_analyzer as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"");
//                }
//            }
//            else{
//                if (tableMapper.findIdProduct( "Select id from portable_analyzers as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"") == null) {
//                    tableMapper.InsertProduct("insert into portable_analyzers (vendor,vendor_code,subcategory,frequency) values ('"+product.getVendor()+"','"+product.getVendor_code()+"',"+(product.getSubcategory() != null?"'"+product.getSubcategory()+"'":"null")+","+(product.getFrequency() != null?"'"+product.getFrequency()+"'":"null")+")");
//                    id = tableMapper.findIdProduct("Select id from portable_analyzers as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"");
//                } else {
//                    id = tableMapper.findIdProduct("Select id from portable_analyzers as pr where pr.vendor_code ='" + product.getVendor_code() + "' and pr.vendor = " + product.getVendor()+"");
//                }
//
//            }
////            Insert into oscilloscope (vendor_code, frequency ,vendor, vxi, usb, channel) values(#{vendor_code}, #{frequency},#{vendor}, #{vxi},#{usb}, #{channel})
//
//            List<Long> orders = tableMapper.findAllOrdersIdbyProduct(product.getId(), 1L);
//            for (Long order : orders) {
//                if(!product.getPortable()){
//                    tableMapper.ChangeProduct(order, id, 4L);
//                }
//                else{
//                    tableMapper.ChangeProduct(order, id, 11L);
//                }
//            }
//        }
//        return "good";
//    }

    @ApiOperation(value = "Заменяет один продукт на другой")
    @PostMapping("/ChangeCategory")
    @ResponseBody
    Map<String, String> ChangeCategory(@RequestBody ChangeCategory changeCategory) {
        List<Long> firstProduct = tableMapper.findAllOrdersIdbyProduct(changeCategory.getVendor_code());
        for (Long id : firstProduct) {
            tableMapper.ChangeProduct(id, changeCategory.getNewVendor_code());
            searchAtribut.UpdateProductTender(tableMapper.findTenderIdbyId(id));
        }

        HashMap<String, String> a = new HashMap<>();
        a.put("name", "Заменил");
        return a;
    }

    @ApiOperation(value = "Функция для тестирования каких-то функций и возможностей")
    @GetMapping(path = "/Test")
    @ResponseBody
    Object Test() throws JSONException {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.of(2018,1,1), LocalTime.of(0,0), ZoneId.of("UTC"));
        while ( !ZonedDateTime.now().minusDays(1).isBefore(zonedDateTime) ){
           if(tableMapper.RateByDate(zonedDateTime) == null) {
               Map<String, Double> currency = new HashMap<>();
               currency = getCurrency.currency(zonedDateTime.format(formatCurrency));
               double rate = currency.get("USD");
               tableMapper.InsertRate(zonedDateTime, rate);
           }
            System.out.println(zonedDateTime);
            zonedDateTime = zonedDateTime.plusDays(1);
        }

        return 1;
    }
//    private static class TestModel{
//        private String name;
//        private Double value;
//
//        public TestModel(String name, Double value) {
//            this.name = name;
//            this.value = value;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public Double getValue() {
//            return value;
//        }
//
//        public void setValue(Double value) {
//            this.value = value;
//        }
//    }
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

    @ApiOperation(value = "Возвращает список комментариев к тендеру")
    @GetMapping("/getCommentsForUser/{user}")
    @ResponseBody
    List<Comment> getCommentsForUser(@PathVariable Long user) {

        return tableMapper.findAllCommentsForUser(user);
    }

    @ApiOperation(value = "Добавляет комментарий к тендеру")
    @PostMapping(path = "/postComment")
    @ResponseBody
    List<Comment> PostComment(@RequestBody Comment comment){
        comment.setDate(ZonedDateTime.now().plusHours(3));
        tableMapper.insertComment(comment.getText(),comment.getUsr(),comment.getDate(),comment.getTender());

        Long id_comment = tableMapper.GetCommentId(comment.getTender(),comment.getDate());
        for(Long id: comment.getUsers()){
            tableMapper.InsertCommentForUser(id_comment,id);
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
    List<String> ColumnCategory(@PathVariable Long category) {
        List<String> column = new ArrayList<>();
        if(category != 0L) {
            ProductCategory productCategory = tableMapper.findOneCategoryFullById(category);
            column.add("id");
            if (productCategory.getSubcategory()) {
                column.add("subcategory");
            }
            if (category != 7L) {
                column.add("vendor");
            }
            column.add("vendor_code");
            if (productCategory.getFrequency()) {
                column.add("frequency");
            }
            if (productCategory.getUsb()) {
                column.add("usb");
            }
            if (productCategory.getVxi()) {
                column.add("vxi");
            }
            if (productCategory.getPortable()) {
                column.add("portable");
            }
            if (productCategory.getChannel()) {
                column.add("channel");
            }
            if (productCategory.getPort()) {
                column.add("port");
            }
            if (productCategory.getForm_factor()) {
                column.add("form_factor");
            }
            if (productCategory.getPurpose()) {
                column.add("purpose");
            }
            if (productCategory.getVoltage()) {
                column.add("voltage");
            }
            if (productCategory.getCurrent()) {
                column.add("current");
            }
        }
        else{
            column.add("id");
            column.add("subcategory");
            column.add("vendor");
            column.add("vendor_code");
            column.add("frequency");
            column.add("usb");
            column.add("vxi");
            column.add("portable");
            column.add("channel");
            column.add("port");
            column.add("form_factor");
            column.add("purpose");
            column.add("voltage");
            column.add("current");
        }
        return column;
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

//    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
//    @GetMapping("/changeGenerator")
//    @ResponseBody
//    List<String> ChangeGenerator() {
//        List<String> answear = new ArrayList<>();
//        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(269L,2L);
//        for(OrdersDB order : ordersDBS){
//        //boolean flag = false;
//        String str = order.getComment().toLowerCase();
//        int index = -1;
//        if (str.contains("гц")) {
//            boolean flag = str.charAt(str.lastIndexOf("гц") - 1) == 'м';
//            boolean flagK = str.charAt(str.lastIndexOf("гц") - 1) == 'к';
//
//            for (int x = str.lastIndexOf("гц") - 2; x >= 0; x--) {
//
//                if (flagK ) {
//                    break;
//                } else if (Character.isLetter(str.charAt(x))) {
//                    index = x;
//                    break;
//                } else if (!Character.isLetterOrDigit(str.charAt(x))) {
//                    if (str.charAt(x) == ')' || str.charAt(x) == '(') {
//                        index = x;
//                        break;
//                    } else if (x != 0 && !Character.isDigit(str.charAt(x - 1))) {
//                        index = x;
//                        break;
//                    }
//
//                }
//            }
//            try {
//                if (flagK ) {
//                    answear.add(order.getTender().toString());
//
//                } else {
//                    String a = (index != -1 ? str.substring(0, index).trim() : "") +
//                            (str.lastIndexOf("гц") + 2 != str.length() ? str.substring(str.lastIndexOf("гц") + 2, str.length() - 1).trim() : "");
//                    Double x = Double.parseDouble(str.substring(index + 1, str.lastIndexOf("гц") - 1).replace(',', '.'));
//                    x = flag ? x / 1000 : x;
//                    if (x < 1) {
//                        tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,10L,a,x);
//                    }
//                    else{
//                        tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),269L,2L,a,x);
//                    }
//                }
//
//            } catch (Exception e) {
//                answear.add(order.getTender().toString());
//            }
//
//        }
//        }
//    return answear;
//    }
//
//    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
//    @GetMapping("/changeOscilloscope")
//    @ResponseBody
//    List<String> ChangeOscilloscope() {
//        List<String> answear = new ArrayList<>();
//        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(506L,6L);
//        for(OrdersDB order : ordersDBS){
//            //boolean flag = false;
//            String str = order.getComment().toLowerCase();
//            int index = -1;
//            if (str.contains("гц")) {
//                boolean flag = str.charAt(str.lastIndexOf("гц") - 1) == 'м';
//                boolean flagK = str.charAt(str.lastIndexOf("гц") - 1) == 'к';
//
//                for (int x = str.lastIndexOf("гц") - 2; x >= 0; x--) {
//
//                    if (flagK ) {
//                        break;
//                    } else if (Character.isLetter(str.charAt(x))) {
//                        index = x;
//                        break;
//                    } else if (!Character.isLetterOrDigit(str.charAt(x))) {
//                        if (str.charAt(x) == ')' || str.charAt(x) == '(') {
//                            index = x;
//                            break;
//                        } else if (x != 0 && !Character.isDigit(str.charAt(x - 1))) {
//                            index = x;
//                            break;
//                        }
//
//                    }
//                }
//                try {
//                    if (flagK ) {
//                        answear.add(order.getTender().toString());
//
//                    }
//                    else {
//                        String a = (index != -1 ? str.substring(0, index).trim() : "") +
//                                (str.lastIndexOf("гц") + 2 != str.length() ? str.substring(str.lastIndexOf("гц") + 2, str.length() - 1).trim() : "");
//                        Double x = Double.parseDouble(str.substring(index + 1, str.lastIndexOf("гц") - 1).replace(',', '.'));
//                        x = flag ? x / 1000 : x;
//                        if (x <= 0.5) {
//                            tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,9L,a,x);
//                        }
//                        else{
//                            tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),506L,6L,a,x);
//                        }
//                    }
//
//                } catch (Exception e) {
//                    answear.add(order.getTender().toString());
//                }
//
//            }
//        }
//        return answear;
//    }

//    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
//    @GetMapping("/changeOscilloscopeFrequency")
//    @ResponseBody
//    List<String> ChangeOscilloscopeFrequency() {
//        List<String> answear = new ArrayList<>();
//        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(506L,6L);
//        for(OrdersDB order : ordersDBS){
//            try{
//            if(order.getFrequency() != null){
//                if(order.getFrequency() <= 0.5 ){
//                    tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,9L,order.getComment(),order.getFrequency());
//                }
//            }
//
//
//                } catch (Exception e) {
//                    answear.add(order.getTender().toString());
//                }
//
//
//        }
//        return answear;
//    }
//
//    @ApiOperation(value = "Заменяет генераторы на генераторы целевого")
//    @GetMapping("/changeGeneratorFrequency")
//    @ResponseBody
//    List<String> ChangeGeneratorFrequency() {
//        List<String> answear = new ArrayList<>();
//        List<OrdersDB> ordersDBS = tableMapper.findAllOrdersbyProduct(269L,2L);
//        for(OrdersDB order : ordersDBS){
//            try{
//                if(order.getFrequency() != null){
//                    if(order.getFrequency() <= 1 ){
//                        tableMapper.ChangeProductAndCommentAndFrequency(order.getId(),1L,10L,order.getComment(),order.getFrequency());
//                    }
//                }
//
//
//            } catch (Exception e) {
//                answear.add(order.getTender().toString());
//            }
//
//
//        }
//        return answear;
//    }

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
                    if(tableMapper.findIdProduct("Select id from product where vendor_code = '"+product.getVendor_code()+"' and  vendor = "+product.getVendor()) == null){
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
                " left join product as pr on pr.id = orders.product" +
                " left join product_category on pr.product_category = product_category.id" +
                " left join subcategory on pr.subcategory = subcategory.id"+
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
                    " left join product as pr on pr.id = orders.product" +
                    " left join tender on orders.tender = tender.id" +
                    " left join product_category on pr.product_category = product_category.id" +
                    " left join subcategory on pr.subcategory = subcategory.id"+
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
                " left join product as pr on pr.id = orders.product" +
                " left join tender on orders.tender = tender.id" +
                " left join product_category on pr.product_category = product_category.id" +
                " left join subcategory on pr.subcategory = subcategory.id"+
                " left join vendor on pr.vendor = vendor.id" +
                    " left join customer c on c.id = tender.customer" +
                    " left join typetender t on t.id = tender.typetender" +
                    " left join winner w on w.id = tender.winner" +
                    " left join country on c.country = country.id" +
                " where " +
                (!tender.equals("") ?"("+tender+")":"") +
                (!product.equals("")?" and ("+ product + ")":"") +" group by " +groupByForTable + " order by " + groupByForTable ;
        List<Map<String,Object>> tenderTable = tableMapper.Report(selectTenderForTable);
        for(Map<String,Object> a : tenderTable){
            try{
                BigDecimal b = BigDecimal.valueOf(Double.parseDouble(a.get("Сумма").toString()) / kurs.get(a.get(nameYear))).setScale(2, BigDecimal.ROUND_CEILING) ;
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
                    Arrays.toString(searchParameters.getInnCustomer()),
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
                    Arrays.toString(searchParameters.getInnCustomer()),
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


    @ApiOperation(value ="Новая функция для вывода отчетов")
    @PostMapping("/EmailReport")
    @ResponseBody
    List<EmailReport> EmailReport(@RequestBody CriteriaEmailReport criteriaEmailReport){
        List<EmailReport> emailReports = new ArrayList<>();
        String select;
        switch (criteriaEmailReport.getId_step()){
            case 0:
                long countDate = criteriaEmailReport.getDate_start().until(criteriaEmailReport.getDate_finish(), ChronoUnit.DAYS)+1;
                for(int i= 0;i<=5;i++){
                    select = "Select COUNT(tender.id) as number, COUNT(distinct if(price>1000,tender.id,null)) as numberWithPrice," +
                            "SUM(sum) as price  from tender where date_start >= '"+criteriaEmailReport.getDate_start().minusDays(countDate*i).format(format_Dublicate)+"' and date_start<='"+criteriaEmailReport.getDate_finish().minusDays(countDate*i).format(format_Dublicate)+
                            "' and dublicate = false";
                    EmailReport emailReport = tableMapper.emailreport(select);
                    if(emailReport != null){
                        emailReport.setDate_start(criteriaEmailReport.getDate_start().minusDays(countDate*i));
                        emailReport.setDate_finish(criteriaEmailReport.getDate_finish().minusDays(countDate*i));
                        emailReports.add(emailReport);
                    }
                }
                break;
            case 1:
                select = "Select COUNT(tender.id) as number, typetender.type as type_tender, customer.name as customer, customer.id as customer_id, Sum(full_sum) as full_sum, currency as currency," +
                        "SUM(sum) as price from tender left join typetender on tender.typetender = typetender.id left join customer on tender.customer = customer.id" +
                        " where date_start >= '"+criteriaEmailReport.getDate_start().format(format_Dublicate)+"' and date_start<='"+criteriaEmailReport.getDate_finish().format(format_Dublicate)+
                        "' and price >=1000 and dublicate = false group by customer_id order by price desc";
                emailReports = tableMapper.listEmailreport(select);
                for (EmailReport emailReport : emailReports){
                    emailReport.setTenderIn(tableMapper.EmailTender(criteriaEmailReport.getDate_start(),criteriaEmailReport.getDate_finish(), emailReport.getCustomer_id()));
                }
                break;
            case 2:
                select = "Select COUNT(tender.id) as number, typetender.type as type_tender, customer.name as customer, customer.id as customer_id, Sum(full_sum) as full_sum, currency as currency," +
                        "SUM(sum) as price from tender left join typetender on tender.typetender = typetender.id left join customer on tender.customer = customer.id" +
                        " where date_start >= '"+criteriaEmailReport.getDate_start().format(format_Dublicate)+"' and date_start<='"+criteriaEmailReport.getDate_finish().format(format_Dublicate)+
                        "' and price <1000 and dublicate = false group by customer_id order by price desc";
                emailReports = tableMapper.listEmailreport(select);
                for (EmailReport emailReport : emailReports){
                    emailReport.setTenderIn(tableMapper.EmailTender(criteriaEmailReport.getDate_start(),criteriaEmailReport.getDate_finish(), emailReport.getCustomer_id()));
                }
                break;
            case 3:
                select = "Select COUNT(tender.id) as number, typetender.type as type_tender, customer.name as customer, customer.id as customer_id, Sum(full_sum) as full_sum, currency as currency," +
                        "SUM(sum) as price from adjacent_tender as tender left join typetender on tender.typetender = typetender.id left join customer on tender.customer = customer.id" +
                        " where date_start >= '"+criteriaEmailReport.getDate_start().format(format_Dublicate)+"' and date_start<='"+criteriaEmailReport.getDate_finish().format(format_Dublicate)+
                        "' and (name_tender like '%поверк%' or name_tender like '%калибровк%' or name_tender like '%ремонт%' or (name_tender like 'услуги' and name_tender like 'метролог')) group by customer_id order by price desc";
                emailReports = tableMapper.listEmailreport(select);
                for (EmailReport emailReport : emailReports){
                    emailReport.setTenderIn(tableMapper.EmailAdjacentTender(criteriaEmailReport.getDate_start(),criteriaEmailReport.getDate_finish(), emailReport.getCustomer_id()));
                }

                break;
            case 4:
                select = "Select COUNT(tender.id) as number, typetender.type as type_tender, customer.name as customer, customer.id as customer_id, Sum(full_sum) as full_sum, currency as currency," +
                        "SUM(sum) as price from adjacent_tender as tender left join typetender on tender.typetender = typetender.id left join customer on tender.customer = customer.id" +
                        " where date_start >= '"+criteriaEmailReport.getDate_start().format(format_Dublicate)+"' and date_start<='"+criteriaEmailReport.getDate_finish().format(format_Dublicate)+
                        "' and (name_tender not like '%поверк%' and name_tender not like '%калибровк%' and name_tender not like '%ремонт%' and (name_tender not like 'услуги' and name_tender not like 'метролог')) group by customer_id order by price desc";
                emailReports = tableMapper.listEmailreport(select);
                for (EmailReport emailReport : emailReports){
                    emailReport.setTenderIn(tableMapper.EmailAdjacentTender(criteriaEmailReport.getDate_start(),criteriaEmailReport.getDate_finish(), emailReport.getCustomer_id()));
                }
                break;
            case 5:
                select = "Select COUNT(tender.id) as number, SUM(tender.sum) as price," +
                        "SUM(price) as price  from plan_schedule_tender as tender where year(date_start) = '"+criteriaEmailReport.getDate_start().getYear()+
                        "'";
                emailReports.add( tableMapper.emailreport(select));
                break;
        }
        return emailReports;
    }

    @ApiOperation(value = "обновляем таблицу ORders")
    @GetMapping(path = "/ChangeOrders")
    @ResponseBody
    String ChangeOrders() throws JSONException {
        String answear="";
        List<OrdersDB> ordersDBS = tableMapper.findAllOrders();
        for(OrdersDB ordersDB : ordersDBS){
            try{
                if(ordersDB.getProduct() == null){
                    Long product = tableMapper.CheckProductLong(ordersDB.getProduct_category(),ordersDB.getId_product());
                    if(product != null){
                        tableMapper.ChangeProductFormat(ordersDB.getId(),product);
                    }
                    else {
                        answear = answear + ordersDB.getId()+", ";
                    }
                }

            }
            catch (Exception e){
                answear = answear + ordersDB.getId()+", ";
            }
        }
        return answear;
    }

    @ApiOperation(value = "Верхняя диаграмма для главной странице")
    @PostMapping(path = "/getTopDiagrammHome")
    @ResponseBody
    Object getTopDiagrammHome(@RequestBody String period){
        List<NameValue> rezult = tableMapper.getTopDiagrammHome(searchAtribut.startDateByPeriod(period));
        if(rezult.size() != 0){
            Long maxValue = rezult.get(0).getValue();
            List<NameValue> ret = rezult.stream().filter((en) ->
                    en.getValue()>=(maxValue*0.1)).collect(Collectors.toList());
            Long another = rezult.stream().map(NameValue::getValue).filter(value ->
                    value <=(maxValue*0.1)).reduce(0L,Long::sum);
            ret.add(new NameValue("Меньше 10%",another));
            return ret.stream().map(entity ->{
                List<Object> list = new ArrayList<>();
                list.add(entity.getName());
                list.add(entity.getValue());
                return list;
            }).collect(Collectors.toList());
        }
        else {
            return rezult;
        }
    }

    @ApiOperation(value = "Нижняя диаграмма для главной странице")
    @PostMapping(path = "/getBottomDiagrammHome")
    @ResponseBody
    Object getBottomDiagrammHome(@RequestBody String period){
        List<NameValue> rezult = tableMapper.getBottomDiagrammHome(searchAtribut.startDateByPeriod(period));
        if(rezult.size() != 0){
            Long maxValue = rezult.get(0).getValue();
            List<NameValue> ret = rezult.stream().filter((en) ->
                    en.getValue()>=(maxValue*0.1)).collect(Collectors.toList());
            Long another = rezult.stream().map(NameValue::getValue).filter(value ->
                    value <=(maxValue*0.1)).reduce(0L,Long::sum);
            ret.add(new NameValue("Меньше 10%",another));
            return ret.stream().map(entity ->{
                List<Object> list = new ArrayList<>();
                list.add(entity.getName());
                list.add(entity.getValue());
                return list;
            }).collect(Collectors.toList());
        }
        else {
            return rezult;
        }

    }
}

