package com.jm.bid.user.finance.mapper;


import com.jm.bid.user.finance.entity.BankCashAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface BankCashAccountMapper {

    int insertSelective(BankCashAccount record);

    BankCashAccount selectByAccId(@Param("accId") String accId, @Param("accountType") int accountType);

    int updateByPrimaryKeySelective(BankCashAccount record);

    List<BankCashAccount> loadBankAccountsByStatus(Boolean available);


}