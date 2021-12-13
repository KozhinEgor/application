package com.application_tender.tender.mapper;

import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.*;
import org.apache.ibatis.annotations.*;
import org.json.JSONObject;


import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TableMapper {
    final String atributTender = "distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as winner, product, dublicate, country.name as country";
    final String atributAdjacentTender = "distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, dublicate, country.name as country";
///////////////////////////////////////////////////////////
//              Tender SQL
///////////////////////////////////////////////////////////

    @Select("Select id from tender where number_tender = #{number_tender}")
    Long findTenderByNumber_tender(String number_tender);

    @Select("Select " + atributTender +", w.inn as winner_inn, winner_country.name as winner_country"+
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id left join country as winner_country on w.country = winner_country.id where tender.id = #{id}")
    Tender findTenderbyId(Long id);

    ////
    @Select("Select distinct tender.id,name_tender,number_tender from tender where date_start>'2021-04-01' and date_start<'2021-08-01'")
    List<Tender> findNameTenderByDate();

    @Select("Select id from tender where winner = #{winner}")
    List<Long> findTenderByWinner(Long winner);

    @Select("Select id from tender where customer = #{customer}")
    List<Long> findTenderByCustomer(Long customer);

    @Select("Select number_tender from tender where customer = #{customer} limit 1")
    String BicoNumberbyCustomer(Long customer);

    ///
    @Select("Select " + atributTender +
            " from tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join winner w on w.id = tender.winner" +
            " left join country on c.country = country.id" +
            " left join orders on orders.tender = tender.id" +
            " left join product as pr on pr.id_product = orders.id_product and pr.product_category = orders.product_category" +
            " left join product_category on orders.product_category = product_category.id" +
            " left join subcategory on subcategory = subcategory.id"+
            " left join vendor on pr.vendor = vendor.id" +
            " ${where} order by date_start")
    List<Tender> findAllTenderTerms(String where);

    @Insert("insert into tender (name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding,number_tender,  full_sum, win_sum, currency, price, rate, sum, customer, typetender, winner) " +
            "values (#{name_tender}, #{bico_tender},#{gos_zakupki},#{date_start}, #{date_finish},#{date_tranding},#{number_tender},  #{full_sum}, #{win_sum}, #{currency}, #{price}, #{rate}, #{sum}, #{customer}, #{typetender}, #{winner}) ")
    void insertTender(String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start, ZonedDateTime date_finish, ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, BigDecimal win_sum, String currency, BigDecimal price, Double rate, BigDecimal sum, Long customer, Long typetender, Long winner);

    @Update("Update tender set product = #{product} where id = #{id}")
    Long UpdateProductTender(String product, Long id);

    @Update("Update tender set  name_tender =#{name_tender} , bico_tender=#{bico_tender},gos_zakupki=#{gos_zakupki},date_start=#{date_start}, date_finish=#{date_finish},date_tranding=#{date_tranding} ,number_tender=#{number_tender},  full_sum=#{full_sum}, win_sum=#{win_sum}, currency=#{currency}, price=#{price}, rate=#{rate}, sum=#{sum}, customer=#{customer}, typetender=#{typetender}, winner=#{winner}, dublicate=#{dublicate} where id = #{id}")
    void UpdateTender(Long id, String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start, ZonedDateTime date_finish, ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, BigDecimal win_sum, String currency, BigDecimal price, double rate, BigDecimal sum, Long customer, Long typetender, Long winner, Boolean dublicate);

    @Update("Update tender set customer = #{customer} where tender.id = #{tender}")
    void changeCustomerTender(Long tender, Long customer);

    @Update("Update tender set name_tender =#{name_tender} where tender.id = #{tender}")
    void changeNameTender(Long tender, String name_tender);

    @Delete("DELETE FROM tender where id = #{id} limit 1")
    void DeleteTender(Long id);

    ///////////////////////////////////////////////////////////
//              AdjacentTender SQL adjacent_tender
///////////////////////////////////////////////////////////
    @Select("Select id from adjacent_tender where number_tender = #{number_tender}")
    Long findAdjacentTenderByNumber_tender(String number_tender);

    @Select("Select " + atributAdjacentTender +
            " from adjacent_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id where tender.id = #{id}")
    Tender findAdjacentTenderbyId(Long id);

    @Select("Select id from adjacent_tender where customer = #{customer}")
    List<Long> findAdjacentTenderByCustomer(Long customer);

    @Select("Select " + atributAdjacentTender +
            " from adjacent_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id" +
            " ${where} order by date_start")
    List<Tender> findAllAdjacentTenderTerms(String where);

    @Insert("insert into adjacent_tender (name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding,number_tender,  full_sum, currency, price, rate, sum, customer, typetender) " +
            "values (#{name_tender}, #{bico_tender},#{gos_zakupki},#{date_start}, #{date_finish},#{date_tranding},#{number_tender},  #{full_sum}, #{currency}, #{price}, #{rate}, #{sum}, #{customer}, #{typetender}) ")
    void insertAdjacentTender(String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start, ZonedDateTime date_finish, ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, String currency, BigDecimal price, Double rate, BigDecimal sum, Long customer, Long typetender);

    @Update("Update adjacent_tender set  name_tender =#{name_tender} , bico_tender=#{bico_tender},gos_zakupki=#{gos_zakupki},date_start=#{date_start}, date_finish=#{date_finish},date_tranding=#{date_tranding} ,number_tender=#{number_tender},  full_sum=#{full_sum}, currency=#{currency}, price=#{price}, rate=#{rate}, sum=#{sum}, customer=#{customer}, typetender=#{typetender}, dublicate=#{dublicate} where id = #{id}")
    void UpdateAdjacentTender(Long id, String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start, ZonedDateTime date_finish, ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, String currency, BigDecimal price, double rate, BigDecimal sum, Long customer, Long typetender, Boolean dublicate);

    @Update("Update adjacent_tender set customer = #{customer} where adjacent_tender.id = #{tender}")
    void changeCustomerAdjacentTender(Long tender, Long customer);

    @Delete("DELETE FROM adjacent_tender where id = #{id} limit 1")
    void DeleteAdjacentTender(Long id);

    ///////////////////////////////////////////////////////////
//              ProductCategory SQL
///////////////////////////////////////////////////////////
    @Select("Select id,category,category_en,category_product from product_category order by category")
    List<ProductCategory> findAllProductCategory();

    @Select("Select category from product_category where id = #{id} limit 1")
    String findOneCategoryById(Long id);

    @Select("Select id,category,category_en,category_product from product_category where id = #{id} limit 1")
    ProductCategory findCategoryById(Long id);

    @Select("Select category_en from product_category where id = #{id} limit 1")
    String findOneCategoryENById(Long id);

    @Insert("Insert into product_category (category,category_en, category_product) values (#{category},#{category_en}, #{cat})")
    void InsertCategory(String category, String category_en, String cat);

    ///////////////////////////////////////////////////////////
//              SynonymsProduct SQL synonyms_product
///////////////////////////////////////////////////////////
    @Select("Select synonyms_product.id,product_category.category as product_category,synonyms, product_category.id as id_category from synonyms_product left join product_category on product_category.id=synonyms_product.product_category")
    List<SynonymsProduct> findAllSynonymsProduct();

    @Insert("Insert into synonyms_product (product_category,synonyms) values (#{product_category},#{synonyms})")
    void InsertSynonymsProduct(Long product_category, String synonyms);

    @Update("Update synonyms_product set product_category = #{product_category},synonyms = #{synonyms} where id = #{id}")
    void UpdateSynonymsProduct(Long id, Long product_category, String synonyms);

    ///////////////////////////////////////////////////////////
//              BigCategory SQL big_category
//              SQL big_category_dependencies
///////////////////////////////////////////////////////////
    @Select("Select id from big_category")
    List<Long> findAllBigCategory();

    @Select("Select name from big_category where id =#{id}")
    String findBigCategorybyId(Long id);

    @Select("Select id from big_category where name =#{name}")
    Long findBigCategorybyName(String name);

    @Insert("Insert into big_category (name) values (#{name})")
    void InsertBigCategory(String name);

    @Select("Select category from big_category_dependencies where big_category = #{big_category}")
    List<Long> findCategorybyBigCategory(Long big_category);

    @Select("Insert into big_category_dependencies (big_category,category) values (#{big_category},#{category})")
    void InsertBig_category_dependencies(Long big_category, Long category);

    @Delete("DELETE FROM big_category_dependencies where big_category = #{big_category} and category = #{category}")
    void DeleteBig_category_dependencies(Long big_category, Long category);

    ///////////////////////////////////////////////////////////
//              Spectrum_analyser SQL   spectrum_analyser
///////////////////////////////////////////////////////////
    @Select("Select * from spectrum_analyser")
    List<Spectrum_analyzers> findAllSpectrum_analysers();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, portable, usb,  name as vendor from spectrum_analyser as pr left join vendor v on pr.vendor = v.id")
    List<Product> findAllSpectrum_analyserToProduct();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, portable, usb,  name as vendor from spectrum_analyser as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
    Product findAllSpectrum_analyserToProductById(Long id_product);

    @Select("Select id,vendor_code, vendor as vendor_id, portable, usb from spectrum_analyser where id = #{id} limit 1")
    Product findOneSpectrum_analyserById(Long id);

    @Update("Update spectrum_analyser set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor}, portable = #{portable}, usb= #{usb} where id = #{id}")
    Long UpdateSpectrum_analyser(String vendor_code, double frequency, Long vendor, Long id, boolean portable, boolean usb);

    @Insert("Insert into spectrum_analyser (vendor_code, frequency ,vendor, portable, usb) values(#{vendor_code}, #{frequency},#{vendor}, #{portable},#{usb})")
    Long InsertSpectrum_analyser(String vendor_code, double frequency, Long vendor, boolean portable, boolean usb);

    ///////////////////////////////////////////////////////////
//              SignalGenerator SQL     signal_generator
///////////////////////////////////////////////////////////
    @Select("Select * from signal_generator")
    List<SignalGenerator> findAllSignalGenerator();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from signal_generator as pr left join vendor v on pr.vendor = v.id")
    List<Product> findAllSignalGeneratorToProduct();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from signal_generator as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
    Product findAllSignalGeneratorToProductById(Long id_product);

    @Select("Select id,vendor_code, vendor as vendor_id from signal_generator where id = #{id} limit 1")
    Product findOneSignalGeneratorById(Long id);

    @Update("Update signal_generator set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor} where id = #{id}")
    Long UpdateSignalGenerator(String vendor_code, double frequency, Long vendor, Long id);

    @Insert("Insert into signal_generator (vendor_code, frequency ,vendor) values(#{vendor_code}, #{frequency},#{vendor})")
    Long InsertSignalGenerator(String vendor_code, double frequency, Long vendor);

    ///////////////////////////////////////////////////////////
//              PulseGenerator SQL      pulse_generator
///////////////////////////////////////////////////////////
    @Select("Select * from pulse_generator")
    List<PulseGenerator> findAllPulseGenerator();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from pulse_generator as pr left join vendor v on pr.vendor = v.id")
    List<Product> findAllPulseGeneratorToProduct();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from pulse_generator as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
    Product findAllPulseGeneratorToProductById(Long id_product);

    @Select("Select id,vendor_code, vendor as vendor_id from pulse_generator where id = #{id} limit 1")
    Product findOnePulseGeneratorById(Long id);

    @Update("Update pulse_generator set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor} where id = #{id}")
    Long UpdatePulseGenerator(String vendor_code, double frequency, Long vendor, Long id);

    @Insert("Insert into pulse_generator (vendor_code, frequency ,vendor) values(#{vendor_code}, #{frequency},#{vendor})")
    Long InsertPulseGenerator(String vendor_code, double frequency, Long vendor);

    ///////////////////////////////////////////////////////////
//              SignalAnalyzer SQL      signal_analyzer
///////////////////////////////////////////////////////////
    @Select("Select * from signal_analyzer")
    List<SignalAnalyzer> findAllSignalAnalyzer();

    @Select("Select pr.id, vendor_code, frequency, vendor as vendor_id, name as vendor from signal_analyzer as pr left join vendor v on pr.vendor = v.id")
    List<Product> findAllSignalAnalyzerToProduct();

    @Select("Select pr.id, vendor_code, frequency, vendor as vendor_id, name as vendor from signal_analyzer as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
    Product findAllSignalAnalyzerToProductById(Long id_product);

    @Select("Select id,vendor_code, vendor as vendor_id from signal_analyzer where id = #{id} limit 1")
    Product findOneSignalAnalyzerById(Long id);

    @Update("Update signal_analyzer set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor} where id = #{id}")
    Long UpdateSignalAnalyzer(String vendor_code, double frequency, Long vendor, Long id);

    @Insert("Insert into signal_analyzer (vendor_code, frequency ,vendor) values(#{vendor_code}, #{frequency},#{vendor})")
    Long InsertSignalAnalyzer(String vendor_code, double frequency, Long vendor);

    ///////////////////////////////////////////////////////////
//              NetworkAnalyzers SQL        network_analyzers
///////////////////////////////////////////////////////////
    @Select("Select  pr.id, vendor_code, frequency,vendor as vendor_id, usb,  name as vendor from network_analyzers as pr left join vendor v on pr.vendor = v.id")
    List<Product> findAllNetworkAnalyzersToProduct();

    @Select("Select * from network_analyzers")
    List<Spectrum_analyzers> findAllNetworkAnalyzers();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb,  name as vendor from network_analyzers as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
    Product findAllNetworkAnalyzersToProductById(Long id_product);

    @Select("Select id,vendor_code, vendor as vendor_id from network_analyzers where id = #{id} limit 1")
    Product findOneNetworkAnalyzersById(Long id);

    @Update("Update  network_analyzers set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor}, usb= #{usb} where id = #{id}")
    Long UpdateNetworkAnalyzers(String vendor_code, double frequency, Long vendor, Long id, boolean usb);

    @Insert("Insert into  network_analyzers (vendor_code, frequency ,vendor, usb) values(#{vendor_code}, #{frequency},#{vendor},#{usb})")
    Long InsertNetworkAnalyzers(String vendor_code, double frequency, Long vendor, boolean usb);

    ///////////////////////////////////////////////////////////
//              Oscilloscope SQL        oscilloscope
///////////////////////////////////////////////////////////
    @Select("Select * from oscilloscope")
    List<Oscilloscope> findAllOscilloscope();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, vxi, channel, name as vendor from oscilloscope as pr left join vendor v on pr.vendor = v.id")
    List<Product> findAllOscilloscopeToProduct();

    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, vxi, channel, name as vendor from oscilloscope as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
    Product findAllOscilloscopeToProductById(Long id_product);

    @Select("Select id,vendor_code, vendor as vendor_id from oscilloscope where id = #{id} limit 1")
    Product findOneOscilloscopeById(Long id);

    @Update("Update oscilloscope set vendor_code = #{vendor_code}, frequency = #{frequency},vendor = #{vendor}, vxi = #{portable}, usb= #{usb}, channel =#{channel} where id = #{id}")
    Long UpdateOscilloscope(String vendor_code, double frequency, Long vendor, Long id, boolean vxi, boolean usb, double channel);

    @Insert("Insert into oscilloscope (vendor_code, frequency ,vendor, vxi, usb, channel) values(#{vendor_code}, #{frequency},#{vendor}, #{vxi},#{usb}, #{channel})")
    Long InsertOscilloscope(String vendor_code, double frequency, Long vendor, boolean vxi, boolean usb, double channel);

    ///////////////////////////////////////////////////////////
//              AnotherProduct SQL      another_product
///////////////////////////////////////////////////////////
    @Select("Select * from another_product")
    List<AnotherProduct> findAllAnotherProduct();

    @Select("Select id,name as vendor_code from another_product")
    List<Product> findAllAnotherProductToProduct();

    @Select("Select id,name as vendor_code from another_product as pr where pr.id = #{id_product} limit 1")
    Product findAllAnotherProductToProductById(Long id_product);

    @Select("Select name as vendor_code from another_product where id = #{id} limit 1")
    Product findOneAnotherProductById(Long id);

    @Update("Update  another_product set name = #{vendor_code} where id = #{id}")
    Long UpdateAnotherProduct(String vendor_code, Long id);

    @Insert("Insert into  another_product (name) values(#{vendor_code})")
    Long InsertAnotherProduct(String vendor_code);

    ///////////////////////////////////////////////////////////
//              country SQL
///////////////////////////////////////////////////////////
    @Select("Select * from country")
    List<Country> findAllCountry();

    @Select("Select * from country where id = #{id}")
    List<Country> findAllCountryById(Long id);

    @Select("Select * from country where name = #{name}")
    List<Country> findAllCountryByName(String name);

    ///////////////////////////////////////////////////////////
//              Customer SQL
///////////////////////////////////////////////////////////
    @Select("Select customer.id,inn,customer.name as name,country.name as country  from customer left join country on customer.country = country.id")
    List<Company> findAllCustomer();

    @Select("Select customer.id,inn,customer.name as name,country.name as country  from customer left join country on customer.country = country.id where customer.id not in (Select distinct customer from tender) and customer.id not in (Select distinct customer from adjacent_tender)")
    List<Company> findAllCustomerNoUses();

    @Select("Select id from customer where inn = #{inn} limit 1")
    Long findCustomerByInn(String inn);

    @Select("Select id from customer where name = #{name} limit 1")
    Long findCustomerByName(String name);

    @Select("Select id from customer where name = #{name} and inn = #{inn} limit 1")
    Long findCustomerByNameandINN(String name, String inn);

    @Select("Select inn from customer where id = #{id} limit 1")
    String findCustomerInnById(Long id);

    @Select("Select name from customer where id = #{id} limit 1")
    String findCustomerNameById(Long id);

    @Select("Select id from customer where inn = 0 and id > 2140 limit 100")
    List<Long> CustomersZeroINN();

    @Insert("INSERT into customer (name,inn, country) values (#{name},#{inn},#{country})")
    void insertCustomer(String inn, String name, Long country);

    @Update("UPDATE customer SET inn = #{inn} WHERE id = #{id}")
    void updateCustomerInn(String inn, Long id);

    @Update("UPDATE customer SET inn = #{inn},country = 2 WHERE id = #{id}")
    void updateCustomerInnAndCountry(String inn, Long id);

    @Update("UPDATE customer SET inn = #{inn}, name = #{name}, country = #{country} WHERE id = #{id}")
    void updateCustomer(Long id, String inn, String name, Long country);

    @Delete("Delete from customer where id = #{id}")
    void deleteCustomer(Long id);

    ///////////////////////////////////////////////////////////
//              Type_tender SQL
///////////////////////////////////////////////////////////
    @Select("Select * from typetender")
    List<TypeTender> findAllType();

    @Select("Select id from typetender where type = #{type}")
    Long findTypeTenderByType(String type);

    @Insert("insert into typetender (type) values (#{type})")
    Long insertTypeTender(String type);

    ///////////////////////////////////////////////////////////
//              Winner SQL
///////////////////////////////////////////////////////////
    @Select("Select winner.id,winner.inn,winner.name as name,country.name as country from winner left join country on winner.country = country.id")
    List<Company> findAllWinner();

    @Select("Select winner.id,winner.inn,winner.name as name,country.name as country from winner left join country on winner.country = country.id where winner.id not in (Select distinct winner from tender)")
    List<Company> findAllWinnerNoUses();

    @Select("Select name from winner where id = #{id} limit 1")
    String findWinnerNameById(Long id);

    @Insert("Insert into winner (inn,name, country) values (#{inn},#{name},#{country})")
    void insertWinner(String inn, String name, Long country);

    @Update("Update winner set inn = #{inn}, name = #{name}, country = #{country} where id =#{id}")
    void updateWinner(Long id, String inn, String name, Long country);

    @Delete("Delete from winner where id = #{id}")
    void deleteWinner(Long id);

    @Update("Update tender set winner = #{winner} where tender.id = #{tender}")
    void changeWinner(Long tender, Long winner);

    ///////////////////////////////////////////////////////////
//              Vendor SQL
///////////////////////////////////////////////////////////
    @Select("Select * from vendor")
    List<Vendor> findAllVendor();

    @Select("Select name from vendor where id = #{id}")
    String findOneVendorById(Long id);

    @Select("Select id from vendor where name = #{name}")
    Long findOneVendorByName(String name);

    @Select("Select * from vendor where id in(Select DISTINCT vendor from ${category})")
    List<Vendor> findAllVendorByCategory(String category);

    ///////////////////////////////////////////////////////////
//              Order SQL
///////////////////////////////////////////////////////////
    String orders_columns = "id,comment,id_product,product_category,tender,number,price,win_price as winprice, frequency,usb,vxi,portable,channel,port,form_factor,purpose,voltage,current,options ";
    @Select("Select * from orders")
    List<OrdersDB> findAllOrders();

    @Select("Select " + orders_columns + " from orders where tender = #{tender}")
    List<OrdersDB> findAllOrdersBDbyTender(Long tender);

    @Select("Select id from orders where tender = #{tender}")
    List<Long> findAllOrdersIdbyTender(Long tender);

    @Select("Select tender from orders where id = #{id}")
    Long findTenderIdbyId(Long id);

    @Select("Select " + orders_columns + " from orders where tender = #{tender} and product_category = #{product_category}")
    List<OrdersDB> findAllOrdersbyTender(Long tender, Long product_category);


    @Insert("insert into orders (comment, id_product,product_category,tender,number,price,win_price, frequency,usb,vxi,portable,channel,port,form_factor,purpose,voltage,current)" +
            " values (#{comment}, #{id_product},#{product_category},#{tender},#{number},#{price},#{winprice}, #{frequency},#{usb},#{vxi},#{portable},#{channel},#{port},#{form_factor},#{purpose},#{voltage},#{current})")
    Long insertOrder(OrdersDB ordersDB);

    @Select("Select LAST_INSERT_ID()")
    Long checkId();

    @Update("update orders set comment = #{comment}, id_product = #{id_product},product_category = #{product_category},tender = #{tender},number = #{number},price = #{price},win_price = #{win_price}," +
            "frequency = #{frequency},usb = #{usb},vxi = #{vxi},portable = #{portable},channel = #{channel},port = #{port},form_factor = #{form_factor},purpose = #{purpose},voltage = #{voltage},current = #{current} where id = #{id}")
    void updateOrder(Long id, String comment, Long id_product, Long product_category, Long tender, int number, BigDecimal price, BigDecimal win_price,
                     Double frequency,Boolean usb,Boolean vxi,Boolean portable,Integer channel,Integer port,String form_factor,String purpose,Double voltage,Double current);

    @Update("update orders set id_product = #{id_product},product_category = #{product_category} where id = #{id}")
    void ChangeProduct(Long id, Long id_product, Long product_category);

    @Update("update orders set id_product = #{id_product},product_category = #{product_category}, comment = #{comment}, frequency = #{frequency} where id = #{id}")
    void ChangeProductAndCommentAndFrequency(Long id, Long id_product, Long product_category, String comment, Double frequency);

    @Update("update orders set comment = #{comment}, id_product = #{id_product},product_category = #{product_category} where id = #{id}")
    void ChangeProductFromFile(Long id, Long id_product, Long product_category, String comment);

    @Select("Select id from orders where id_product = #{id_product} and product_category = #{product_category}")
    List<Long> findAllOrdersIdbyProduct(Long id_product, Long product_category);

    @Select("Select * from orders where id_product = #{id_product} and product_category = #{product_category}")
    List<OrdersDB> findAllOrdersbyProduct(Long id_product, Long product_category);

    @Delete("Delete from orders where id = #{id}")
    void deleteOrder(Long id);

    @Update("Update orders set options = #{options} where id =#{id}")
    void updateOrdersOptions(String options,Long id);
    ///////////////////////////////////////////////////////////
//              Order-Tenders SQL
///////////////////////////////////////////////////////////
    @Select("SELECT count(*) as count FROM keysight.tender WHERE NOT EXISTS (SELECT orders.ID FROM keysight.orders WHERE tender.ID = orders.tender)")
    Long findCountTenderWithoutOrders();

    @Select("SELECT " + atributTender +
            " FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id WHERE NOT EXISTS (SELECT orders.ID FROM keysight.orders WHERE tender.ID = orders.tender)")
    List<Tender> findTenderWithoutOrders();

    @Select("SELECT " + atributTender +
            "        FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner right join orders o on tender.id = o.tender left join country on c.country = country.id where o.product_category = 7 and o.id_product = 255")
    List<Tender> findTendernoDocumentation();

    @Select("SELECT count(*) as count, sum(sum) as sum, #{year} as year, #{NumberQuarter} as quarter from (Select distinct tender.id, tender.sum as sum from keysight.tender join keysight.orders on orders.tender = tender.id where ${dateRange} and ${category}  ${tenders}) as c")
    ReportQuarter findForOrders(int year, int NumberQuarter, String dateRange, String category, String tenders);

    @Select("${select}")
    List<Map<String,String>> Report(String select);

    @Select("SELECT name from (Select distinct tender.id, vendor.name as name from keysight.tender join keysight.orders on orders.tender = tender.id left join ${category_en} as prod on prod.id = orders.id_product left join vendor on  prod.vendor = vendor.id where ${dateRange} and ${category} ${tenders}) as c ")
    List<String> findVendorForOrders(String dateRange, String category, String category_en, String tenders);

    @Select("SELECT trim(name) from (Select distinct tender.id, orders.comment as name from keysight.tender join keysight.orders on orders.tender = tender.id left join ${category_en} as prod on prod.id = orders.id_product where ${dateRange} and ${category} and prod.vendor = 1 ${tenders}) as c")
    List<String> findNoVendorForOrders(String dateRange, String category, String category_en, String tenders);

    @Select("SELECT " + atributTender +
            " FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner  left join orders o on tender.id = o.tender left join country on c.country = country.id WHERE o.id_product like #{product} and  o.product_category = #{category} and (date_start between #{dateStart} and #{dateFinish})"
    )
    List<Tender> TenderOnProduct(Long category, ZonedDateTime dateStart, ZonedDateTime dateFinish, String product);

    @Select("Select tender from orders where product_category = #{category}")
    List<Long> findTenderByCategory(Long category);

    @Select("Select tender from orders left join product on product.id_product= orders.id_product and product.product_category = orders.product_category" +
            " left join product_category on orders.product_category = product_category.id" +
            " left join subcategory on subcategory = subcategory.id  where ${where}")
    List<Long> findTenderByProduct(String where);

    @Select("SELECT customer.name as name,count(*) as count FROM tender left join customer on tender.customer=customer.id where ${dateRange} ${tenders} group by tender.customer ;")
    List<ReportCompany> CustomerForOrders(String dateRange, String tenders);

    @Select("SELECT winner.name as name,count(*) as count FROM tender left join winner on tender.winner=winner.id where ${dateRange} ${tenders} group by tender.winner ;")
    List<ReportCompany> WinnerForOrders(String dateRange, String tenders);

    ///////////////////////////////////////////////////////////
//              Universal SQL
///////////////////////////////////////////////////////////
    @Select("Select category_en from product_category where id = #{id}")
    String findNameCategoryById(Long id);

    @Select("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'keysight' AND TABLE_NAME = #{name_table} order by ORDINAL_POSITION")
    String[] findcolumnName(String name_table);

    @Select("${select}")
    List<Product> findListProduct(String select);

    @Select("${select}")
    Product findOneProduct(String select);

    @Select("Select subcategory.name from ${category} as pr left join subcategory on subcategory.id = pr.subcategory where pr.id = #{id}")
    String findSubcategoryProduct(String category,Long id);

    @Select("${select}")
    Long findIdProduct(String select);

    @Insert("${insert} ")
    void InsertProduct(String insert);

    @Update("${update} limit 1")
    void UpdateProduct(String update);

    @Update("${update}")
    void UpdateProductAll(String update);

    @Delete("Delete from ${category} where id = #{id} limit 1")
    void DeleteProduct(String category, Long id);

    @Insert("${create}")
    void CreateTable(String create);

    @Select("Select vendor from ${category} where id = #{id} limit 1")
    Long findOneUniversalById(Long id, String category);

    @Select("Select name from ${category} as pr left join vendor on pr.vendor = vendor.id where pr.id = #{id} limit 1")
    String findOneVendorByIdProduct(Long id, String category);

    @Select("Select id from ${category} where vendor = #{vendor}")
    List<Long> findProductByVendor(String category, Long vendor);

    @Select("Select id from ${category} where subcategory = #{subcategory}")
    List<Long> findProductBySubcategory(String category, Long subcategory);

    @Select("Select id from ${category} where subcategory = #{subcategory} and vendor = #{vendor}")
    List<Long> findProductBySubcategoryAndVendor(String category, Long subcategory, Long vendor);

    @Select("${query}")
    List<Map<Object, Object>> selectQuery(String query);

    @Select("SELECT distinct subcategory.name FROM ${category} as pr left join subcategory on pr.subcategory = subcategory.id")
    String[] subcategoryInCategory(String category);

    @Select("Select id from subcategory where name = #{name}")
    Long findIdSubcategory(String name);
    @Select("Select  name from subcategory ")
    String[] findSubcategory();
///////////////////////////////////////////////////////////
//              User SQL
///////////////////////////////////////////////////////////
    @Select("Select * from usr where username = #{username} limit 1")
    User findUserByUserName(String username);

    @Select("Select * from usr where nickname = #{nickname} limit 1")
    User findUserByNickname(String nickname);

    @Insert("Insert into usr (username, role, activationCode) values (#{username}, #{role}, #{activationCode})")
    void insertUser(String username, String role, String activationCode);

    @Update("Update usr set password = #{password}, activationCode='' where id = #{id}")
    void updatePassword(String password, Long id);

    @Update("Update usr set nickname = #{nickname} where id = #{id}")
    void updateNickName(String nickname, Long id);

    @Select("Select username,role,activationCode,nickname from usr")
    List<User> findAllUsers();

    @Select("Select username from usr where id=#{id}")
    String findUserById(Long id);

    @Select("Select id,nickname,role from usr where password is NOT NULL")
    List<User> findUsers();

    ///////////////////////////////////////////////////////////
//              Comment SQL
///////////////////////////////////////////////////////////
    @Select("Select comment.id,comment.text, comment.usr, usr.nickname as user, comment.date from comment left join usr on comment.usr = usr.id where tender = #{tender} order by date DESC")
    List<Comment> findAllCommentsByTender(Long tender);

    @Insert("Insert into comment (text,usr,date,tender) values (#{text},#{usr},#{date},#{tender})")
    void insertComment(String text, Long usr, ZonedDateTime date, Long tender);

    @Select("SELECT count(*) as count FROM keysight.comment WHERE tender=#{tender}")
    Long CountCommentByTender(Long tender);

    ///////////////////////////////////////////////////////////
//              Buffer SQL
///////////////////////////////////////////////////////////
    @Update("Update buffer set buf = #{buf} where id = #{id}")
    void upadateBuffer(String buf, Long id);

    @Select("Select buf from buffer where id = #{id}")
    String SelectBuf(Long id);
    ///////////////////////////////////////////////////////////
//              Option SQL
///////////////////////////////////////////////////////////
    @Select("Select * from options")
    List<Option> getAllOptions();
    @Select("Select * from options where name = #{name} limit 1")
    Option CheckOption(String name);
    @Insert("Insert into options (name) values (#{name})")
    void insertOptions(String name);
    ///////////////////////////////////////////////////////////
//              Option_product SQL
///////////////////////////////////////////////////////////
    @Select("Select options from options_product where product_category = #{product_category} and id_product = #{id_product}")
    List<Long> getAllOptionsByProduct(Long product_category, Long id_product);
    @Select("Select options.id,options.name from options_product left join options on options_product.options = options.id where product_category = #{product_category} and id_product = #{id_product}")
    List<Option> getAllOptionsByProductForOrders(Long product_category, Long id_product);
    @Insert("Insert into options_product (product_category,id_product,options) values (#{product_category},#{id_product},#{option})")
    void insertOptionsByProduct(Long product_category,Long id_product,Long option);
    @Delete("Delete from options_product where id = #{id}")
    void deleteOptionsByProduct(Long id);
    ///////////////////////////////////////////////////////////
//              Option_orders SQL
///////////////////////////////////////////////////////////
    @Select("Select options from options_orders where options_orders.orders = #{order}")
    List<Long> getAllOptionsByOrder(Long order);
    @Insert("Insert into options_orders (options_orders.orders, options_orders.options) values (#{order},#{options})")
    void insertOptionsOrders(Long order, Long options);
    @Delete("Delete from options_orders where id = #{id}")
    void deleteOptionsOrder(Long id);
    @Select("select GROUP_CONCAT(`name` separator ' ')  from options_orders left join options on options_orders.options = options.id where options_orders.orders = #{order}")
    String SelectOptionsForOrdes(Long order);
    ///////////////////////////////////////////////////////////
//              Product SQL
///////////////////////////////////////////////////////////
    @Select("Select id from product where product_category = #{product_category_id} and id_product = #{id} limit 1")
    Long CheckProduct(Product product);
    @Insert("Insert into product (id_product,product_category, subcategory, vendor_code, vendor, frequency,usb,vxi,portable,channel,port,form_factor,purpose,voltage,current)" +
            " values (#{id},#{product_category_id}, #{subcategory_id},trim(#{vendor_code}), #{vendor_id}, #{frequency},#{usb},#{vxi},#{portable},#{channel},#{port},#{form_factor},#{purpose},#{voltage},#{current})")
    void InsertIntoProduct(Product product);
    ///////////////////////////////////////////////////////////
//              SaveParameters SQL
///////////////////////////////////////////////////////////
    @Select("Select * from search_parameters")
    List<SearchParametersFromDB> search_parameters();
    @Select("Select count(*) from search_parameters where name = #{name}")
    Integer countSearchParametersByName(String name);
    @Select("Select id from search_parameters where name = #{name}")
    Long idSearchParametersByName(String name);
    @Select("Select count(*) from search_parameters where id = #{id}")
    Integer countSearchParametersById(Long id);
    @Insert("Insert into search_parameters (nickname,name,dateStart,dateFinish,dublicate,typeExclude,type,customExclude,custom,innCustomer,country,winnerExclude,winner,minSum,maxSum,ids,bicotender,numberShow,product, region, district) values (#{nickname}, #{name},#{dateStart},#{dateFinish},#{dublicate},#{typeExclude},#{type},#{customExclude},#{custom},#{innCustomer},#{country},#{winnerExclude},#{winner},#{minSum},#{maxSum},#{ids},#{bicotender},#{numberShow},#{product},#{region},#{district})")
    void saveParameters(String nickname, String name,ZonedDateTime dateStart,ZonedDateTime dateFinish,Boolean dublicate,Boolean typeExclude, String type,Boolean customExclude,String custom,String innCustomer,Long country, Boolean winnerExclude, String winner,BigDecimal minSum, BigDecimal maxSum,String ids, String bicotender,Boolean numberShow,String product, String region,String district);
    @Update("Update search_parameters set nickname=#{nickname}, name=#{name}, dateStart=#{dateStart}, dateFinish=#{dateFinish}, dublicate = #{dublicate}, typeExclude = #{typeExclude}, type = #{type}, customExclude = #{customExclude}, custom = #{custom}, innCustomer = #{innCustomer}, country = #{country}, winnerExclude= #{winnerExclude}, winner = #{winner}, minSum = #{minSum}, maxSum = #{maxSum}, ids = #{ids}, bicotender = #{bicotender}, numberShow = #{numberShow}, product = #{product},region = #{region},district = #{district} where id = #{id}")
    void updateParameters(Long id,String nickname, String name,ZonedDateTime dateStart,ZonedDateTime dateFinish,Boolean dublicate,Boolean typeExclude, String type,Boolean customExclude,String custom,String innCustomer,Long country, Boolean winnerExclude, String winner,BigDecimal minSum, BigDecimal maxSum,String ids, String bicotender,Boolean numberShow,String product, String region,String district);
    @Delete("Delete from search_parameters where id = #{id}")
    void deleteSearchParametersById(Long id);
///////////////////////////////////////////////////////////
//              Region SQL
///////////////////////////////////////////////////////////
    @Select("Select * from region")
    List<Region> selectRegion();
///////////////////////////////////////////////////////////
//              District SQL
///////////////////////////////////////////////////////////
    @Select("Select * from district")
    List<District> selectDistrict();
    @Select("Select region.number from region_district left join region on region = region.id where district = #{idDistrict}")
    String[] regionInDistrict(Long idDistrict);
}

