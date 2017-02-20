package com.jm.bid.user.finance.mapper;


import com.jm.bid.user.finance.entity.SystemCashAccount;

public interface SystemCashAccountMapper {


    int insertSelective(SystemCashAccount record);

    SystemCashAccount selectByCompanyId(String id);

    SystemCashAccount selectByAccId(String id);

    int updateByPrimaryKeySelective(SystemCashAccount record);


}