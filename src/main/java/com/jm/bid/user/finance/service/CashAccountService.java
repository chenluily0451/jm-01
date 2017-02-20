package com.jm.bid.user.finance.service;

import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.SMS;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.CashAccountDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.entity.SystemCashAccount;
import com.jm.bid.user.finance.mapper.BankCashAccountMapper;
import com.jm.bid.user.finance.mapper.SystemCashAccountMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/12.
 */
@Service
@Transactional(readOnly = true)
public class CashAccountService {

    private static final Logger logger = LoggerFactory.getLogger(CashAccountService.class);

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private BankCashAccountMapper bankCashAccountMapper;

    @Autowired
    private SystemCashAccountMapper systemCashAccountMapper;

    @Autowired
    private SMS sms;

    @Autowired
    private UserService userService;

    public static final String openCashAccountSuccess_sms = "您好!贵公司的资金账户已开通，请妥善保管您的支付密码!";

    /**
     * 开通系统资金账户
     *
     * @param companyId
     * @param userDTO
     * @return
     */
    @Transactional(readOnly = false)
    public SystemCashAccount openSystemCashAccount(String companyId, UserDTO userDTO) {
        SystemCashAccount systemCashAccount = this.loadSysAccountByCompanyId(companyId);
        if (systemCashAccount == null) {
            CashAccountDTO cashAccountDTO = restPayClient.createAccount(companyId);
            if (cashAccountDTO.isProcessed() || cashAccountDTO.isSuccess()) {
                systemCashAccount = new SystemCashAccount();
                systemCashAccount.setCompanyId(companyId);
                systemCashAccount.setAccId(StringUtils.trimToNull(cashAccountDTO.getAccId()));
                systemCashAccount.setCreateDate(LocalDateTime.now());
                systemCashAccount.setCreateBy(String.valueOf(userDTO.getId()));
                systemCashAccountMapper.insertSelective(systemCashAccount);
                return systemCashAccount;
            } else {
                logger.error("aegis-pg openSystemCashAccount:{}", cashAccountDTO);
            }
        }
        return systemCashAccount;

    }


    /**
     * 开通中信银行资金账户
     *
     * @param accountName
     * @param accId
     * @param userDTO
     * @return
     */
    @Transactional(readOnly = false)
    public CashAccountDTO openZXBankAccount(String accountName, String accId, UserDTO userDTO) {
        CashAccountDTO cashAccountDTO = restPayClient.createZXBankAccount(accountName, accId);
        if (cashAccountDTO.isProcessed() || cashAccountDTO.isSuccess()) {
            BankCashAccount cashAccount = new BankCashAccount();
            cashAccount.setAccId(accId);
            cashAccount.setBankAccountName(accountName);
            cashAccount.setBankAccountNum(StringUtils.trimToNull(cashAccountDTO.getAccNo()));
            cashAccount.setCreateDate(LocalDateTime.now());
            cashAccount.setCreateBy(String.valueOf(userDTO.getId()));
            cashAccount.setBankId(Consts.BankType.ZXBank.value);
            cashAccount.setAccountType(BankCashAccount.AccountType.generalAccount.value);
            cashAccount.setAvailable(false);
            bankCashAccountMapper.insertSelective(cashAccount);
        } else {
            logger.error("aegis-pg openZXBankAccount,pg responseErrorMsg:{}", cashAccountDTO.getErrMess());
        }
        return cashAccountDTO;
    }


    /**
     * 通过公司id加载系统账户
     *
     * @param companyId
     * @return
     */

    public SystemCashAccount loadSysAccountByCompanyId(String companyId) {
        return systemCashAccountMapper.selectByCompanyId(companyId);
    }

    /**
     * 通过公司id加载中信资金账户
     *
     * @param companyId 公司id
     * @param type      账号类型
     * @return
     * @throws NoHasCashAccountException 没有资金账户抛异常,异常解析器会自动重定向到开通资金账户页面
     */
    public BankCashAccount loadBankCashAccountByCompanyId(String companyId, BankCashAccount.AccountType type) throws NoHasCashAccountException {
        SystemCashAccount systemCashAccount = systemCashAccountMapper.selectByCompanyId(companyId);
        if (systemCashAccount == null) {
            throw new NoHasCashAccountException("/payLicence");
        }
        BankCashAccount bankCashAccount = bankCashAccountMapper.selectByAccId(systemCashAccount.getAccId(), type.value);
        if (bankCashAccount == null) {
            throw new NoHasCashAccountException("/payLicence");
        }
        return bankCashAccount;
    }


    //job任务验证银行资金账户有效性
    @Transactional(readOnly = false)
    public void verifyBankCashAccount() {
        bankCashAccountMapper.loadBankAccountsByStatus(false).parallelStream().forEach(account -> {
            TransferStatusDTO statusDTO = restPayClient.queryAccountStatus(account.getAccId(), account.getBankAccountNum());
            if (statusDTO.isSuccess()) {
                account.setAvailable(true);
                bankCashAccountMapper.updateByPrimaryKeySelective(account);
                try {
                    SystemCashAccount systemCashAccount = systemCashAccountMapper.selectByAccId(account.getAccId());
                    User user = userService.loadEmployeeByCompanyId(systemCashAccount.getCompanyId(), UserRole.TREASURER).get(0);
                    sms.sendSMS(user.getSecurePhone(), openCashAccountSuccess_sms);
                } catch (Exception e) {
                    logger.error("短信通知开通资金账户失败", e);
                }
            }
        });
    }

    public String selectCompanyByAccId(String accId) {
        SystemCashAccount systemCashAccount = systemCashAccountMapper.selectByAccId(accId);
        if (systemCashAccount == null) {
           throw  new BusinessException("资金账户不存在");
        }
        return systemCashAccount.getCompanyId();
    }
}
