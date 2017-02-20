package com.jm.bid.user.finance.service;

import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.service.SMS;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.finance.dto.PaymentApplyDTO;
import com.jm.bid.user.finance.entity.PaymentApply;
import com.jm.bid.user.finance.mapper.PaymentApplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by xiangyang on 2016/12/13
 */
@Service
@Transactional(readOnly = true)
public class PayApplyService {

    private static final Logger logger = LoggerFactory.getLogger(PayApplyService.class);

    @Autowired
    private PaymentApplyMapper paymentApplyMapper;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    @Autowired
    private SMS sms;

    @Autowired
    private UserService userService;


    public static final String depositPayApply_sms = "您好,贵公司有一笔{0}元的保证金付款申请，请登录平台在{1}前完成支付，以免耽误业务合作！";

    @Transactional(readOnly = false)
    public void addPayApply(long tenderId, int quantity, UserDTO userDTO) {
        Tender tender = tenderService.loadTenderById(tenderId);
        if (tender == null) {
            throw new NotFoundException();
        }
       PaymentApply paymentApply = paymentApplyMapper.selectByTenderIdAndCompanyId(tenderId, userDTO.getCompanyId());
        if(paymentApply!=null){
           throw  new BusinessException("您已经报名成功,请不要重复报名!");
        }else{
            try {
                // 支付到保证金账户
                JMSelfBankCashAccount cashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.depositAccount, Consts.BankType.ZXBank);
                PaymentApply apply = new PaymentApply();
                apply.setAmount(calcDepositAmount(quantity));
                apply.setQuantity(quantity);
                apply.setRecAccountNo(cashAccount.getBankAccountNum());
                apply.setType(PaymentApply.Type.deposit.value);
                apply.setTenderId(tender.getId());
                apply.setTenderCode(tender.getTenderCode());
                apply.setCreateBy(userDTO.getSecurePhone());
                apply.setCompanyId(userDTO.getCompanyId());
                apply.setCompanyName(userDTO.getCompanyName());
                apply.setDeleted(false);
                apply.setDone(false);
                apply.setCreateDate(LocalDateTime.now());
                paymentApplyMapper.insertSelective(apply);

                //发短信通知财务人员缴纳保证金
                User user = userService.loadEmployeeByCompanyId(userDTO.getCompanyId(), UserRole.TREASURER).get(0);
                if (user != null) {
                    sms.sendSMS(user.getSecurePhone(), MessageFormat.format(depositPayApply_sms, apply.getAmount(), tender.getTenderStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                }


            } catch (NoHasCashAccountException e) {
                logger.error("业务员报名交保证金失败:晋煤没有开通资金账号");
            }

        }

    }

    private BigDecimal calcDepositAmount(int quantity) {

        if (quantity == 1000) {
            return BigDecimal.valueOf(100000);
        } else if (quantity == 2000) {
            return BigDecimal.valueOf(150000);
        } else if (quantity == 3000) {
            return BigDecimal.valueOf(200000);
        } else if (quantity == 4000) {
            return BigDecimal.valueOf(250000);
        } else {
            return BigDecimal.valueOf(300000);
        }

    }

    public List<PaymentApplyDTO> loadWaitPay(String companyId) {
        return paymentApplyMapper.loadAllPayApply(companyId, false);
    }

    @Transactional(readOnly = false)
    public void deletePayApply(long applyId, UserDTO user) {
        PaymentApply paymentApply = paymentApplyMapper.selectByPrimaryKey(applyId, user.getCompanyId());
        if (paymentApply == null) {
            throw new NotFoundException();
        }
        paymentApply.setDeleted(true);
        paymentApplyMapper.updateByPrimaryKeySelective(paymentApply);
    }

    public PaymentApply loadByCompanyIdAndId(long applyId, String companyId) {
        return paymentApplyMapper.selectByPrimaryKey(applyId, companyId);
    }


    @Transactional(readOnly = false)
    public void updatePayApplyDone(long applyId, String companyId) {
        PaymentApply paymentApply = paymentApplyMapper.selectByPrimaryKey(applyId, companyId);
        if (paymentApply == null) {
            throw new BusinessException("支付申请不存在!");
        }
        paymentApply.setDone(true);
        paymentApplyMapper.updateByPrimaryKeySelective(paymentApply);
    }

    public PaymentApply loadPayApplyByTenderId(long tenderId, String companyId) {
        return paymentApplyMapper.selectByTenderIdAndCompanyId(tenderId, companyId);
    }
}
