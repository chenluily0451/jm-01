package com.jm.bid.admin.tender.mapper;


import com.jm.bid.boot.persistence.Page;
import com.jm.bid.common.dto.TenderParamDTO;
import com.jm.bid.common.entity.Tender;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TenderMapper {

    int insertSelective(Tender record);

    Tender selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Tender record);

    boolean isExistsTenderTitle(String tenderTitle);

    Boolean isExistsTenderTitleExclude(@Param("tenderTitle") String tenderTitle, @Param("id") long id);

    Page<Tender> findAllTender(TenderParamDTO paramDTO);

    Page<Tender> findAllTenderInProcessing(TenderParamDTO paramDTO);

    List<Tender> loadAllTender(@Param("status") Integer... status);

    List<Tender> loadWaitReleaseNotice();

    int updateVisitorsCount(long tenderId);

}