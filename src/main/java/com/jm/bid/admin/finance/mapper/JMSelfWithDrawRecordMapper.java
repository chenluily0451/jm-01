package com.jm.bid.admin.finance.mapper;

import com.jm.bid.admin.finance.entity.JMSelfWithDrawRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface JMSelfWithDrawRecordMapper {


    int insertSelective(JMSelfWithDrawRecord record);

    JMSelfWithDrawRecord selectByTradeId(String tradeId);

    List<JMSelfWithDrawRecord> loadWithDrawByStatus(@Param("status") int... status);

    int updateByPrimaryKeySelective(JMSelfWithDrawRecord record);

}