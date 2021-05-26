package com.application_tender.tender.mapper;

import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.Product;
import com.application_tender.tender.subsidiaryModels.ReportQuarter;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
@Mapper
public interface TableMapper {
    final String atributTender = "tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as winner, product";
///////////////////////////////////////////////////////////
//              Tender SQL
///////////////////////////////////////////////////////////
    @Select("Select " + atributTender +
        " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner where tender.id > 4378")
        List<Tender> findAllTender();

    @Select("Select id from tender where number_tender = #{number_tender}")
        Long findTenderByNumber_tender(String number_tender);

    @Select("Select " + atributTender +
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner where tender.id = #{id}")
        Tender findTenderbyId(Long id);
    ////
    @Select("Select " + atributTender  +
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner where date_start > '2021-01-01' and price > 0 and sum = 0")
        List<Tender> findTender();
    ///
    @Select("Select " + atributTender +
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner" +
            " where date_start between #{start_period} and #{finish_period} and customer like #{customer} and typetender like #{type} and winner like #{winner} and price between #{strat_price} and #{finish_price} order by date_start")
        List<Tender> findAllTenderTerms(ZonedDateTime start_period, ZonedDateTime finish_period, String type, String winner, String customer, BigDecimal strat_price, BigDecimal finish_price);
    // @Select("SELECT * from keysight.tender where year(date_start) = #{year} and quarter(date_start) = #{quarter}")
   //     List<Tender> findForOrders(int year, int quarter);
    @Insert("insert into tender (name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding,number_tender,  full_sum, win_sum, currency, price, rate, sum, customer, typetender, winner) " +
            "values (#{name_tender}, #{bico_tender},#{gos_zakupki},#{date_start}, #{date_finish},#{date_tranding},#{number_tender},  #{full_sum}, #{win_sum}, #{currency}, #{price}, #{rate}, #{sum}, #{customer}, #{typetender}, #{winner}) ")
        Long insertTender(String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start,ZonedDateTime date_finish,ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, BigDecimal win_sum, String currency, BigDecimal price, Double rate, BigDecimal sum, Long customer, Long typetender, Long winner);
    @Update("Update tender set product = #{product} where id = #{id}")
        Long UpdateProduct(String product, Long id);
    @Update("Update tender set price = #{price}, sum = #{sum} where id = #{id}")
        Long UpdateSum(BigDecimal price, BigDecimal sum,Long id);
    @Update("Update tender set rate =  #{rate}, sum = #{sum} where id = #{id}")
        Long UpdateRate(Double rate, BigDecimal sum, Long id);
    @Update("Update tender set date_start = #{date_start}, date_finish= #{date_finish} where id = #{id}")
        Long UpdateDate(Long id, ZonedDateTime date_start,ZonedDateTime date_finish);
///////////////////////////////////////////////////////////
//              ProductCategory SQL
///////////////////////////////////////////////////////////
    @Select("Select id,category from product_category")
        List<ProductCategory> findAllProductCategory();
    @Select("Select category from product_category where id = #{id} limit 1")
        String findOneCategoryById(Long id);
    @Select("Select category_en from product_category where id = #{id} limit 1")
        String findOneCategoryENById(Long id);
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
//              Contry SQL
///////////////////////////////////////////////////////////
    @Select("Select * from contry")
        List<Contry> findAllContry();
    @Select("Select * from contry where id = #{id}")
        List<Contry> findAllContryById(Long id);
    @Select("Select * from contry where name = #{name}")
        List<Contry> findAllContryByName(String name);
///////////////////////////////////////////////////////////
//              Customer SQL
///////////////////////////////////////////////////////////
    @Select("Select * from customer")
        List<Customer> findAllCustomer();
    @Select("Select id from customer where inn = #{inn} limit 1")
        Long findCustomerByInn(String inn);
    @Select("Select id from customer where name = #{name} limit 1")
        Long findCustomerByName(String name);
    @Select("Select id from customer where name = #{name} and inn = #{inn} limit 1")
        Long findCustomerByNameandINN(String name, String inn);
    @Select("Select inn from customer where id = #{id} limit 1")
        String findCustomerInnById(Long id);
    @Insert("INSERT into customer (name,inn, contry) values (#{name},#{inn},#{contry})")
        Long insertCustomer(String name, String inn, Long contry);
    @Update("UPDATE customer SET inn = #{inn} WHERE id = #{id}")
        Long updateCustomerInn(String inn,Long id);
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
    @Select("Select * from winner")
        List<Winner> findAllWinner();

///////////////////////////////////////////////////////////
//              Vendor SQL
///////////////////////////////////////////////////////////
    @Select("Select * from vendor")
        List<Vendor> findAllVendor();
    @Select("Select name from vendor where id = #{id}")
        String findOneVendorById(Long id);
///////////////////////////////////////////////////////////
//              Order SQL
///////////////////////////////////////////////////////////
    @Select("Select * from orders")
        List<OrdersDB> findAllOrders();
    @Select("Select id,comment,id_product,product_category,tender,number,price,win_price as winprice from orders where tender = #{tender}")
        List<OrdersDB> findAllOrdersBDbyTender(Long tender);
    @Select("Select id from orders where tender = #{tender}")
        List<Long> findAllOrdersIdbyTender(Long tender);
    @Select("Select id,comment,id_product,product_category,tender,number,price,win_price as winprice from orders where tender = #{tender} and product_category = #{product_category}")
        List<OrdersDB> findAllOrdersbyTender(Long tender, Long product_category);
    @Insert("insert into orders (comment, id_product,product_category,tender,number,price,win_price) values (#{comment}, #{id_product},#{product_category},#{tender},#{number},#{price},#{win_price})")
        Long insertOrder(String comment,Long id_product, Long product_category,Long tender,int number,BigDecimal price,BigDecimal win_price);
    @Update("update orders set comment = #{comment}, id_product = #{id_product},product_category = #{product_category},tender = #{tender},number = #{number},price = #{price},win_price = #{win_price} where id = #{id}")
        Long updateOrder(Long id,String comment,Long id_product, Long product_category,Long tender,int number,BigDecimal price,BigDecimal win_price);
    @Delete("Delete from orders where id = #{id}")
        Long deleteOrder(Long id);
///////////////////////////////////////////////////////////
//              Order-Tenders SQL
///////////////////////////////////////////////////////////
    @Select("SELECT count(*) as count FROM keysight.tender WHERE NOT EXISTS (SELECT orders.ID FROM keysight.orders WHERE tender.ID = orders.tender)")
        Long findCountTenderWithoutOrders();
    @Select("SELECT "+ atributTender +
            " FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner WHERE NOT EXISTS (SELECT orders.ID FROM keysight.orders WHERE tender.ID = orders.tender)")
    List<Tender> findTenderWithoutOrders();
    @Select("SELECT " + atributTender +
            "        FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner right join orders o on tender.id = o.tender where o.product_category = 7 and o.id_product = 255")
    List<Tender> findTendernoDocumentation();
    @Select("SELECT count(*) as count, sum(sum) as sum, #{year} as year, #{quarter} as quarter from (Select distinct tender.id, tender.sum as sum from keysight.tender join keysight.orders on orders.tender = tender.id where year(date_start) = #{year} and quarter(date_start) = #{quarter} and product_category = #{category}) as c")
        ReportQuarter findForOrders(int year, int quarter, Long category);
    @Select("SELECT name from (Select distinct tender.id, vendor.name as name from keysight.tender join keysight.orders on orders.tender = tender.id left join ${category_en} as prod on prod.id = orders.id_product left join vendor on  prod.vendor = vendor.id where year(date_start) = #{year} and quarter(date_start) = #{quarter} and product_category = #{category}) as c")
        List<String> findVendorForOrders(int year, int quarter, Long category, String category_en);
    @Select("SELECT trim(name) from (Select distinct tender.id, orders.comment as name from keysight.tender join keysight.orders on orders.tender = tender.id left join ${category_en} as prod on prod.id = orders.id_product where year(date_start) = #{year} and quarter(date_start) = #{quarter} and product_category = #{category} and prod.vendor = 1) as c")
        List<String> findNoVendorForOrders(int year, int quarter, Long category, String category_en);
    @Select("SELECT " +atributTender +
            " FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner  left join orders o on tender.id = o.tender WHERE o.id_product like #{product} and  o.product_category = #{category} and (date_start between #{dateStart} and #{dateFinish})"
            )
    List<Tender> TenderOnProduct (Long category, ZonedDateTime dateStart, ZonedDateTime dateFinish, String product);
///////////////////////////////////////////////////////////
//              Universal SQL
///////////////////////////////////////////////////////////
    @Select("Select vendor  from ${category} where id = #{id} limit 1")
        Long findOneUniversalById(Long id, String category);

}
