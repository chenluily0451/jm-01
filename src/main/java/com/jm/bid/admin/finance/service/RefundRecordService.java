package com.jm.bid.admin.finance.service;

import com.jm.bid.admin.account.entity.Admin;
import com.jm.bid.admin.account.service.AdminService;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.entity.RefundRecord;
import com.jm.bid.admin.finance.mapper.RefundRecordMapper;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.boot.util.BeanMapper;
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
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.bid.dto.BidDTO;
import com.jm.bid.user.bid.service.BidService;
import com.jm.bid.user.finance.entity.PayRecord;
import com.jm.bid.user.finance.service.CashAccountService;
import com.jm.bid.user.finance.service.PayRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiangyang on 2016/12/22.
 */
@Service
@Transactional(readOnly = false)
public class RefundRecordService {

    private static final Logger logger = LoggerFactory.getLogger(RefundRecordService.class);

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private PayRecordService payRecordService;

    @Autowired
    private BidService bidService;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private UserService userService;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SMS sms;

    @Transactional(readOnly = false)
    public void refundDeposit(long tenderId) {
        //所有投标记录
        List<BidDTO> allBidRecord = bidService.selectBidByTenderId(tenderId).parallelStream().filter(b -> b.getChecked() != null).collect(Collectors.toList());

        List<String> winBidCompanyIds = allBidRecord.parallelStream()
                .filter(b -> b.getChecked() != null && b.getChecked())
                .map(BidDTO::getCompanyId).collect(Collectors.toList());

        List<String> failBidCompanyIds = allBidRecord.parallelStream()
                .filter(b -> b.getChecked() != null && !b.getChecked())
                .map(BidDTO::getCompanyId).collect(Collectors.toList());

        //所有付款记录
        payRecordService.selectByTypeAndTenderId(tenderId, PayRecord.Type.deposit, PayRecord.PayStatus.success)
                .parallelStream().forEach(p -> {
            if (winBidCompanyIds.contains(p.getPayCompanyId())) {
                //财务手动退款
                insertRefundDepositRecord(p, false);
            } else if (failBidCompanyIds.contains(p.getPayCompanyId())) {
                //系统自动退款
                insertRefundDepositRecord(p, true);
            } else {
                //缴纳保证金,没有投标,系统自动退款
                insertRefundDepositRecord(p, true);
            }
        });
    }


    /**
     * 插入退还保证金记录
     *
     * @param payRecord 支付记录
     * @param auto      是否自动退款
     */
    @Transactional(readOnly = false)
    private void insertRefundDepositRecord(PayRecord payRecord, boolean auto) {
        try {
            JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.depositAccount, Consts.BankType.ZXBank);
            RefundRecord refundRecord = new RefundRecord();

            if (auto) {
                refundRecord.setAuto(true);
                refundRecord.setFulledRefund(true);
                refundRecord.setStatus(RefundRecord.RefundStatus.init.value);
                refundRecord.setRefundAmount(payRecord.getAmount());
                refundRecord.setPayRemark(payRecord.getTenderCode() + "竞标保证金退款");
                refundRecord.setCreateBy("自动退还保证金");
            } else {
                refundRecord.setAuto(false);
                refundRecord.setFulledRefund(false);
                refundRecord.setStatus(RefundRecord.RefundStatus.waitTrigger.value);
                refundRecord.setCreateBy("财务退还保证金");
            }
            refundRecord.setType(RefundRecord.Type.deposit.value);
            refundRecord.setPayTotalAmount(payRecord.getAmount());

            refundRecord.setTenderId(payRecord.getTenderId());
            refundRecord.setTenderCode(payRecord.getTenderCode());
            refundRecord.setPayTradeId(payRecord.getTradeId());

            refundRecord.setPaySystemNum(jmSelfBankCashAccount.getAccId());
            refundRecord.setPayAccountNo(jmSelfBankCashAccount.getBankAccountNum());
            refundRecord.setPayAccountName(jmSelfBankCashAccount.getBankAccountName());

            refundRecord.setRecSystemNum(payRecord.getPaySystemNum());
            refundRecord.setRecAccountNo(payRecord.getPayAccountNo());
            refundRecord.setRecAccountName(payRecord.getPayAccountName());
            refundRecord.setCreateDate(LocalDateTime.now());
            refundRecordMapper.insertSelective(refundRecord);
        } catch (NoHasCashAccountException e) {
            e.printStackTrace();
        }
    }

    //退还全部保证金
    @Transactional(readOnly = false)
    public void refundWholeDeposit(long refundId, String remark) {

        RefundRecord refundRecord = refundRecordMapper.selectByPrimaryKey(refundId);
        if (refundRecord == null) {
            throw new NotFoundException("退款记录不存在");
        }
        RefundRecord refundRecord1 = BeanMapper.map(refundRecord, RefundRecord.class);
        refundRecord1.setStatus(RefundRecord.RefundStatus.init.value);
        refundRecord1.setFulledRefund(true);
        refundRecord1.setRefundAmount(refundRecord.getPayTotalAmount());
        refundRecord1.setCreateDate(LocalDateTime.now());
        refundRecord1.setPayRemark(remark);
        refundRecordMapper.updateByPrimaryKeySelective(refundRecord1);
    }


    /**
     * 财务确认扣除保证金
     *
     * @param refundId
     * @param refundAmount
     */
    @Transactional(readOnly = false)
    public void cutPaymentDeposit(long refundId, BigDecimal refundAmount, String remark) throws NoHasCashAccountException {

        RefundRecord refundRecord = refundRecordMapper.selectByPrimaryKey(refundId);

        //全部扣除保证金
        if (refundRecord.getPayTotalAmount().compareTo(refundAmount) == 0) {
            //修改退款记录
            refundRecord.setStatus(RefundRecord.RefundStatus.init.value);
            refundRecord.setFulledRefund(true);
            refundRecord.setRefundAmount(refundAmount);
            refundRecord.setPayRemark(refundRecord.getTenderCode() + "竞标保证金退款");
            refundRecord.setCreateDate(LocalDateTime.now());
            refundRecordMapper.updateByPrimaryKeySelective(refundRecord);
        } else {
            //退还到客户
            RefundRecord refundToCustomerRecord = BeanMapper.map(refundRecord, RefundRecord.class);

            refundToCustomerRecord.setStatus(RefundRecord.RefundStatus.init.value);
            refundToCustomerRecord.setFulledRefund(false);
            refundToCustomerRecord.setRefundAmount(refundRecord.getPayTotalAmount().subtract(refundAmount));
            refundToCustomerRecord.setCreateDate(LocalDateTime.now());
            refundToCustomerRecord.setPayRemark(refundRecord.getTenderCode() + "竞标保证金退款");
            refundRecordMapper.updateByPrimaryKeySelective(refundToCustomerRecord);


            //退还到晋煤
            RefundRecord refundToJMRecord = BeanMapper.map(refundRecord, RefundRecord.class);
            JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);

            refundToJMRecord.setRecSystemNum(jmSelfBankCashAccount.getAccId());
            refundToJMRecord.setRecAccountNo(jmSelfBankCashAccount.getBankAccountNum());
            refundToJMRecord.setRecAccountName(jmSelfBankCashAccount.getBankAccountName());

            refundToJMRecord.setRefundAmount(refundAmount);
            refundToJMRecord.setStatus(RefundRecord.RefundStatus.init.value);
            refundToJMRecord.setFulledRefund(false);
            refundToJMRecord.setPayRemark(refundRecord.getTenderCode() + "竞标保证金扣款");
            refundToJMRecord.setCreateDate(LocalDateTime.now());
            refundRecordMapper.insertSelective(refundToJMRecord);

        }

    }

    /**
     * @param auto   退款方式  true 自动退款  false 手动退款
     * @param status 退款状态方式
     * @return
     */
    public List<RefundRecord> selectByRefundTypeAndStatus(boolean auto, RefundRecord.RefundStatus status) {
        return refundRecordMapper.selectByRefundTypeAndStatus(auto, status.value);
    }

    public RefundRecord selectByPrimaryKey(long id) {
        return refundRecordMapper.selectByPrimaryKey(id);
    }


    //依次进行退款
    @Transactional(readOnly = false, noRollbackFor = {HttpServerErrorException.class})
    public void refundDeposit() {
        refundRecordMapper.selectByStatus(RefundRecord.RefundStatus.init.value)
                .parallelStream()
                .filter(r -> r.getStatus() == RefundRecord.RefundStatus.init.value)
                .forEach(r -> invokeBank(r));
    }


    private void invokeBank(RefundRecord r) {
        //向银行发起退款
        TransferDTO transferDTO = new TransferDTO();
        String tradeId = Ids.generateZXClientId();
        transferDTO.setClientId(tradeId);
        transferDTO.setSrcAccId(r.getPaySystemNum());
        transferDTO.setSrcAccNo(r.getPayAccountNo());
        transferDTO.setSrcAccNm(r.getPayAccountName());

        transferDTO.setTargetAccId(r.getRecSystemNum());
        transferDTO.setTargetAccNo(r.getRecAccountNo());
        transferDTO.setTargetAccNm(r.getRecAccountName());
        transferDTO.setAmount(r.getAuto() ? r.getPayTotalAmount() : r.getRefundAmount());

        transferDTO.setOperator("refund_deposit");
        transferDTO.setBankId(Consts.BankType.ZXBank.value);
        transferDTO.setMemo(r.getPayRemark());
        try {
            TransferStatusDTO statusDTO = restPayClient.cashAccountTransfer(transferDTO);
            if (statusDTO.isProcessed()) {
                r.setTradeId(tradeId);
                r.setStatus(RefundRecord.RefundStatus.processing.value);
                refundRecordMapper.updateByPrimaryKeySelective(r);
            } else if (statusDTO.isFail()) {
                r.setTradeId(tradeId);
                r.setStatus(RefundRecord.RefundStatus.fail.value);
                r.setBankMsg(statusDTO.getMessage());
                refundRecordMapper.updateByPrimaryKeySelective(r);
            }
        } catch (Exception e) {
            logger.error("退款出错:", e);
        }

    }

    //刷新退款状态
    public void refreshRefundDepositStatus() {
        refundRecordMapper.selectByStatus(RefundRecord.RefundStatus.processing.value)
                .parallelStream()
                .forEach(r -> {
                    TransferStatusDTO statusDTO = restPayClient.queryTransferStatus(r.getTradeId());
                    if (statusDTO.isSuccess()) {
                        r.setStatus(RefundRecord.RefundStatus.success.value);
                        refundRecordMapper.updateByPrimaryKeySelective(r);
                        try {
                            this.refundSuccessNoticeSms(r);
                        } catch (NoHasCashAccountException e) {
                            e.printStackTrace();
                        }
                    } else if (statusDTO.isFail()) {
                        r.setStatus(RefundRecord.RefundStatus.fail.value);
                        r.setBankMsg(statusDTO.getMessage());
                        refundRecordMapper.updateByPrimaryKeySelective(r);
                    }
                });

    }

    public Page<RefundRecord> findAllRefundRecord(TradeRecordDTO tradeRecordDTO) {
        return refundRecordMapper.findAllRefundRecord(tradeRecordDTO);
    }

    public Page<RefundRecord> findJMAllRefundRecord(TradeRecordDTO tradeRecordDTO) {
        return refundRecordMapper.findJMAllRefundRecord(tradeRecordDTO);
    }

    //晋煤资金账户收款短信
    public static final String jmReceiveDeposit_sms = "您好,您尾号{0}的资金账户于{1},转入人民币{2}元，当前余额为{3}元。";

    //通知财务
    public static final String refundDepositSuccess_sms1 = "您好,贵公司对于竞价公告编号:{0}所缴纳的保证金{1}元已退还，请登录平台查询！";

    //通知业务
    public static final String refundDepositSuccess_sms2 = "您好,贵公司对于竞价公告编号:{0}的合作出现违约，扣除保证金{1}元，剩余{2}元已退还原账户，请登录平台查询！";


    private void refundSuccessNoticeSms(RefundRecord record) throws NoHasCashAccountException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Tender tender = tenderService.loadTenderById(record.getTenderId());

        JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);

        //给晋煤发送短信通知(扣款)
        if (record.getRecAccountNo().equals(jmSelfBankCashAccount.getBankAccountNum())) {
            Admin admin = adminService.loadAdminByRole(UserRole.ADMIN_TREASURER).get(0);

            try {
                CashAccountDTO cashAccountDTO = restPayClient.queryCashAccountBalance(jmSelfBankCashAccount.getAccId(), jmSelfBankCashAccount.getBankAccountNum());
                sms.sendSMS(admin.getSecurePhone(), MessageFormat.format(
                        jmReceiveDeposit_sms,
                        record.getRecAccountNo().substring(record.getRecAccountNo().length() - 4),
                        record.getCreateDate().format(dtf),
                        record.getRefundAmount(),
                        cashAccountDTO.getKyAmount()));
            } catch (Exception e) {
                logger.error("发送退还成功通知短信失败", e);
            }
        } else {
            //给客户发送短信通知
            String companyId = cashAccountService.selectCompanyByAccId(record.getRecSystemNum());
            List<String> phones = userService.loadEmployeeByCompanyId(companyId, UserRole.TREASURER, UserRole.SALESMAN).stream()
                    .map(u -> u.getSecurePhone()).collect(Collectors.toList());
//            全额退款
            if (record.getFulledRefund()) {
                phones.forEach(p -> sms.sendSMS(p, MessageFormat.format(refundDepositSuccess_sms1, tender.getTenderCode(), record.getRefundAmount())));
            }
//           扣款
            if (!record.getFulledRefund()) {
                phones.forEach(p -> sms.sendSMS(p, MessageFormat.format(refundDepositSuccess_sms2,
                        tender.getTenderCode(),
                        record.getPayTotalAmount().subtract(record.getRefundAmount()),
                        record.getRefundAmount()
                )));
            }
        }

    }

}
