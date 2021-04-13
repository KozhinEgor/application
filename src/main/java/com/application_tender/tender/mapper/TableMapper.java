package com.application_tender.tender.mapper;

import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
@Mapper
public interface TableMapper {
///////////////////////////////////////////////////////////
//              Tender SQL
///////////////////////////////////////////////////////////
    @Select("Select tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,number_tender,  full_sum, win_sum, currency, price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as winner" +
        " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner")
        List<Tender> findAllTender();

    @Select("Select id from tender where number_tender = #{number_tender}")
        Long findTenderByNumber_tender(String number_tender);

    @Select("Select tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,number_tender,  full_sum, win_sum, currency, price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as winner" +
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner where tender.id = #{id}")
        Tender findTenderbyId(Long id);
    @Select("Select tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,number_tender,  full_sum, win_sum, currency, price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as win" +
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner" +
            " where date_start between #{start_period} and #{finish_period} and customer like #{customer} and typetender like #{type} and winner like #{winner} and price between #{strat_price} and #{finish_price} order by date_start")
        List<Tender> findAllTenderTerms(ZonedDateTime start_period, ZonedDateTime finish_period, String type, String winner, String customer, BigDecimal strat_price, BigDecimal finish_price);
    @Insert("insert into tender (name_tender, bico_tender,gos_zakupki,date_start, date_finish,number_tender,  full_sum, win_sum, currency, price, rate, sum, customer, typetender, winner) " +
            "values (#{name_tender}, #{bico_tender},#{gos_zakupki},#{date_start}, #{date_finish},#{number_tender},  #{full_sum}, #{win_sum}, #{currency}, #{price}, #{rate}, #{sum}, #{customer}, #{typetender}, #{winner}) ")
        Long insertTender(String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start,ZonedDateTime date_finish, String number_tender, BigDecimal full_sum, BigDecimal win_sum, String currency, BigDecimal price, Double rate, BigDecimal sum, Long customer, Long typetender, Long winner);
///////////////////////////////////////////////////////////
//              ProductCategory SQL
///////////////////////////////////////////////////////////
    @Select("Select id,category from product_category")
        List<ProductCategory> findAllProductCategory();
    @Select("Select category from product_category where id = #{id} limit 1")
        String findOneCategoryById(Long id);
///////////////////////////////////////////////////////////
//              Spectrum_analyser SQL   spectrum_analyser
///////////////////////////////////////////////////////////
    @Select("Select * from spectrum_analyser")
        List<Spectrum_analyzers> findAllSpectrum_analysers();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, portable,  name as vendor from spectrum_analyser as pr left join vendor v on pr.vendor = v.id")
        List<Product> findAllSpectrum_analyserToProduct();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, portable,  name as vendor from spectrum_analyser as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
        Product findAllSpectrum_analyserToProductById(Long id_product);
    @Select("Select vendor_code, vendor as vendor_id from spectrum_analyser where id = #{id} limit 1")
        Product findOneSpectrum_analyserById(Long id);
///////////////////////////////////////////////////////////
//              SignalGenerator SQL     signal_generator
///////////////////////////////////////////////////////////
    @Select("Select * from signal_generator")
        List<SignalGenerator> findAllSignalGenerator();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from signal_generator as pr left join vendor v on pr.vendor = v.id")
        List<Product> findAllSignalGeneratorToProduct();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from signal_generator as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
        Product findAllSignalGeneratorToProductById(Long id_product);
    @Select("Select vendor_code, vendor as vendor_id from signal_generator where id = #{id} limit 1")
        Product findOneSignalGeneratorById(Long id);
///////////////////////////////////////////////////////////
//              PulseGenerator SQL      pulse_generator
///////////////////////////////////////////////////////////
    @Select("Select * from pulse_generator")
        List<PulseGenerator> findAllPulseGenerator();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from pulse_generator as pr left join vendor v on pr.vendor = v.id")
        List<Product> findAllPulseGeneratorToProduct();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, name as vendor from pulse_generator as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
        Product findAllPulseGeneratorToProductById(Long id_product);
    @Select("Select vendor_code, vendor as vendor_id from pulse_generator where id = #{id} limit 1")
        Product findOnePulseGeneratorById(Long id);
///////////////////////////////////////////////////////////
//              SignalAnalyzer SQL      signal_analyzer
///////////////////////////////////////////////////////////
    @Select("Select * from signal_analyzer")
        List<SignalAnalyzer> findAllSignalAnalyzer();
    @Select("Select pr.id, vendor_code, frequency, vendor as vendor_id, name as vendor from signal_analyzer as pr left join vendor v on pr.vendor = v.id")
        List<Product> findAllSignalAnalyzerToProduct();
    @Select("Select pr.id, vendor_code, frequency, vendor as vendor_id, name as vendor from signal_analyzer as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
        Product findAllSignalAnalyzerToProductById(Long id_product);
    @Select("Select vendor_code, vendor as vendor_id from signal_analyzer where id = #{id} limit 1")
        Product findOneSignalAnalyzerById(Long id);
///////////////////////////////////////////////////////////
//              Oscilloscope SQL        oscilloscope
///////////////////////////////////////////////////////////
    @Select("Select * from oscilloscope")
        List<Oscilloscope> findAllOscilloscope();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, vxi, channel, name as vendor from oscilloscope as pr left join vendor v on pr.vendor = v.id")
        List<Product> findAllOscilloscopeToProduct();
    @Select("Select pr.id, vendor_code, frequency,vendor as vendor_id, usb, vxi, channel, name as vendor from oscilloscope as pr left join vendor v on pr.vendor = v.id where pr.id = #{id_product} limit  1")
        Product findAllOscilloscopeToProductById(Long id_product);
    @Select("Select vendor_code, vendor as vendor_id from oscilloscope where id = #{id} limit 1")
        Product findOneOscilloscopeById(Long id);
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
///////////////////////////////////////////////////////////
//              Customer SQL
///////////////////////////////////////////////////////////
    @Select("Select * from customer")
        List<Customer> findAllCustomer();
    @Select("Select id from customer where inn = #{inn}")
        Long findCustomerByInn(String inn);
    @Select("Select id from customer where name = #{name}")
        Long findCustomerByName(String name);
    @Select("Select inn from customer where id = #{id}")
        String findCustomerInnById(Long id);
    @Insert("INSERT into customer (name,inn) values (#{name},#{inn})")
        Long insertCustomer(String name, String inn);
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
    @Insert("insert into orders (comment, id_product,product_category,tender,number,price,win_price) values (#{comment}, #{id_product},#{product_category},#{tender},#{number},#{price},#{win_price})")
        Long insertOrder(String comment,Long id_product, Long product_category,Long tender,int number,BigDecimal price,BigDecimal win_price);
    @Update("update orders set comment = #{comment}, id_product = #{id_product},product_category = #{product_category},tender = #{tender},number = #{number},price = #{price},win_price = #{win_price} where id = #{id}")
        Long updateOrder(Long id,String comment,Long id_product, Long product_category,Long tender,int number,BigDecimal price,BigDecimal win_price);
    @Delete("Delete from orders where id = #{id}")
        Long deleteOrder(Long id);
}
