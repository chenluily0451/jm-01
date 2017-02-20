package com.jm.bid.common.mapper;

import com.jm.bid.common.entity.BankSite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by xiangyang on 16/5/16.
 */
public interface BankSiteMapper {

    //加载所有开户行
    @Select("select  bankName, bankCode from jm_bankSite where bankName is not null group by bankName order by bankCode")
    List<BankSite> loadAllBank();

    //加载所有开户行省份
    @Select("SELECT  provinceName,provinceCode FROM jm_bankSite group by provinceName order by provinceCode")
    List<BankSite> loadBankSiteProvinces();

    //加载银行网点市区
    @Select("SELECT cityName,cityCode FROM jm_bankSite where provinceCode=#{value} group by cityName order by cityCode ")
    List<BankSite> loadBankSiteCitys(int provinceId);

    //加载支行
    @Select("select childBankCode,childBankName from jm_bankSite where cityCode=#{cityCode} and bankCode=#{bankCode} and  childBankName like '%' #{childBankName} '%' ")
    List<BankSite> loadChildBanks(@Param("cityCode") Integer cityCode, @Param("bankCode") Integer bankCode, @Param("childBankName") String childBankName);

    //加载支行
    @Select("select childBankCode,childBankName from jm_bankSite where cityCode=#{cityCode} and bankCode=#{bankCode} ")
    List<BankSite> loadChildBanksByCityCodeBankCode(@Param("cityCode") Integer cityCode, @Param("bankCode") Integer bankCode);

    //加载支行
    @Select("select * from jm_bankSite where  childBankCode=#{value} ")
    BankSite loadChildBank(String childBankCode);


}
