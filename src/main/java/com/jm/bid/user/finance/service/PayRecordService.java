package com.jm.bid.user.finance.service;

import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.dto.TradeRecordDTO;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.service.SMS;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.CashAccountDTO;
import com.jm.bid.restpay.dto.TransferDTO;
import com.jm.bid.restpay.dto.TransferStatusDTO;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.entity.PayRecord;
import com.jm.bid.user.finance.entity.PaymentApply;
import com.jm.bid.user.finance.mapper.PayRecordMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by xiangyang on 2016/12/13.
 */
@Service
@Transactional(readOnly = true)
public class PayRecordService {

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private PayApplyService payApplyService;

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private SMS sms;

    @Autowired
    private PayRecordMapper payRecordMapper;

    private static final Logger logger = LoggerFactory.getLogger(PayRecordService.class);

    public PayRecord loadPayRecordByTenderId(long tenderId, String companyId, PayRecord.Type type) {
        return payRecordMapper.selectByTypeAndCompanyId(tenderId, type.value, companyId);
    }

    @Transactional(readOnly = false, noRollbackFor = {HttpServerErrorException.class})
    public void payDeposit(long payApplyId, String remark, UserDTO userDTO) throws NoHasCashAccountException {
        PaymentApply paymentApply = payApplyService.loadByCompanyIdAndId(payApplyId, userDTO.getCompanyId());
        //判断支付请求存在
        if (paymentApply == null) {
            logger.error("支付申请不存在,申请id={},操作人{}", payApplyId, userDTO.getSecurePhone());
            throw new BusinessException("支付申请不存在!");
        }

        //加载保证金支付记录
        PayRecord payDepositRecord = this.loadPayRecordByTenderId(paymentApply.getTenderId(), userDTO.getCompanyId(), PayRecord.Type.deposit);
        if (payDepositRecord != null && payDepositRecord.getStatus() != PayRecord.PayStatus.fail.value) {
            throw new BusinessException("编号为:" + paymentApply.getTenderCode() + "的竞价保证金已经支付,请不要重复支付!");
        }

        //判断余额不足
        BankCashAccount payCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        CashAccountDTO cashAccountDTO = restPayClient.queryCashAccountBalance(payCashAccount.getAccId(), payCashAccount.getBankAccountNum());
        if(cashAccountDTO.getStatus()==null || cashAccountDTO.getStatus()==3){
            throw new ResourceAccessException("查询资金账户余额失败");
        }
        if (cashAccountDTO.getKyAmount().compareTo(paymentApply.getAmount()) == -1) {
            throw new BusinessException("支付失败,余额不足!");
        }

        JMSelfBankCashAccount jmDepositAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.depositAccount, Consts.BankType.ZXBank);

        PayRecord payRecord = new PayRecord();
        String tradeId = Ids.generateZXClientId();
        payRecord.setPayApplyId(payApplyId);
        payRecord.setType(PayRecord.Type.deposit.value);
        payRecord.setAmount(paymentApply.getAmount());
        payRecord.setTenderId(paymentApply.getTenderId());
        payRecord.setTenderCode(paymentApply.getTenderCode());

        payRecord.setBankId(Consts.BankType.ZXBank.value);
        payRecord.setCreateBy(userDTO.getSecurePhone());
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setTradeId(tradeId);
        payRecord.setDeleted(false);

        payRecord.setPayRemark(StringUtils.trimToNull(remark));
        payRecord.setPaySystemNum(payCashAccount.getAccId());
        payRecord.setPayAccountName(payCashAccount.getBankAccountName());
        payRecord.setPayAccountNo(payCashAccount.getBankAccountNum());
        payRecord.setPayCompanyId(userDTO.getCompanyId());

        payRecord.setRecSystemNum(jmDepositAccount.getAccId());
        payRecord.setRecAccountNo(jmDepositAccount.getBankAccountNum());
        payRecord.setRecAccountName(jmDepositAccount.getBankAccountName());

        payRecordMapper.insertSelective(payRecord);

        //向银行发起付款

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setClientId(tradeId);

        transferDTO.setSrcAccId(payRecord.getPaySystemNum());
        transferDTO.setSrcAccNo(payRecord.getPayAccountNo());
        transferDTO.setSrcAccNm(payRecord.getPayAccountName());

        transferDTO.setTargetAccId(payRecord.getRecSystemNum());
        transferDTO.setTargetAccNo(payRecord.getRecAccountNo());
        transferDTO.setTargetAccNm(payRecord.getRecAccountName());
        transferDTO.setAmount(payRecord.getAmount());

        transferDTO.setOperator(userDTO.getSecurePhone());
        transferDTO.setBankId(Consts.BankType.ZXBank.value);
        transferDTO.setMemo(remark);

        //银行发起转账
        restPayClient.cashAccountTransfer(transferDTO);
    }

    /**
     * @param tenderId  竞价id
     * @param type      支付类型
     * @param companyId 公司id
     * @return
     */
    public boolean isPaySuccess(long tenderId, PayRecord.Type type, String companyId) {
        PayRecord payRecord = payRecordMapper.selectByTypeAndCompanyId(tenderId, type.value, companyId);
        if (payRecord != null && payRecord.getStatus() == PayRecord.PayStatus.success.value) {
            return true;
        }
        return false;
    }


    public Page<PayRecord> findAllPayRecord(TradeRecordDTO tradeRecordDTO) {
        return payRecordMapper.findAllPayRecord(tradeRecordDTO);
    }

    public List<PayRecord> selectByTypeAndTenderId(long tenderId, PayRecord.Type type, PayRecord.PayStatus status) {
        return payRecordMapper.selectByTypeAndTenderId(tenderId, type.value, status.value);
    }


    @Transactional(readOnly = false)
    public void updatePayRecord() {
        payRecordMapper.loadAllTradeByStatus(PayRecord.PayStatus.init.value, PayRecord.PayStatus.processing.value)
                .parallelStream()
                .forEach(r -> {
                    try {
                        TransferStatusDTO statusDTO = restPayClient.queryTransferStatus(r.getTradeId());
                        if (statusDTO.isProcessed() && r.getStatus() == PayRecord.PayStatus.init.value) {
                            r.setStatus(PayRecord.PayStatus.processing.value);
                            payRecordMapper.updateByPrimaryKeySelective(r);
                        } else if (statusDTO.isSuccess()) {
                            r.setStatus(PayRecord.PayStatus.success.value);
                            payRecordMapper.updateByPrimaryKeySelective(r);
                           //设置支付完成
                           payApplyService.updatePayApplyDone(r.getPayApplyId(), r.getPayCompanyId());
                           this.paySuccessNoticeSms(r);
                        } else if (statusDTO.isFail()) {
                            r.setBankMsg(statusDTO.getMessage());
                            r.setStatus(PayRecord.PayStatus.fail.value);
                            payRecordMapper.updateByPrimaryKeySelective(r);
                        }
                    } catch (Exception e) {
                        logger.error("银行网络异常...", e);
                    }
                });
    }

    //通知财务
    public static final String payDepositSuccess_sms1 = "您尾号{0}的资金账户于{1},晋煤平台支出人民币{2}元，当前余额为{3}元。";

    //通知业务
    public static final String payDepositSuccess_sms2 = "您申请的一笔{0}元的保证金已支付成功，请及时登录平台进行报价！报价时间为：{1}至{2}";


    private void paySuccessNoticeSms(PayRecord record) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Tender tender = tenderService.loadTenderById(record.getTenderId());
        List<User> userList = userService.loadEmployeeByCompanyId(record.getPayCompanyId(), UserRole.TREASURER, UserRole.SALESMAN);
        for (User user : userList) {
            //通过财务
            if (user.getRole() == UserRole.TREASURER) {
                try {

                    CashAccountDTO cashAccountDTO = restPayClient.queryCashAccountBalance(record.getPaySystemNum(), record.getPayAccountNo());
                    sms.sendSMS(user.getSecurePhone(),
                            MessageFormat.format(payDepositSuccess_sms1,
                                    record.getPayAccountNo().substring(record.getPayAccountNo().length() - 4),
                                    record.getCreateDate().format(dtf),
                                    record.getAmount(),
                                    cashAccountDTO.getKyAmount()
                            ));
                } catch (Exception e) {
                    logger.error("财务发送支付保证金短信失败", e);
                }
            }
            //通过业务
            if (user.getRole() == UserRole.SALESMAN) {
                sms.sendSMS(user.getSecurePhone(), MessageFormat.format(payDepositSuccess_sms2, record.getAmount(), tender.getTenderStartDate().format(dtf), tender.getTenderEndDate().format(dtf)));
            }
        }
    }
}
