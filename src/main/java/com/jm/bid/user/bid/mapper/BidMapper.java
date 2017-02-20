package com.jm.bid.user.bid.mapper;

import com.jm.bid.boot.persistence.Page;
import com.jm.bid.user.bid.dto.BidDTO;
import com.jm.bid.user.bid.entity.Bid;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BidMapper {

    int insertSelective(Bid record);

    Bid selectByPrimaryKey(Long id);

    BigDecimal selectMaxPriceByTenderId(Long id);

    List<BidDTO> selectByTenderId(Long id);

    int updateByPrimaryKeySelective(Bid record);

    Page<BidDTO> findCompanyBid(BidDTO bidDTO);

    BidDTO selectByCompanyIdAndTenderId(@Param("tenderId") long tenderId, @Param("companyId") String companyId);

    BigDecimal selectTotalSaleMoney();

    Long selectTotalSaleAmount();

    List<Bid> selectNoBidCompany(long tenderId);

}