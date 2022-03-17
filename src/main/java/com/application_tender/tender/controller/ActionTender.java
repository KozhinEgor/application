package com.application_tender.tender.controller;

import com.application_tender.tender.config.ErrorBicotender;
import com.application_tender.tender.mapper.TableMapper;
import com.application_tender.tender.models.Company;
import com.application_tender.tender.models.Orders;
import com.application_tender.tender.models.Tender;
import com.application_tender.tender.service.Bicotender;
import com.application_tender.tender.service.FileService;
import com.application_tender.tender.subsidiaryModels.SearchParameters;
import com.application_tender.tender.subsidiaryModels.SearchTender;
import com.application_tender.tender.subsidiaryModels.Tenders;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/demo")
public class ActionTender {
    @Autowired
    private SearchAtribut searchAtribut;
    @Autowired
    private GetCurrency getCurrency;
    @Autowired
    private Bicotender bicotender;
    @Value("${file.pathname}")
    private String pathname;
    private final FileService fileService;
    private final TableMapper tableMapper;

    private final DateTimeFormatter format_date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");
    private final DateTimeFormatter format_dateFile = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter format_API_Bico = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private final DateTimeFormatter format_Dublicate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter formatCurrency = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    ActionTender(TableMapper tableMapper,FileService fileService){
        this.tableMapper =tableMapper;
        this.fileService = fileService;
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

    @ApiOperation(value = "Возвращает Информацию об основных тендерах по заданным условиям", notes = "Вначале закпускается функция формирующая условия поиска тендеров, после чего сформированная строка используется в запросе к БД")
    @PostMapping("/Tenders")
    @ResponseBody
    Tenders Tenders(@RequestBody SearchTender json) {
        String where = searchAtribut.findTenderByTerms(json.getSearchParametrs());
        json.getSearchParametrs().setMinSum(new BigDecimal(1));
        String whereWithPrice = searchAtribut.findTenderByTerms(json.getSearchParametrs());
        if(json.getSearchParametrs().isAdjacent_tender()){

            return new Tenders(
                    tableMapper.findAdjacentTenderTerms(where,searchAtribut.orderTender(json.getPage(),json.getSortName(),json.getSortDirection(),json.getPageSize())),
                    tableMapper.findCountAdjacentTender(where),
                    tableMapper.findCountAdjacentTender(whereWithPrice),
                    tableMapper.findSumAdjacentTender(whereWithPrice),
                    null,
                    null);
        }
        else if(json.getSearchParametrs().isPlan_schedule()){
            return new Tenders(
                    tableMapper.findPlanTenderTerms(where,searchAtribut.orderTender(json.getPage(),json.getSortName(),json.getSortDirection(),json.getPageSize())),
                    tableMapper.findCountPlanTender(where),
                    tableMapper.findCountPlanTender(whereWithPrice),
                    tableMapper.findSumPlanTender(whereWithPrice),
                    null,
                    null
            );
        }
        else{
            json.getSearchParametrs().setMinSum(new BigDecimal(1));
            String where_win = where.equals("") ? "where win_sum > 1" : where + " and win_sum > 1";
            return new Tenders(
                    tableMapper.findTenderTerms(where,searchAtribut.orderTender(json.getPage(),json.getSortName(),json.getSortDirection(),json.getPageSize())),
                    tableMapper.findCountTender(where),
                    tableMapper.findCountTender(whereWithPrice),
                    tableMapper.findSumTender(whereWithPrice),
                    tableMapper.findCountTender(where_win),
                    tableMapper.findWinSumTender(where_win)
            );
        }

    }

    @ApiOperation(value = "Добавление основных тендеров через excel файл", notes = "Получает на вход файл в определенном формате, после чего проходится по всем строкам добавляя, новые тендеры в систему")
    @RequestMapping(value = "/addTender", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    @ResponseBody
    List<Tender> addTender(MultipartFile excel) throws IOException, InvalidFormatException {

        LinkedList<Tender> tenders = new LinkedList<>();
        File temp = new File(pathname);

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
                        BigDecimal.valueOf(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING),
                        new BigDecimal(0),
                        row.getCell(5).getStringCellValue(),
                        BigDecimal.valueOf(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING),
                        rate,
                        BigDecimal.valueOf(row.getCell(4).getNumericCellValue()).setScale(2, BigDecimal.ROUND_CEILING).multiply(new BigDecimal(rate)),
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

            if (tableMapper.findTenderByNumber_tender(num.toString()) != null) {

                id = tableMapper.findTenderByNumber_tender(num.toString());

            }
            else {
                JSONObject tender = bicotender.loadTender(num);
                if(tender == null){
                    throw new ErrorBicotender("Ошибка Ответа от Бикотендера при загрузке тендера "+num);
                }
                if(tender.get("company").toString().equals("null") ){
                    id = null;
                }
                else {
                    ZonedDateTime dateStart = ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico).plusDays(1);
                    Map<String, Double> currency = new HashMap<>();
                    currency = getCurrency.currency(dateStart.format(formatCurrency));
                    double rate = tender.get("valuta").toString().equals("RUB") || tender.get("valuta").toString().equals("null") ? 1 : currency.get(tender.get("valuta").toString());
                    JSONObject company = new JSONObject(tender.get("company").toString());
                    String cost = tender.get("cost").toString().equals("null") ? "0" : tender.get("cost").toString();
                    tableMapper.insertTender(tender.get("name").toString(),
                            "https://www.bicotender.ru/tc/tender/show/tender_id/" + tender.get("tender_id"),
                            tender.get("sourceUrl").toString(),
                            ZonedDateTime.parse(tender.get("loadTime").toString() + " Z", format_API_Bico),
                            tender.get("finishDate").toString().equals("null") ? null : ZonedDateTime.parse(tender.get("finishDate").toString() + " Z", format_API_Bico),
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

            }
            List<Tender> tenderList = new ArrayList<>();
            if(id == null){
                tenderList.add( new Tender("Тендер с номером "+num+" отсутствует в Бикотендере"));
            }
            else{
                tenderList.add(tableMapper.findTenderbyId(id));
                if(tableMapper.SelectNameDublicate(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate), tenderList.get(0).getId()).size() != 0){
                    tenderList.addAll(tableMapper.SelectNameDublicate(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate), tenderList.get(0).getId()));
                }
                if(tableMapper.SelectNameDublicatePlan(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate)).size() != 0){
                    tenderList.addAll(tableMapper.SelectNameDublicatePlan(tenderList.get(0).getFull_sum(),tenderList.get(0).getName_tender(),tenderList.get(0).getInn(), tenderList.get(0).getDate_start().format(format_Dublicate)));
                }

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

            if (tableMapper.findAdjacentTenderByNumber_tender(num.toString()) != null) {
                id = tableMapper.findAdjacentTenderByNumber_tender(num.toString());

            } else {
                JSONObject tender = bicotender.loadTender(num);
                if(tender == null){
                    throw new ErrorBicotender("Ошибка Ответа от Бикотендера при загрузке тендера "+num);
                }
                if(tender.get("company").toString().equals("null") ){
                    id = null;
                }
                else {
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
                            tender.get("finishDate").toString().equals("null") ? null : ZonedDateTime.parse(tender.get("finishDate").toString() + " Z", format_API_Bico),
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
            }
            if(id == null){
                tenders.add( new Tender("Тендер с номером "+num+" отсутствует в Бикотендере"));
            }
            else {
                tenders.add(tableMapper.findAdjacentTenderbyId(id));
            }
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
            if (tableMapper.findPlanTenderByNumber_tender(num.toString()) != null) {
                id = tableMapper.findPlanTenderByNumber_tender(num.toString());

            } else {
                JSONObject tender = bicotender.loadTender(num);
                if(tender == null){
                    throw new ErrorBicotender("Ошибка Ответа от Бикотендера при загрузке тендера "+num);
                }
                if(tender.get("company").toString().equals("null") ){
                    id = null;
                }
                else {
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
                            tender.get("finishDate").toString().equals("null") ? null : ZonedDateTime.parse(tender.get("finishDate").toString() + " Z", format_API_Bico),
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
            }
            if(id == null){
                tenders.add( new Tender("Тендер с номером "+num+" отсутствует в Бикотендере"));
            }
            else {
                tenders.add(tableMapper.findPlanTenderbyId(id));
            }
        }
        tableMapper.upadateBuffer(null,3L);
        return tenders;
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

    @ApiOperation(value = "Возвращает тендеры без продуктов", notes = "Возвращает тендеры, которые не упоминаются в таблице orders")
    @GetMapping("/TenderWithoutOrdersForHome")
    @ResponseBody
    List<Tender> findTenderWithoutOrdersForHome() {
        return tableMapper.findTenderWithoutOrdersForHome();
    }

    @ApiOperation(value = "Возвращает тендеры в которых есть продукт, нет документации", notes = "Возвращает тендеры в которых есть продукт, нет документации")
    @GetMapping("/TendernoDocumentation")
    @ResponseBody
    List<Tender> findTendernoDocumentation() {
        return tableMapper.findTendernoDocumentation();
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
        searchAtribut.UpdateProductTender(tender.getId());
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

    @ApiOperation(value = "удаляет основной тендер по Id")
    @GetMapping("/DeleteTender/{tender}")
    @ResponseBody
    Map<String, String> DeleteTender(@PathVariable Long tender) {
        Long[] dublicate = tableMapper.tender_dublicate(tender);
        for(Long d: dublicate){
            tableMapper.changeNoDublicate(d);
        }
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
            row.createCell(17).setCellValue(tender.getWin_sum().doubleValue()>tender.getPrice().doubleValue()?tender.getWin_sum().doubleValue()*tender.getRate()*(tender.getPrice().doubleValue()/tender.getFull_sum().doubleValue()):tender.getWin_sum().doubleValue()*tender.getRate());
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

    @ApiOperation(value = "Устанавливает связь тендера с дубликатом")
    @GetMapping("/setDublicate/{id}/{id_d}")
    @ResponseBody
    String setDublicate(@PathVariable Long id,@PathVariable Long id_d) {

        tableMapper.changeDublicate(id_d);
        if(tableMapper.CheckDublicate(id,id_d) == null){
            tableMapper.insertDublicate(id,id_d);
        }
        for(Orders o : tableMapper.findAllOrdersbyTender(id)){
            tableMapper.deleteOrder(o.getId());
        }
        for(Orders o : tableMapper.findAllOrdersbyTender(id_d)){
            o.setTender(id);
            System.out.println(o.toString());
            tableMapper.insertOrder(o.OrdersToOrdersDB());
        }
        searchAtribut.UpdateProductTender(id);
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

    @ApiOperation(value = "Возвращает возможные дубликаты")
    @GetMapping("/getDublicate/{id}")
    @ResponseBody
    List<Tender> getDublicate(@PathVariable Long id) {
        Tender tender = tableMapper.findTenderbyId(id);
        return tableMapper.SelectDublicate(tender.getFull_sum(),tender.getInn(),tender.getDate_start().format(format_Dublicate));
    }
}
