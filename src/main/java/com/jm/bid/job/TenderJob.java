package com.jm.bid.job;

import com.jm.bid.admin.tender.mapper.TenderMapper;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.user.bid.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;

/**
 * Created by xiangyang on 2016/12/13.
 */
@Service
@EnableScheduling
@Profile("schedule")
public class TenderJob {

    @Autowired
    private TenderMapper tenderMapper;

    @Autowired
    private BidService bidService;

    //修改招标开始
    @Scheduled(fixedRate = 3000)
    public void setUpTenderStart() {
        List<Tender> allYuGaoTender = tenderMapper.loadAllTender(2);

        //发送预约短信
        allYuGaoTender.parallelStream().filter(t -> t.getTenderStartDate().truncatedTo(ChronoUnit.MINUTES).minusHours(2).isEqual(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
                .forEach(t -> bidService.noticeSubscribe(t.getId()));

        //修改预告开始
        allYuGaoTender.parallelStream()
                .filter(t -> t.getStatus() == 2 && LocalDateTime.now().isAfter(t.getTenderStartDate()))
                .forEach(t -> {
                    Tender tender = new Tender();
                    tender.setStatus(3);
                    tender.setId(t.getId());
                    tenderMapper.updateByPrimaryKeySelective(tender);
                });

        //竞价结束,修改应标
        tenderMapper.loadAllTender(3).parallelStream()
                .filter(t -> t.getStatus() == 3 && LocalDateTime.now().isAfter(t.getTenderEndDate()))
                .forEach(t -> {
                    bidService.autoAnswerBid(t.getId());
                });

    }


}
