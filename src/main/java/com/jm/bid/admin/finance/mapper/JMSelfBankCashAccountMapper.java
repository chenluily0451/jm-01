package com.jm.bid.admin.finance.mapper;


import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface JMSelfBankCashAccountMapper {


    int insertSelective(JMSelfBankCashAccount record);

    JMSelfBankCashAccount selectByAccountTypeAndBankType(@Param("accountType") int type, @Param("bankType") int bankType);

    List<JMSelfBankCashAccount> loadBankAccountsByStatus(boolean available);

    int updateByPrimaryKeySelective(JMSelfBankCashAccount record);

}