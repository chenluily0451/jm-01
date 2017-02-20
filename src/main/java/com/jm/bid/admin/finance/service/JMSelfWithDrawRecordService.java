package com.jm.bid.admin.finance.service;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.finance.entity.JMSelfBankCard;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.entity.JMSelfWithDrawRecord;
import com.jm.bid.admin.finance.mapper.JMSelfWithDrawRecordMapper;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.entity.BankSite;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.TransferDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.finance.entity.WithDrawRecord;
import com.jm.bid.user.finance.service.BankCardService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Service
@Transactional(readOnly = false)
public class JMSelfWithDrawRecordService {


    private static final Logger logger = LoggerFactory.getLogger(JMSelfWithDrawRecordService.class);

    @Autowired
    private JMSelfBankCardService jmSelfBankCardService;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    @Autowired
    private JMSelfWithDrawRecordMapper jmSelfWithDrawRecordMapper;

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private BankCardService bankCardService;

    @Transactional(readOnly = false, noRollbackFor = {HttpServerErrorException.class})
    public void withDraw(BigDecimal amount, long bankCardId, String remark, AdminDTO adminDTO) throws NoHasCashAccountException {

        JMSelfBankCashAccount cashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);

        JMSelfBankCard bankCard = jmSelfBankCardService.loadJMSelfBankCard();


        String clientId = Ids.generateZXClientId();

        JMSelfWithDrawRecord withDraw = new JMSelfWithDrawRecord();

        withDraw.setCreateDate(LocalDateTime.now());
        withDraw.setAmount(amount);
        withDraw.setBankCardId(bankCardId);
        withDraw.setCreateBy(String.valueOf(adminDTO.getId()));
        withDraw.setDeleted(false);
        withDraw.setStatus(WithDrawRecord.WithDrawStatus.init.value);
        withDraw.setRemark(remark);
        withDraw.setTradeId(clientId);

        jmSelfWithDrawRecordMapper.insertSelective(withDraw);


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
        transferDTO.setMemo(StringUtils.trimToNull(remark));
        restPayClient.withdraw(transferDTO);

    }

    @Transactional(readOnly = false)
    public void updateWithDrawRecord() {
        jmSelfWithDrawRecordMapper.loadWithDrawByStatus(WithDrawRecord.WithDrawStatus.init.value,
                WithDrawRecord.WithDrawStatus.processing.value)
                .parallelStream()
                .forEach(r -> {
                    try {
                        TransferStatusDTO statusDTO = restPayClient.queryTransferStatus(r.getTradeId());
                        if (statusDTO.isProcessed() && r.getStatus() == JMSelfWithDrawRecord.WithDrawStatus.init.value) {
                            r.setStatus(JMSelfWithDrawRecord.WithDrawStatus.processing.value);
                            jmSelfWithDrawRecordMapper.updateByPrimaryKeySelective(r);
                        } else if (statusDTO.isSuccess()) {
                            r.setStatus(JMSelfWithDrawRecord.WithDrawStatus.success.value);
                            jmSelfWithDrawRecordMapper.updateByPrimaryKeySelective(r);
                        } else if (statusDTO.isFail()) {
                            r.setBankMsg(statusDTO.getMessage());
                            r.setStatus(JMSelfWithDrawRecord.WithDrawStatus.fail.value);
                            jmSelfWithDrawRecordMapper.updateByPrimaryKeySelective(r);
                        }
                    } catch (Exception e) {
                        logger.error("银行网络异常...", e);
                    }
                });

    }

}
