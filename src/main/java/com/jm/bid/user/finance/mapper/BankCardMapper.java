package com.jm.bid.user.finance.mapper;

import com.jm.bid.user.finance.entity.BankCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BankCardMapper {
    int insertSelective(BankCard record);

    BankCard selectByCompanyId(String companyId);

    BankCard selectByIdAndCompanyId(@Param("id") long id, @Param("companyId") String companyId);

    List<BankCard> loadAllNoTransferCards();

    List<BankCard> loadAllCardsByStatus(int status);

    int updateByPrimaryKeySelective(BankCard record);


}