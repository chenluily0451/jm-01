package com.jm.bid.admin.finance.service;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.entity.JMSelfSystemCashAccount;
import com.jm.bid.admin.finance.mapper.JMSelfBankCashAccountMapper;
import com.jm.bid.admin.finance.mapper.JMSelfSystemCashAccountMapper;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.CashAccountDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/12.
 */
@Service
@Transactional(readOnly = true)
public class JMSelfCashAccountService {

    private static final Logger logger = LoggerFactory.getLogger(JMSelfCashAccountService.class);

    public static final String system_cashAccountName = "山西晋城无烟煤矿业集团有限责任公司";

    public static final String system_depositAccountName = "山西晋城无烟煤矿业集团有限责任公司保证金账户";

    /*晋煤默认idValue*/
    @Value("${pg.jm.idValue}")
    private String idValue;

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private JMSelfSystemCashAccountMapper jmSelfSystemCashAccountMapper;

    @Autowired
    private JMSelfBankCashAccountMapper jmSelfBankCashAccountMapper;


    /**
     * 开通系统资金账户
     *
     * @param adminDTO
     * @return
     */
    @Transactional(readOnly = false)
    public JMSelfSystemCashAccount openSystemCashAccount(AdminDTO adminDTO) {
        JMSelfSystemCashAccount jmSystemAccount = this.loadJMSystemAccount();
        if (jmSystemAccount == null) {
            CashAccountDTO cashAccountDTO = restPayClient.createAccount(idValue);
            if (cashAccountDTO.isProcessed() || cashAccountDTO.isSuccess()) {
                jmSystemAccount = new JMSelfSystemCashAccount();
                jmSystemAccount.setAccId(cashAccountDTO.getAccId());
                jmSystemAccount.setCreateDate(LocalDateTime.now());
                jmSystemAccount.setCreateBy(adminDTO.getSecurePhone());
                jmSelfSystemCashAccountMapper.insertSelective(jmSystemAccount);
                return jmSystemAccount;
            } else {
                logger.error("aegis-pg admin openSystemCashAccount:{}", cashAccountDTO);
            }
        }
        return jmSystemAccount;

    }


    @Transactional(readOnly = false)
    public CashAccountDTO openJMCashAccount(String accountName, String accId, JMSelfBankCashAccount.JMCashAccountType accountType, Consts.BankType bankType, AdminDTO adminDTO) {
        //开通保证金资金账户
        CashAccountDTO cashAccountDTO = restPayClient.createZXBankAccount(accountName, accId);
        if (cashAccountDTO.isProcessed() || cashAccountDTO.isSuccess()) {
            JMSelfBankCashAccount cashAccount = new JMSelfBankCashAccount();
            cashAccount.setAccId(accId);
            cashAccount.setBankAccountName(accountName);
            cashAccount.setBankAccountNum(cashAccountDTO.getAccNo());
            cashAccount.setCreateDate(LocalDateTime.now());
            cashAccount.setCreateBy(adminDTO.getSecurePhone());
            cashAccount.setBankId(bankType.value);
            cashAccount.setAccountType(accountType.value);
            cashAccount.setAvailable(false);
            jmSelfBankCashAccountMapper.insertSelective(cashAccount);
        } else {
            logger.error("aegis-pg openZXBankAccount:{}", cashAccountDTO.getErrMess());
        }
        return cashAccountDTO;
    }


    public JMSelfSystemCashAccount loadJMSystemAccount() {
        return jmSelfSystemCashAccountMapper.selectOne();
    }


    /**
     * @param accountType 资金账户类型
     * @param bankType    银行类型
     * @return 返回晋煤中信交易资金账户
     * @throws NoHasCashAccountException 没有资金账户抛异常,异常解析器会自动重定向到开通资金账户页面
     */
    @Transactional(readOnly = false)
    public JMSelfBankCashAccount loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType accountType, Consts.BankType bankType) throws NoHasCashAccountException {
        JMSelfBankCashAccount bankCashAccount = jmSelfBankCashAccountMapper.selectByAccountTypeAndBankType(accountType.value, bankType.value);
        if (bankCashAccount == null) {
            throw new NoHasCashAccountException("/admin/finance/openCashAccount");
        }
        return bankCashAccount;
    }


    //job任务验证银行资金账户有效性
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    @Async
    public void verifyBankCashAccount() {
        jmSelfBankCashAccountMapper.loadBankAccountsByStatus(false).parallelStream().forEach(account -> {
            TransferStatusDTO statusDTO = restPayClient.queryAccountStatus(account.getAccId(), account.getBankAccountNum());
            if (statusDTO.isSuccess()) {
                account.setAvailable(true);
                jmSelfBankCashAccountMapper.updateByPrimaryKeySelective(account);
            }
        });
    }
}
