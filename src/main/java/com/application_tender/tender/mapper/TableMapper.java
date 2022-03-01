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
    final String atributTender = "distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as winner, tender.product, dublicate, country.name as country";
    final String atributAdjacentTender = "distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, dublicate, country.name as country";
///////////////////////////////////////////////////////////
//              Tender SQL
///////////////////////////////////////////////////////////

    @Select("Select id from tender where number_tender = #{number_tender}")
    Long findTenderByNumber_tender(String number_tender);

    @Select("Select tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.id as winner, product, dublicate, country.name as country, w.inn as winner_inn, winner_country.name as winner_country, " +
            "(Select GROUP_CONCAT(tender_plan.tender_plan separator ' ') from tender_plan where tender_plan.tender = tender.id) as tender_plan, "+"(Select GROUP_CONCAT(tender_dublicate.tender_dublicate separator ' ') from tender_dublicate where tender_dublicate.tender = tender.id) as tender_dublicate "+
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id left join country as winner_country on w.country = winner_country.id where tender.id = #{id}")
    Tender findTenderbyId(Long id);

    @Select("Select distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.id as winner, product, dublicate, country.name as country, w.inn as winner_inn, winner_country.name as winner_country, " +
            "(Select GROUP_CONCAT(tender_plan.tender_plan separator ' ') from tender_plan where tender_plan.tender = tender.id) as tender_plan, "+"(Select GROUP_CONCAT(tender_dublicate.tender_dublicate separator ' ') from tender_dublicate where tender_dublicate.tender = tender.id) as tender_dublicate "+
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id left join country as winner_country on w.country = winner_country.id" +
            " where date_start>=#{date_start} and date_start<=#{date_finish} and tender.customer = #{customer}  and dublicate = false")
    List<Tender> EmailTender(ZonedDateTime date_start, ZonedDateTime date_finish, Long customer);
    ////
    @Select("Select distinct tender.id,name_tender,number_tender from tender where date_start>'2021-04-01' and date_start<'2021-08-01'")
    List<Tender> findNameTenderByDate();

    @Select("Select id from tender where winner = #{winner}")
    List<Long> findTenderByWinner(Long winner);

    @Select("Select id from tender where customer = #{customer}")
    List<Long> findTenderByCustomer(Long customer);

    @Select("Select number_tender from tender where customer = #{customer} limit 1")
    String BicoNumberbyCustomer(Long customer);

    @Select("Select distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.id as winner, product, dublicate, country.name as country, w.inn as winner_inn, winner_country.name as winner_country"+
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id left join country as winner_country on w.country = winner_country.id" +
            " where (INSTR('${name_tender}', SUBSTRING(tender.name_tender,1,length(name_tender)-3))) and (tender.date_start >= date_sub('${date}', interval 3 month)) and (c.inn = #{inn}) and (tender.full_sum = #{full_sum}) and (tender.id <> #{id})" )
    List<Tender> SelectNameDublicate(BigDecimal full_sum,String name_tender,String inn, String date, Long id);

    @Select("Select distinct tender.id,name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.id as winner, product, dublicate, country.name as country, w.inn as winner_inn, winner_country.name as winner_country"+
            " from tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id left join country as winner_country on w.country = winner_country.id" +
            " where (tender.date_start >= date_sub('${date}', interval 3 month)) and (c.inn = #{inn}) and (tender.full_sum = #{full_sum}) order by date_start DESC " )
    List<Tender> SelectDublicate(BigDecimal full_sum,String inn, String date);
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
            " left join subcategory on pr.subcategory = subcategory.id"+
            " left join vendor on pr.vendor = vendor.id" +
            " ${where} order by date_start")
    List<Tender> findAllTenderTerms(String where);

    @Select("Select " + atributTender +
            " from tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join winner w on w.id = tender.winner" +
            " left join country on c.country = country.id" +
            " left join orders on orders.tender = tender.id" +
            " left join product as pr on pr.id = orders.product" +
            " left join product_category on orders.product_category = product_category.id" +
            " left join subcategory on pr.subcategory = subcategory.id"+
            " left join vendor on pr.vendor = vendor.id" +
            " ${where} ${orderBy}")
    List<Tender> findTenderTerms(String where, String orderBy);

    @Select("Select Count(distinct tender.id) from tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join winner w on w.id = tender.winner" +
            " left join country on c.country = country.id" +
            " left join orders on orders.tender = tender.id" +
            " left join product as pr on pr.id = orders.product" +
            " left join product_category on orders.product_category = product_category.id" +
            " left join subcategory on pr.subcategory = subcategory.id"+
            " left join vendor on pr.vendor = vendor.id" +
            " ${where}")
    Long findCountTender(String where);

    @Select("select ROUND(sum(tabl.s),2) from(Select distinct tender.id,tender.sum as s from tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join winner w on w.id = tender.winner" +
            " left join country on c.country = country.id" +
            " left join orders on orders.tender = tender.id" +
            " left join product as pr on pr.id = orders.product" +
            " left join product_category on orders.product_category = product_category.id" +
            " left join subcategory on pr.subcategory = subcategory.id"+
            " left join vendor on pr.vendor = vendor.id" +
            " ${where} ) as tabl")
    BigDecimal findSumTender(String where);

    @Select("select ROUND(sum(tabl.w),2) from(Select distinct tender.id," +
            "               if(win_sum > tender.price , win_sum * (tender.price / tender.full_sum) * tender.rate," +
            "                  win_sum * tender.rate) as w" +
            "              from tender" +
            "            left join customer c on c.id = tender.customer" +
            "            left join typetender t on t.id = tender.typetender" +
            "            left join winner w on w.id = tender.winner" +
            "            left join country on c.country = country.id" +
            "            left join orders on orders.tender = tender.id" +
            "            left join product as pr on pr.id = orders.product" +
            "            left join product_category on orders.product_category = product_category.id" +
            "            left join subcategory on pr.subcategory = subcategory.id" +
            "             left join vendor on pr.vendor = vendor.id ${where} ) as tabl")
    BigDecimal findWinSumTender(String where);

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

    @Update("Update tender set dublicate = 1 where tender.id = #{id}")
    void changeDublicate(Long id);

    @Update("Update tender set dublicate = 0 where tender.id = #{id}")
    void deleteDublicate(Long id);

    @Delete("DELETE FROM tender where tender.id = #{id} limit 1")
    void DeleteTender(Long id);

    ///////////////////////////////////////////////////////////
//              AdjacentTender SQL adjacent_tender
///////////////////////////////////////////////////////////
    @Select("Select id from adjacent_tender where number_tender = #{number_tender}")
    Long findAdjacentTenderByNumber_tender(String number_tender);

    @Select("Select " + atributAdjacentTender +
            " from adjacent_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id where tender.id = #{id}")
    Tender findAdjacentTenderbyId(Long id);

    @Select("Select " + atributAdjacentTender +
            " from adjacent_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id" +
            " where date_start>=#{date_start} and date_start<=#{date_finish} and tender.customer = #{customer}  ")
    List<Tender> EmailAdjacentTender(ZonedDateTime date_start, ZonedDateTime date_finish, Long customer);

    @Select("Select id from adjacent_tender where customer = #{customer}")
    List<Long> findAdjacentTenderByCustomer(Long customer);

    @Select("Select " + atributAdjacentTender +
            " from adjacent_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id" +
            " ${where} order by date_start")
    List<Tender> findAllAdjacentTenderTerms(String where);

    @Select("Select " + atributAdjacentTender +
            " from adjacent_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id" +
            " ${where} ${orderBy}")
    List<Tender> findAdjacentTenderTerms(String where, String orderBy);

    @Select("Select Count(tender.id) from adjacent_tender tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join country on c.country = country.id" +
            " ${where}")
    Long findCountAdjacentTender(String where);

    @Select("Select sum(tender.sum) from adjacent_tender tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join country on c.country = country.id" +
            " ${where}")
    BigDecimal findSumAdjacentTender(String where);


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
//              PlanTender SQL plan_schedule_tender
///////////////////////////////////////////////////////////
    @Select("Select id from plan_schedule_tender where number_tender = #{number_tender}")
    Long findPlanTenderByNumber_tender(String number_tender);

    @Select("Select " + atributAdjacentTender + ",true as plan, (Select GROUP_CONCAT(tender_plan.tender separator ' ') from tender_plan where tender_plan.tender_plan = tender.id) as tender_plan "+
            " from plan_schedule_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id  where tender.id = #{id}")
    Tender findPlanTenderbyId(Long id);

    @Select("Select id from plan_schedule_tender where customer = #{customer}")
    List<Long> findPlanTenderByCustomer(Long customer);

    @Select("Select " + atributAdjacentTender +",true as plan, (Select GROUP_CONCAT(tender_plan.tender separator ' ') from tender_plan where tender_plan.tender_plan = tender.id) as tender_plan "+
            " from plan_schedule_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id " +
            " ${where} order by date_start")
    List<Tender> findAllPlanTenderTerms(String where);

    @Select("Select " + atributAdjacentTender +",true as plan, (Select GROUP_CONCAT(tender_plan.tender separator ' ') from tender_plan where tender_plan.tender_plan = tender.id) as tender_plan "+
            " from plan_schedule_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id " +
            " ${where} ${orderBy}")
    List<Tender> findPlanTenderTerms(String where, String orderBy);

    @Select("Select Count(tender.id) from plan_schedule_tender tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join country on c.country = country.id" +
            " ${where}")
    Long findCountPlanTender(String where);

    @Select("Select sum(tender.sum) from plan_schedule_tender tender" +
            " left join customer c on c.id = tender.customer" +
            " left join typetender t on t.id = tender.typetender" +
            " left join country on c.country = country.id" +
            " ${where}")
    BigDecimal findSumPlanTender(String where);

    @Select("Select " + atributAdjacentTender +",true as plan, (Select GROUP_CONCAT(tender_plan.tender separator ' ') from tender_plan where tender_plan.tender_plan = tender.id) as tender_plan "+
            " from plan_schedule_tender as tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join country on c.country = country.id " +
            " where  (tender.full_sum = #{full_sum}) and (INSTR('${name_tender}'," +
            " SUBSTRING(tender.name_tender,1,Position('(План График)' in tender.name_tender)-3))) and (tender.date_start <= date_add('${date}', interval 7 day)) and (c.inn = #{inn})")
    List<Tender> SelectNameDublicatePlan(BigDecimal full_sum,String name_tender,String inn, String date);


    @Insert("insert into plan_schedule_tender (name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding,number_tender,  full_sum, currency, price, rate, sum, customer, typetender) " +
            "values (#{name_tender}, #{bico_tender},#{gos_zakupki},#{date_start}, #{date_finish},#{date_tranding},#{number_tender},  #{full_sum}, #{currency}, #{price}, #{rate}, #{sum}, #{customer}, #{typetender}) ")
    void insertPlanTender(String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start, ZonedDateTime date_finish, ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, String currency, BigDecimal price, Double rate, BigDecimal sum, Long customer, Long typetender);

    @Update("Update plan_schedule_tender set customer = #{customer} where plan_schedule_tender.id = #{tender}")
    void changeCustomerPlanTender(Long tender, Long customer);

    @Update("Update plan_schedule_tender set  name_tender =#{name_tender} , bico_tender=#{bico_tender},gos_zakupki=#{gos_zakupki},date_start=#{date_start}, date_finish=#{date_finish},date_tranding=#{date_tranding} ,number_tender=#{number_tender},  full_sum=#{full_sum}, currency=#{currency}, price=#{price}, rate=#{rate}, sum=#{sum}, customer=#{customer}, typetender=#{typetender}, dublicate=#{dublicate} where id = #{id}")
    void UpdatePlanTender(Long id, String name_tender, String bico_tender, String gos_zakupki, ZonedDateTime date_start, ZonedDateTime date_finish, ZonedDateTime date_tranding, String number_tender, BigDecimal full_sum, String currency, BigDecimal price, double rate, BigDecimal sum, Long customer, Long typetender, Boolean dublicate);


    @Delete("DELETE FROM plan_schedule_tender where id = #{id} limit 1")
    void DeletePlanTender(Long id);

    ///////////////////////////////////////////////////////////
//              ProductCategory SQL
///////////////////////////////////////////////////////////
    @Select("Select id,category,category_en,category_product from product_category order by category")
    List<ProductCategory> findAllProductCategory();

    @Select("Select category from product_category where id = #{id} limit 1")
    String findOneCategoryById(Long id);

    @Select("Select * from product_category where id = #{id} limit 1")
    ProductCategory findOneCategoryFullById(Long id);

    @Select("Select id from product_category where category_en = #{str}")
    Long FindIdCategoryByName_en(String str);

    @Select("Select id,category,category_en,category_product from product_category where id = #{id} limit 1")
    ProductCategory findCategoryById(Long id);

    @Select("Select category_en from product_category where id = #{id} limit 1")
    String findOneCategoryENById(Long id);

    @Insert("Insert into product_category (category,category_en, category_product,subcategory, frequency,usb,vxi,portable,channel,port,form_factor,purpose,voltage,current) " +
            "values (#{name},#{name_en}, #{category},#{subcategory_boolean}, #{frequency},#{usb},#{vxi},#{portable},#{channel},#{port},#{form_factor},#{purpose},#{voltage},#{current})")
    void InsertCategory(NewTable table);

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

    @Select("Select customer.id,inn,customer.name as name,country.name as country  from customer left join country on customer.country = country.id where customer.id not in (Select distinct customer from tender) and customer.id not in (Select distinct customer from adjacent_tender) and customer.id not in (Select distinct customer from plan_schedule_tender)")
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

    @Select("Select winner.id,winner.name,c.name as country, winner.inn from winner left join country as c on c.id = winner.country where winner.inn = #{inn}")
    Company findCompany(String inn);

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

    @Select("Select o.id,comment as comment_DB,o.tender,o.number,o.price," +
            "p.product_category as product_category_DB,pc.category as product_category,p.vendor_code as product,  o.product as product_DB, p.vendor as vendor_DB, v.name as vendor, s.id as subcategory_DB, s.name as subcategory, " +
            "o.frequency,o.usb,o.vxi,o.portable,o.channel,o.port,o.form_factor,o.purpose,o.voltage,o.current,o.options from orders as o " +
            "left join product as p on o.product = p.id " +
            "left join product_category as pc on p.product_category = pc.id " +
            "left join vendor as v on p.vendor = v.id " +
            "left join subcategory as s on p.subcategory = s.id " +
            "where tender = #{tender} order by o.id")
    List<Orders> findAllOrdersbyTender(Long tender);

    @Select("Select o.id,comment as comment_DB,o.tender,o.number,o.price," +
            "p.product_category as product_category_DB,pc.category as product_category,p.vendor_code as product,  o.product as product_DB, p.vendor as vendor_DB, v.name as vendor, s.id as subcategory_DB, s.name as subcategory, " +
            "o.frequency,o.usb,o.vxi,o.portable,o.channel,o.port,o.form_factor,o.purpose,o.voltage,o.current,o.options from orders as o " +
            "left join product as p on o.product = p.id " +
            "left join product_category as pc on p.product_category = pc.id " +
            "left join vendor as v on p.vendor = v.id " +
            "left join subcategory as s on p.subcategory = s.id "+
            "where tender = #{tender} and o.product = 1491")
    Orders findAnotherProductbyTender(Long tender);

    @Select("Select o.id,comment as comment_DB,o.tender,o.number,o.price," +
            "p.product_category as product_category_DB,pc.category as product_category,p.vendor_code as product,  o.product as product_DB, p.vendor as vendor_DB, v.name as vendor, s.id as subcategory_DB, s.name as subcategory, " +
            " o.frequency,o.usb,o.vxi,o.portable,o.channel,o.port,o.form_factor,o.purpose,o.voltage,o.current,o.options from orders as o " +
            "left join product as p on o.product = p.id " +
            "left join product_category as pc on p.product_category = pc.id " +
            "left join vendor as v on p.vendor = v.id " +
            "left join subcategory as s on p.subcategory = s.id "+
            "where o.id = #{id}")
    Orders findProductbyId(Long id);

    @Select("Select id from orders where tender = #{tender}")
    List<Long> findAllOrdersIdbyTender(Long tender);

    @Select("Select tender from orders where id = #{id}")
    Long findTenderIdbyId(Long id);

    @Select("Select " + orders_columns + " from orders where tender = #{tender} and product_category = #{product_category}")
    List<OrdersDB> findAllOrdersbyTenderAndProduct(Long tender, Long product_category);


    @Insert("insert into orders (comment, product,tender,number,price, frequency,usb,vxi,portable,channel,port,form_factor,purpose,voltage,current)" +
            " values (#{comment}, #{product},#{tender},#{number},#{price}, #{frequency},#{usb},#{vxi},#{portable},#{channel},#{port},#{form_factor},#{purpose},#{voltage},#{current})")
    Long insertOrder(OrdersDB ordersDB);

    @Update("Update orders set comment = #{comment}, product = #{product},tender =#{tender},number = #{number},price = #{price}, " +
            "frequency = #{frequency},usb = #{usb},vxi = #{vxi},portable = #{portable},channel = #{channel},port = #{port}, " +
            "form_factor = #{form_factor},purpose = #{purpose},voltage = #{voltage},current = #{current} where id = #{id}" )
    Long updateOrder(OrdersDB ordersDB);

    @Select("Select LAST_INSERT_ID()")
    Long checkId();

    @Update("update orders set comment = #{comment}, id_product = #{id_product},product_category = #{product_category},tender = #{tender},number = #{number},price = #{price},win_price = #{win_price}," +
            "frequency = #{frequency},usb = #{usb},vxi = #{vxi},portable = #{portable},channel = #{channel},port = #{port},form_factor = #{form_factor},purpose = #{purpose},voltage = #{voltage},current = #{current} where id = #{id}")
    void updateOrderLast(Long id, String comment, Long id_product, Long product_category, Long tender, int number, BigDecimal price, BigDecimal win_price,
                     Double frequency,Boolean usb,Boolean vxi,Boolean portable,Integer channel,Integer port,String form_factor,String purpose,Double voltage,Double current);

    @Update("update orders set product = #{product} where id = #{id}")
    void ChangeProduct(Long id, Long product);

    @Update("update orders set product = #{product} where id = #{id}")
    void ChangeProductFormat(Long id, Long product);

    @Update("update orders set comment = #{comment}, number = #{number} where id= #{id}")
    void UpdateAnotherProduct(String comment,int number, Long id);

    @Insert("insert into orders (comment, number,product, tender) values('(1 наименвоание)','1','1491',#{tender})")
    void InsertNewAnother(Long tender);

    @Update("update orders set id_product = #{id_product},product_category = #{product_category}, comment = #{comment}, frequency = #{frequency} where id = #{id}")
    void ChangeProductAndCommentAndFrequency(Long id, Long id_product, Long product_category, String comment, Double frequency);

    @Update("update orders set comment = #{comment}, id_product = #{id_product},product_category = #{product_category} where id = #{id}")
    void ChangeProductFromFile(Long id, Long id_product, Long product_category, String comment);

    @Select("Select id from orders where product = #{product} and product_category = #{product_category}")
    List<Long> findAllOrdersIdbyProduct(Long product);

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

    @Select("SELECT" +
            " distinct tender.id,if(position('объявляет тендер:' in name_tender) = 0, trim(name_tender), trim(substring(name_tender, position('объявляет тендер:' in name_tender)+17))) as name_tender, bico_tender,gos_zakupki,date_start, date_finish,date_tranding ,number_tender,  full_sum, win_sum, currency, tender.price, rate, sum, c.name as customer, c.inn as inn, type as typetender, w.name as winner, tender.product, dublicate, country.name as country" +
            " FROM keysight.tender left join customer c on c.id = tender.customer left join typetender t on t.id = tender.typetender left join winner w on w.id = tender.winner left join country on c.country = country.id WHERE NOT EXISTS (SELECT orders.ID FROM keysight.orders WHERE tender.ID = orders.tender)")
    List<Tender> findTenderWithoutOrdersForHome();

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

    @Select("Select comment.id,comment.text, comment.usr, usr.nickname as user, comment.date from comment left join usr on comment.usr = usr.id left join comment_for_user on comment.id = comment_for_user.comment where comment_for_user.usr = #{user} order by date DESC limit 4")
    List<Comment> findAllCommentsForUser(Long user);

    @Select("Select id from comment where tender =#{tender} and date = #{date}")
    Long GetCommentId(Long tender, ZonedDateTime date);

    @Insert("Insert into comment (text,usr,date,tender) values (#{text},#{usr},#{date},#{tender})")
    void insertComment(String text, Long usr, ZonedDateTime date, Long tender);

    @Insert("Insert into comment_for_user (comment,usr) values (#{comment},#{usr})")
    void InsertCommentForUser(Long comment, Long usr);

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

    @Select("Select id from product where product_category = #{product_category} and id_product = #{id_product} limit 1")
    Long CheckProductLong(Long product_category, Long id_product);

    @Select("Select id from product where product_category = #{product_category} and vendor_code = 'Без артикула' and subcategory is null")
    Long FindFirstProductInCategory(Long product_category);

    @Insert("Insert into product (id_product,product_category, subcategory, vendor_code, vendor, frequency,usb,vxi,portable,channel,port,form_factor,purpose,voltage,current)" +
            " values (#{id},#{product_category_id}, #{subcategory_id},trim(#{vendor_code}), #{vendor_id}, #{frequency},#{usb},#{vxi},#{portable},#{channel},#{port},#{form_factor},#{purpose},#{voltage},#{current})")
    void InsertIntoProduct(Product product);

    @Update("Update product set product_category=#{product_category_id}, subcategory= #{subcategory_id}, vendor_code=trim(#{vendor_code}), vendor=#{vendor_id}, frequency=#{frequency},usb=#{usb},vxi=#{vxi},portable=#{portable},channel=#{channel},port=#{port},form_factor=#{form_factor},purpose=#{purpose},voltage=#{voltage},current=#{current} where id = #{id}")
    void UpdateInProduct(Product product);

    @Delete("Delete from product where id = #{id} limit 1")
    void DeleteProduct(Long id);
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
    @Insert("Insert into search_parameters (nickname,name,dateStart,dateFinish,dublicate,typeExclude,type,customExclude,custom,innCustomer,country,winnerExclude,winner,minSum,maxSum,ids,bicotender,numberShow,product, region, district,plan_schedule,realized,adjacent_tender,private_search) values (#{nickname}, #{name},#{dateStart},#{dateFinish},#{dublicate},#{typeExclude},#{type},#{customExclude},#{custom},#{innCustomer},#{country},#{winnerExclude},#{winner},#{minSum},#{maxSum},#{ids},#{bicotender},#{numberShow},#{product},#{region},#{district},#{plan_schedule},#{realized},#{adjacent_tender},#{private_search})")
    void saveParameters(String nickname, String name,ZonedDateTime dateStart,ZonedDateTime dateFinish,Boolean dublicate,Boolean typeExclude, String type,Boolean customExclude,String custom,String innCustomer,Long country, Boolean winnerExclude, String winner,BigDecimal minSum, BigDecimal maxSum,String ids, String bicotender,Boolean numberShow,String product, String region,String district, Boolean plan_schedule,Boolean realized,Boolean adjacent_tender,Boolean private_search);
    @Update("Update search_parameters set nickname=#{nickname}, name=#{name}, dateStart=#{dateStart}, dateFinish=#{dateFinish}, dublicate = #{dublicate}, typeExclude = #{typeExclude}, type = #{type}, customExclude = #{customExclude}, custom = #{custom}, innCustomer = #{innCustomer}, country = #{country}, winnerExclude= #{winnerExclude}, winner = #{winner}, minSum = #{minSum}, maxSum = #{maxSum}, ids = #{ids}, bicotender = #{bicotender}, numberShow = #{numberShow}, product = #{product},region = #{region},district = #{district},plan_schedule = #{plan_schedule},realized = #{realized},adjacent_tender = #{adjacent_tender},private_search = #{private_search} where id = #{id}")
    void updateParameters(Long id,String nickname, String name,ZonedDateTime dateStart,ZonedDateTime dateFinish,Boolean dublicate,Boolean typeExclude, String type,Boolean customExclude,String custom,String innCustomer,Long country, Boolean winnerExclude, String winner,BigDecimal minSum, BigDecimal maxSum,String ids, String bicotender,Boolean numberShow,String product, String region,String district, Boolean plan_schedule,Boolean realized,Boolean adjacent_tender,Boolean private_search);
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

///////////////////////////////////////////////////////////
//              Tender_dublicate SQL
///////////////////////////////////////////////////////////
    @Select("Select id from tender_dublicate where tender = #{id} and tender_dublicate = #{id_d}")
    Long CheckDublicate(Long id, Long id_d);
    @Insert("Insert into tender_dublicate (tender,tender_dublicate) values (#{id},#{id_d})")
    void insertDublicate(Long id, Long id_d);
    @Delete("Delete from tender_dublicate where tender_dublicate = #{id}")
    void delete_tender_Dublicate(Long id);
    ///////////////////////////////////////////////////////////
//              Tender_plane SQL
///////////////////////////////////////////////////////////
    @Select("Select id from tender_plan where tender = #{id} and tender_plan = #{id_d}")
    Long CheckPlane(Long id, Long id_d);
    @Select("Select tender_plan from tender_plan")
    List<Long> AllIdPlan();
    @Insert("Insert into tender_plan (tender,tender_plan) values (#{id},#{id_d})")
    void insertPlane(Long id, Long id_d);
    @Delete("Delete from tender_plan where tender_plan = #{id}")
    void delete_tender_Plane(Long id);
    ///////////////////////////////////////////////////////////
//              EmailReport SQL
///////////////////////////////////////////////////////////
    @Select("${select}")
    EmailReport emailreport(String select);
    @Select("${select}")
    List<EmailReport> listEmailreport(String select);
    ///////////////////////////////////////////////////////////
//              Rate-RUB_USD SQL
///////////////////////////////////////////////////////////
@Insert("Insert into `rate-rub-usd` (date,usd) values (#{date},#{usd})")
    void InsertRate(ZonedDateTime date,double usd);


///////////////////////////////////////////////////////////
//              Another
///////////////////////////////////////////////////////////
    @Select("SELECT count(t.id) as value, v.name as name from tender as t" +
            "    left join orders o on t.id = o.tender" +
            "    left join product p on o.product = p.id" +
            "    left join vendor v on p.vendor= v.id" +
            " where date_start > #{date} and v.id  <> 1 and t.dublicate <> true group by v.id")
    List<NameValue> getTopDiagrammHome(ZonedDateTime date);

    @Select("SELECT count(t.id) as value, v.category as name from tender as t" +
            "    left join orders o on t.id = o.tender" +
            "    left join product p on o.product = p.id" +
            "    left join product_category v on p.vendor= v.id" +
            " where date_start > #{date} and v.id  <> 1 and t.dublicate <> true group by v.id")
    List<NameValue> getBottomDiagrammHome(ZonedDateTime date);
}