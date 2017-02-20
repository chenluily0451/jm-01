package com.jm.bid.job;

import com.jm.bid.admin.finance.entity.JMSelfBankCard;
import com.jm.bid.admin.finance.service.JMSelfBankCardService;
import com.jm.bid.user.finance.entity.BankCard;
import com.jm.bid.user.finance.service.BankCardService;
import com.jm.bid.user.finance.service.CashAccountService;
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
public class CashAccountJob {

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private JMSelfBankCardService adminBankCardService;

    //验证资金账号
    @Scheduled(fixedRate = 3000)
    public void validateCashAccount() {
        cashAccountService.verifyBankCashAccount();
    }


    //转到到银行卡,验证有效性
    @Scheduled(fixedRate = 5000)
    public void transferToCard() {
        bankCardService.loadAllNoTransferCards().parallelStream().forEach(b -> {
            bankCardService.validateCardTransfer(b);
        });

        //校验晋煤
        adminBankCardService.loadAllNoTransferCards().parallelStream().forEach(b -> {
            adminBankCardService.validateCardTransfer(b);
        });
    }

    // 验证银行卡有效性
    @Scheduled(fixedRate = 1000 * 20)
    public void updateBankAvailability() {
        bankCardService.loadAllCardsByStatus(BankCard.Status.processing).parallelStream().forEach(b -> {
            bankCardService.updateBankAvailability(b);
        });

        //校验晋煤
        adminBankCardService.loadAllCardsByStatus(JMSelfBankCard.Status.processing).parallelStream().forEach(b -> {
            adminBankCardService.updateBankAvailability(b);
        });
    }
}
