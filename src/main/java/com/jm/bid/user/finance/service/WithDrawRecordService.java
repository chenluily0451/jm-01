package com.jm.bid.user.finance.service;

import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.entity.BankSite;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.TransferDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.finance.entity.BankCard;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.entity.WithDrawRecord;
import com.jm.bid.user.finance.mapper.WithDrawRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Service
@Transactional(readOnly = false)
public class WithDrawRecordService {


    private static final Logger logger = LoggerFactory.getLogger(WithDrawRecordService.class);

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private WithDrawRecordMapper withDrawRecordMapper;

    @Autowired
    private RestPayClient restPayClient;


    @Transactional(readOnly = false, noRollbackFor = {HttpClientErrorException.class, HttpServerErrorException.class, ResourceAccessException.class})
    public void withDraw(BigDecimal amount, long bankCardId, String remark, UserDTO userDTO) throws NoHasCashAccountException {

        BankCashAccount cashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        BankCard bankCard = bankCardService.selectByIdAndCompanyId(bankCardId, userDTO.getCompanyId());


        String clientId = Ids.generateZXClientId();

        WithDrawRecord withDraw = new WithDrawRecord();

        withDraw.setCompanyId(userDTO.getCompanyId());
        withDraw.setCreateDate(LocalDateTime.now());
        withDraw.setAmount(amount);
        withDraw.setBankCardId(bankCardId);
        withDraw.setCreateBy(String.valueOf(userDTO.getId()));
        withDraw.setDeleted(false);
        withDraw.setStatus(WithDrawRecord.WithDrawStatus.init.value);
        withDraw.setRemark(remark);
        withDraw.setTradeId(clientId);

        withDrawRecordMapper.insertSelective(withDraw);


        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSrcAccId(cashAccount.getAccId());
        transferDTO.setSrcAccNo(cashAccount.getBankAccountNum());
        transferDTO.setSrcAccNm(cashAccount.getBankAccountName());
        transferDTO.setAmount(amount);

        transferDTO.setTargetAccNo(bankCard.getCardNum());
        transferDTO.setTargetAccNm(bankCard.getCardAccountName());

        BankSite bankSite = bankCardService.loadChildBank(bankCard.getChildBankCode());

        transferDTO.setTargetBankName(bankSite.getBankName());

        transferDTO.setTargetSubBranchName(bankCard.getChildBankName());
        transferDTO.setTargetZFLHH(bankCard.getChildBankCode());
        transferDTO.setSameBank(bankCard.getCardNum().startsWith("302") ? 0 : 1);
        transferDTO.setBankId(Consts.BankType.ZXBank.value);
        transferDTO.setClientId(clientId);
        transferDTO.setOperator("jm_system");
        transferDTO.setMemo(remark);
        restPayClient.withdraw(transferDTO);

    }

    @Transactional(readOnly = false)
    public void updateWithDrawRecord() {
        withDrawRecordMapper.loadWithDrawByStatus(WithDrawRecord.WithDrawStatus.init.value,
                WithDrawRecord.WithDrawStatus.processing.value)
                .parallelStream()
                .forEach(r -> {
                    try {
                        TransferStatusDTO statusDTO = restPayClient.queryTransferStatus(r.getTradeId());
                        if (statusDTO.isProcessed() && r.getStatus() == WithDrawRecord.WithDrawStatus.init.value) {
                            r.setStatus(WithDrawRecord.WithDrawStatus.processing.value);
                            withDrawRecordMapper.updateByPrimaryKeySelective(r);
                        } else if (statusDTO.isSuccess()) {
                            r.setStatus(WithDrawRecord.WithDrawStatus.success.value);
                            withDrawRecordMapper.updateByPrimaryKeySelective(r);
                        } else if (statusDTO.isFail()) {
                            r.setBankMsg(statusDTO.getMessage());
                            r.setStatus(WithDrawRecord.WithDrawStatus.fail.value);
                            withDrawRecordMapper.updateByPrimaryKeySelective(r);
                        }
                    } catch (Exception e) {
                        logger.error("银行网络异常...", e);
                    }
                });

    }

}
