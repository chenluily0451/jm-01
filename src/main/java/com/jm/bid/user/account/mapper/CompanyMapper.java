package com.jm.bid.user.account.mapper;


import com.jm.bid.user.account.entity.Company;
import org.apache.ibatis.annotations.Param;

public interface CompanyMapper {

    int addSelective(Company company);

    Company loadByUUID(String UUID);

    boolean checkCompanyNameExists(String companyName);

    boolean checkCompanyNameExists2(@Param("UUID") String UUID, @Param("companyName") String companyName);




}