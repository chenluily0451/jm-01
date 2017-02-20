package com.jm.bid.admin.tender.service;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.tender.dto.TenderNoticeDTO;
import com.jm.bid.admin.tender.mapper.TenderNoticeMapper;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.boot.validator.BeanValidators;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.entity.TenderNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Service
@Transactional(readOnly = false)
public class TenderNoticeService {

    private static final Logger logger = LoggerFactory.getLogger(TenderNoticeService.class);

    @Autowired
    private TenderService tenderService;

    @Autowired
    private TenderNoticeMapper tenderNoticeMapper;

    public TenderNotice loadNoticeById(long id) {
        return tenderNoticeMapper.loadById(id);
    }


    public Page<TenderNotice> findTenderNotice(TenderNoticeDTO tenderNoticeDTO) {
        return tenderNoticeMapper.findTenderNotice(tenderNoticeDTO);
    }

    @Transactional(readOnly = false)
    public void releaseNotice(TenderNotice tenderNotice, AdminDTO admin) {
        BeanValidators.validateWithException(tenderNotice);
        tenderNotice.setStatus(1);
        this.addTenderNotice(tenderNotice, admin);
    }

    private void addTenderNotice(TenderNotice tenderNotice, AdminDTO admin) {
        Tender tender = tenderService.loadTenderById(tenderNotice.getTenderId());
        if (tender == null) {
            throw new NotFoundException();
        } else {
            tenderNotice.setTenderTitle(tender.getTenderTitle());
            tenderNotice.setTenderCode(tender.getTenderCode());
            tenderNotice.setHasWinBid(tender.getStatus() > 4 ? true : false);
            tenderNotice.setCreateBy(admin.getSecurePhone());
            tenderNotice.setCreateDate(LocalDateTime.now());
            tenderNotice.setLastUpdateDate(LocalDateTime.now());
            if (tenderNoticeMapper.loadByTenderId(tender.getId()) != null) {
                throw new BusinessException("竞价公示已经发布,请不要重新发布!");
            } else {
                tenderNoticeMapper.insertSelective(tenderNotice);
            }
        }
    }


    @Transactional(readOnly = false)
    public void backOutNotice(long noticeId) {
        TenderNotice notice = tenderNoticeMapper.loadById(noticeId);
        if (notice == null) {
            throw new NotFoundException("竞标公示不存在!");
        }
        notice.setStatus(2);
        tenderNoticeMapper.updateByPrimaryKeySelective(notice);
    }
}
