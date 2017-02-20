package com.jm.bid.job;

import com.jm.bid.admin.finance.service.JMSelfWithDrawRecordService;
import com.jm.bid.admin.finance.service.RefundRecordService;
import com.jm.bid.user.finance.service.PayRecordService;
import com.jm.bid.user.finance.service.WithDrawRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by xiangyang on 2016/12/13.
 */
@Service
@EnableScheduling
@Profile("schedule")
public class TradingJob {

    @Autowired
    private PayRecordService payRecordService;

    @Autowired
    private WithDrawRecordService withDrawRecordService;

    @Autowired
    private JMSelfWithDrawRecordService jmSelfWithDrawRecordService;

    @Autowired
    private RefundRecordService refundRecordService;

    //更新交易记录
    @Scheduled(fixedRate = 1000 * 10)
    public void refreshTradeRecord() {
        //修改保证金支付记录
        payRecordService.updatePayRecord();
    }

    // 更新提现(2小时间隔更新提现记录)
    @Scheduled(fixedRate = 3600000 * 2)
    public void withDraw() {
        withDrawRecordService.updateWithDrawRecord();
        jmSelfWithDrawRecordService.updateWithDrawRecord();
    }

    //向银行发起退款
    @Scheduled(fixedRate = 2000 * 10)
    public void doRefundDeposit() {
        refundRecordService.refundDeposit();
    }

    //更新退款交易记录
    @Scheduled(fixedRate = 5000 * 10)
    public void refreshRefundRecord() {
        refundRecordService.refreshRefundDepositStatus();
    }

}
