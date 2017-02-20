package com.jm.bid.user.finance.mapper;

import com.jm.bid.user.finance.entity.WithDrawRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WithDrawRecordMapper {


    int insertSelective(WithDrawRecord record);

    WithDrawRecord selectByTradeId(String tradeId);

    List<WithDrawRecord> loadWithDrawByStatus(@Param("status") int... status);

    int updateByPrimaryKeySelective(WithDrawRecord record);

}