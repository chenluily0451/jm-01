package com.jm.bid.user.finance.mapper;

import com.jm.bid.boot.persistence.Page;
import com.jm.bid.common.dto.TradeRecordDTO;
import com.jm.bid.user.finance.entity.PayRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayRecordMapper {


    int insertSelective(PayRecord record);

    PayRecord selectByTradeId(String tradeId);

    PayRecord selectByTypeAndCompanyId(@Param("tenderId") Long tenderId, @Param("type") int type, @Param("payCompanyId") String companyId);

    List<PayRecord> selectByTypeAndTenderId(@Param("tenderId") Long tenderId, @Param("type") int type,@Param("status") int status);

    int updateByPrimaryKeySelective(PayRecord record);

    Page<PayRecord> findAllPayRecord(TradeRecordDTO tradeRecordDTO);

    List<PayRecord> loadAllTradeByStatus(@Param("status") int... status);

}