package com.application_tender.tender.mapper;

import com.application_tender.tender.models.*;
import com.application_tender.tender.subsidiaryModels.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
@Mapper
public interface TableMapper {

    @Select("Select * from orders")
        List<Orders> findAllOrders();



    @Select("Select * from pulse_generator")
        List<PulseGenerator> findAllPulseGenerator();
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
///////////////////////////////////////////////////////////
//              Oscilloscope SQL
///////////////////////////////////////////////////////////
    @Select("Select * from oscilloscope")
        List<Oscilloscope> findAllOscilloscope();
    @Select("Select oscilloscope.id, vendor_code, frequency, usb, vxi, name as vendor from oscilloscope left join vendor v on oscilloscope.vendor = v.id")
        List<Product> findAllOscilloscopeToProduct();
///////////////////////////////////////////////////////////
//              AnotherProduct SQL
///////////////////////////////////////////////////////////
    @Select("Select * from another_product")
        List<AnotherProduct> findAllAnotherProduct();
    @Select("Select id,name as vendor_code from another_product")
        List<Product> findAllAnotherProductToProduct();
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

}
