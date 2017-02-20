package com.jm.bid.admin.finance.mapper;

import com.jm.bid.admin.finance.entity.RefundRecord;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.common.dto.TradeRecordDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RefundRecordMapper {


    int insertSelective(RefundRecord record);

    RefundRecord selectByPrimaryKey(long id);

    int updateByPrimaryKeySelective(RefundRecord record);

    List<RefundRecord> selectByStatus(@Param("status") int... status);

    List<RefundRecord> selectByRefundTypeAndStatus(@Param("auto") boolean auto, @Param("status") int status);

    Page<RefundRecord> findAllRefundRecord(TradeRecordDTO tradeRecordDTO);

    Page<RefundRecord> findJMAllRefundRecord(TradeRecordDTO tradeRecordDTO);

}