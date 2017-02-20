package com.jm.bid.admin.tender.mapper;

import com.jm.bid.admin.tender.dto.TenderNoticeDTO;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.common.entity.TenderNotice;
import org.apache.ibatis.annotations.Param;


/**
 * Created by xiangyang on 16/9/4
 */
public interface TenderNoticeMapper {

    void insertSelective(TenderNotice tenderNotice);

    void updateByPrimaryKeySelective(TenderNotice tenderNotice);

    TenderNotice loadById(@Param("id") long id);

    TenderNotice loadByTenderId(@Param("tenderId") long id);

    Page<TenderNotice> findTenderNotice(TenderNoticeDTO tenderNoticeDTO);




}
