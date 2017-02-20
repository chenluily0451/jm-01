package com.jm.bid.user.bid.mapper;

import com.jm.bid.user.bid.entity.BidSubscribe;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BidSubscribeMapper {

    int insert(BidSubscribe record);

    List<BidSubscribe> selectByTenderId(@Param("tenderId") long id);

   BidSubscribe selectByTenderIdAndPhone(@Param("tenderId") long id,@Param("phone") String phone);

    int updateByPrimaryKey(BidSubscribe record);

}