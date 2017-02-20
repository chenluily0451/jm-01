package com.jm.bid.user.finance.service;

import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.entity.BankSite;
import com.jm.bid.common.mapper.BankSiteMapper;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.TransferDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.finance.entity.BankCard;
import com.jm.bid.user.finance.mapper.BankCardMapper;
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
public class BankCardService {

    private static final Logger logger = LoggerFactory.getLogger(BankCardService.class);

    @Autowired
    private BankSiteMapper bankSiteMapper;

    @Autowired
    private BankCardMapper bankCardMapper;


    @Autowired
    private RestPayClient restPayClient;

    //公共付费账户配置,用来校验银行卡
    @Value("${pg.pay.accId}")
    private String payAccId;

    @Value("${pg.pay.accNo}")
    private String payAccNo;

    @Value("${pg.pay.accNm}")
    private String payAccNm;


    public List<BankCard> loadAllNoTransferCards() {
        return bankCardMapper.loadAllNoTransferCards();
    }

    public List<BankCard> loadAllCardsByStatus(BankCard.Status status) {
        return bankCardMapper.loadAllCardsByStatus(status.value);
    }

    public BankCard loadCardByCompanyId(String companyId) {
        return bankCardMapper.selectByCompanyId(companyId);
    }

    public BankCard selectByIdAndCompanyId(long bankId, String companyId) {
        return bankCardMapper.selectByIdAndCompanyId(bankId, companyId);
    }

    @Transactional(readOnly = false)
    public void addBindCard(BankCard bankCard, UserDTO userDTO) {

        BankSite bankSite = bankSiteMapper.loadChildBank(bankCard.getChildBankCode());
        if (bankSite == null) {
            throw new BusinessException("所选开户行不存在,请选择正确的开户行信息!");
        }

        bankCard.setChildBankName(bankSite.getChildBankName());
        bankCard.setCreateDate(LocalDateTime.now());
        bankCard.setCreateBy(String.valueOf(userDTO.getId()));
        bankCard.setCompanyId(userDTO.getCompanyId());
        bankCard.setStatus(BankCard.Status.init.value);
        bankCardMapper.insertSelective(bankCard);
    }

    @Transactional(readOnly = false)
    public void deleteBankCard(long bankCardId, UserDTO userDTO) {
        BankCard bankCard = bankCardMapper.selectByIdAndCompanyId(bankCardId, userDTO.getCompanyId());
        if (bankCard == null) {
            throw new NotFoundException();
        }
        bankCard.setDeleted(true);
        bankCardMapper.updateByPrimaryKeySelective(bankCard);
    }


    @Transactional(readOnly = false)
    public void validateCardTransfer(BankCard bankCard) {
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
        transferDTO.setMemo("校验银行卡有效性");

        TransferStatusDTO statusDTO = restPayClient.withdraw(transferDTO);
        if (statusDTO.isProcessed()) {
            bankCard.setValidateAmount(validateAmount);
            bankCard.setTradeId(clientId);
            bankCard.setStatus(BankCard.Status.processing.value);
            bankCardMapper.updateByPrimaryKeySelective(bankCard);
        }


    }

    @Transactional(readOnly = false)
    public void updateBankCard(BankCard bankCard) {
        Assert.notNull(bankCard);
        bankCardMapper.updateByPrimaryKeySelective(bankCard);
    }

    @Transactional(readOnly = false)
    public void updateBankAvailability(BankCard bankCard) {
        Assert.notNull(bankCard);
        TransferStatusDTO statusDTO = restPayClient.queryTransferStatus(bankCard.getTradeId());
        if (statusDTO.isSuccess()) {
            bankCard.setStatus(BankCard.Status.success.value);
            bankCardMapper.updateByPrimaryKeySelective(bankCard);
        } else if (statusDTO.isFail()) {
            bankCard.setStatus(BankCard.Status.fail.value);
            bankCard.setBankMsg(statusDTO.getMessage());
            bankCardMapper.updateByPrimaryKeySelective(bankCard);
        } else {
            logger.error("查询绑定银行卡交易状态失败:" + statusDTO.getMessage());
        }
    }


    //加载开户行字典
    public List<BankSite> loadAllBank() {
        return bankSiteMapper.loadAllBank();
    }

    public List<BankSite> loadBankSiteProvinces() {
        return bankSiteMapper.loadBankSiteProvinces();
    }

    public List<BankSite> loadBankSiteCities(int provinceId) {
        return bankSiteMapper.loadBankSiteCitys(provinceId);
    }


    public List<BankSite> loadChildBanksByCityCodeBankCode(Integer cityCode, Integer bankCode) {
        return bankSiteMapper.loadChildBanksByCityCodeBankCode(cityCode, bankCode);
    }

    public BankSite loadChildBank(String childBankCode) {
        return bankSiteMapper.loadChildBank(childBankCode);
    }


}
