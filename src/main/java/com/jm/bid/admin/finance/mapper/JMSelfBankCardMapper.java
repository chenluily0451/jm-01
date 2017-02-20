package com.jm.bid.admin.finance.mapper;


import com.jm.bid.admin.finance.entity.JMSelfBankCard;

import java.util.List;

public interface JMSelfBankCardMapper {


    int insertSelective(JMSelfBankCard record);

    JMSelfBankCard loadJMSelfBankCard();

    List<JMSelfBankCard> loadAllNoTransferCards();

    List<JMSelfBankCard> loadAllCardsByStatus(int status);

    JMSelfBankCard loadById(long id);

    int updateByPrimaryKeySelective(JMSelfBankCard record);


}