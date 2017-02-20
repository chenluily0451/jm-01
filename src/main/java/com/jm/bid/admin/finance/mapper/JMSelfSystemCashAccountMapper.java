package com.jm.bid.admin.finance.mapper;


import com.jm.bid.admin.finance.entity.JMSelfSystemCashAccount;

public interface JMSelfSystemCashAccountMapper {


    int insertSelective(JMSelfSystemCashAccount record);

    JMSelfSystemCashAccount selectOne();


}