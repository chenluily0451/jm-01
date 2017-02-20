package com.jm.bid.user.finance.mapper;

import com.jm.bid.user.finance.dto.PaymentApplyDTO;
import com.jm.bid.user.finance.entity.PaymentApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaymentApplyMapper {


    int insertSelective(PaymentApply record);

    PaymentApply selectByPrimaryKey(@Param("id") Long id, @Param("companyId") String companyId);

    PaymentApply selectByTenderIdAndCompanyId(@Param("tenderId") Long tenderId, @Param("companyId") String companyId);

    int updateByPrimaryKeySelective(PaymentApply record);

    List<PaymentApplyDTO> loadAllPayApply(@Param("companyId") String companyId, @Param("done") boolean done);


}