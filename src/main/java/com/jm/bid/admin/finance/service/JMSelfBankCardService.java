package com.jm.bid.admin.finance.service;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.finance.entity.JMSelfBankCard;
import com.jm.bid.admin.finance.mapper.JMSelfBankCardMapper;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.entity.BankSite;
import com.jm.bid.common.mapper.BankSiteMapper;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.TransferDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by xiangyang on 2016/12/14.
 */
@Service
public class JMSelfBankCardService {

    private static final Logger logger = LoggerFactory.getLogger(JMSelfBankCardService.class);

    @Autowired
    private BankSiteMapper bankSiteMapper;

    @Autowired
    private JMSelfBankCardMapper jmSelfBankCardMapper;

    @Autowired
    private RestPayClient restPayClient;

    //公共付费账户配置,用来校验银行卡
    @Value("${pg.pay.accId}")
    private String payAccId;

    @Value("${pg.pay.accNo}")
    private String payAccNo;

    @Value("${pg.pay.accNm}")
    private String payAccNm;


    public List<JMSelfBankCard> loadAllNoTransferCards() {
        return jmSelfBankCardMapper.loadAllNoTransferCards();
    }

    public List<JMSelfBankCard> loadAllCardsByStatus(JMSelfBankCard.Status status) {
        return jmSelfBankCardMapper.loadAllCardsByStatus(status.value);
    }

    public JMSelfBankCard loadJMSelfBankCard() {
        return jmSelfBankCardMapper.loadJMSelfBankCard();
    }

    public JMSelfBankCard loadById(long id) {
        return jmSelfBankCardMapper.loadById(id);

    }

    public void addBindCard(JMSelfBankCard bankCard, AdminDTO adminDTO) {

        BankSite bankSite = bankSiteMapper.loadChildBank(bankCard.getChildBankCode());
        if (bankSite == null) {
            throw new BusinessException("所选开户行不存在,请选择正确的开户行信息!");
        }

        bankCard.setChildBankName(bankSite.getChildBankName());
        bankCard.setCreateDate(LocalDateTime.now());
        bankCard.setCreateBy(String.valueOf(adminDTO.getId()));
        bankCard.setStatus(JMSelfBankCard.Status.init.value);
        jmSelfBankCardMapper.insertSelective(bankCard);
    }

    @Transactional(readOnly = false)
    public void deleteBankCard(long bankCardId) {
        JMSelfBankCard bankCard = jmSelfBankCardMapper.loadById(bankCardId);
        if (bankCard == null) {
            throw new NotFoundException();
        }
        bankCard.setDeleted(true);
        jmSelfBankCardMapper.updateByPrimaryKeySelective(bankCard);
    }


    @Transactional(readOnly = false)
    public void validateCardTransfer(JMSelfBankCard bankCard) {
        BankSite bankSite = bankSiteMapper.loadChildBank(bankCard.getChildBankCode());
        String clientId = Ids.generateZXClientId();
        BigDecimal validateAmount = new BigDecimal("0.12");

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSrcAccId(payAccId);
        transferDTO.setSrcAccNo(payAccNo);
        transferDTO.setSrcAccNm(payAccNm);
        transferDTO.setAmount(validateAmount);

        transferDTO.setTargetAccNo(bankCard.getCardNum());
        transferDTO.setTargetAccNm(bankCard.getCardAccountName());
        transferDTO.setTargetBankName(bankSite.getBankName());
        transferDTO.setTargetSubBranchName(bankSite.getChildBankName());
        transferDTO.setTargetZFLHH(bankCard.getChildBankCode());
        transferDTO.setSameBank(bankCard.getCardNum().startsWith("302") ? 0 : 1);
        transferDTO.setBankId(Consts.BankType.ZXBank.value);
        transferDTO.setClientId(clientId);
        transferDTO.setAmount(validateAmount);
        transferDTO.setOperator("jm_system");
        transferDTO.setMemo("validate");

        TransferStatusDTO statusDTO = restPayClient.withdraw(transferDTO);
        if (statusDTO.isProcessed()) {
            bankCard.setValidateAmount(validateAmount);
            bankCard.setTradeId(clientId);
            bankCard.setStatus(JMSelfBankCard.Status.processing.value);
            jmSelfBankCardMapper.updateByPrimaryKeySelective(bankCard);
        }


    }

    @Transactional(readOnly = false)
    public void updateBankCard(JMSelfBankCard bankCard) {
        Assert.notNull(bankCard);
        jmSelfBankCardMapper.updateByPrimaryKeySelective(bankCard);
    }

    @Transactional(readOnly = false)
    public void updateBankAvailability(JMSelfBankCard bankCard) {
        Assert.notNull(bankCard);
        TransferStatusDTO statusDTO = restPayClient.queryTransferStatus(bankCard.getTradeId());
        if (statusDTO.isSuccess()) {
            bankCard.setStatus(JMSelfBankCard.Status.success.value);
            jmSelfBankCardMapper.updateByPrimaryKeySelective(bankCard);
        } else if (statusDTO.isFail()) {
            bankCard.setStatus(JMSelfBankCard.Status.fail.value);
            jmSelfBankCardMapper.updateByPrimaryKeySelective(bankCard);
        } else {
            logger.error("查询交易状态失败:" + statusDTO.getMessage());
        }
    }


}
