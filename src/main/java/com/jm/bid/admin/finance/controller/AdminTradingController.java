package com.jm.bid.admin.finance.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.service.AdminService;
import com.jm.bid.admin.finance.entity.JMSelfBankCard;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.entity.RefundRecord;
import com.jm.bid.admin.finance.service.JMSelfBankCardService;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.admin.finance.service.JMSelfWithDrawRecordService;
import com.jm.bid.admin.finance.service.RefundRecordService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.dto.TradeRecordDTO;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.user.finance.dto.PrintFlowParamDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Created by xiangyang on 2016/12/16.
 */
@Api("jm-admin-trading")
@Controller
@RequestMapping("/admin/finance")
@RequiresRoles(UserRole.ADMIN_TREASURER)
public class AdminTradingController {


    @Autowired
    private JMSelfCashAccountService adminCashAccountService;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private RefundRecordService refundRecordService;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    @Autowired
    private JMSelfBankCardService jmSelfBankCardService;

    @Autowired
    private JMSelfWithDrawRecordService jmSelfWithDrawRecordService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private RestPayClient restPayClient;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String payAccount(@CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        model.addAttribute("user", adminDTO);
        model.addAttribute("waitRefundList", refundRecordService.selectByRefundTypeAndStatus(false, RefundRecord.RefundStatus.waitTrigger));
        model.addAttribute("cashAccount", adminCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank));
        return "/admin/pay/account";
    }

    @RequestMapping(value = "/cutPaymentDeposit", method = RequestMethod.GET)
    @ApiOperation(value = "扣款页面路由")
    public String payment(@RequestParam("refundRecordId") long refundRecordId, @CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        RefundRecord refundRecord = refundRecordService.selectByPrimaryKey(refundRecordId);
        JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
        if (refundRecord == null) {
            throw new NotFoundException();
        }
        model.addAttribute("admin", adminDTO);
        model.addAttribute("refundRecord", refundRecord);
        model.addAttribute("jmSelfCashAccount", jmSelfBankCashAccount);
        return "/admin/pay/cutPayment";
    }

    //发送扣款验证码
    @RequestMapping(value = "/sendCutPaymentDepositAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送扣款验证码")
    public ResponseEntity sendCutPaymentAuthCode(@CurrentAdmin AdminDTO adminDTO) {
        return new ResponseEntity(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminCutPaymentDeposit), HttpStatus.OK);
    }


    //扣款
    @RequestMapping(value = "/cutPaymentDeposit", method = RequestMethod.POST)
    @ApiOperation(value = "处理扣款请求", response = Response.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "refundRecordId", value = "退款记录id", required = true, paramType = "form"),
            @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true, paramType = "form"),
            @ApiImplicitParam(name = "amount", value = "支付金额", required = true, paramType = "form"),
            @ApiImplicitParam(name = "payPwd", value = "支付密码", required = true, paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "支付备注", paramType = "form"),
    })
    public ResponseEntity cutPayment(@RequestParam("refundRecordId") long refundRecordId, @RequestParam("amount") BigDecimal amount,
                                     @RequestParam("payPwd") String payPwd, @RequestParam("authCode") String authCode,
                                     @RequestParam("remark") String remark, @CurrentAdmin AdminDTO admin) throws NoHasCashAccountException {
        AuthCodeService.AuthCodeErrorType authCodeResult = null;

        if ((authCodeResult = authCodeService.validateAuthCode(admin.getSecurePhone(), AuthCodeType.adminCutPaymentDeposit, authCode)) != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else if (!adminService.validPayPasswordEquals(admin.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);
        } else {
            RefundRecord refundRecord = refundRecordService.selectByPrimaryKey(refundRecordId);
            if (refundRecord == null || refundRecord.getStatus() != RefundRecord.RefundStatus.waitTrigger.value) {
                throw new BusinessException("退款记录不存在!");
            }
            if (amount.compareTo(refundRecord.getPayTotalAmount()) > 0) {
                throw new BusinessException("退款金额有误!");
            }
            refundRecordService.cutPaymentDeposit(refundRecordId, amount, remark);
            authCodeService.updateVerifyCodeInvalid(admin.getSecurePhone(), authCode);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }
    }


    //全部退款
    @RequestMapping(value = "/refundWholeDeposit", method = RequestMethod.GET)
    public String cutPayment(@RequestParam("refundRecordId") long payRecordId, @CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        RefundRecord refundRecord = refundRecordService.selectByPrimaryKey(payRecordId);
        if (refundRecord == null) {
            throw new NotFoundException();
        }
        model.addAttribute("admin", adminDTO);
        model.addAttribute("refundRecord", refundRecord);
        model.addAttribute("payBankAccount",jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank));
        return "/admin/pay/payment";
    }

    //发送退款验证码
    @RequestMapping(value = "/sendRefundWholeDepositAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送退款验证码", response = Response.class)
    public ResponseEntity sendRefundPaymentAuthCode(@CurrentAdmin AdminDTO adminDTO) {
        return new ResponseEntity(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminRefundDeposit), HttpStatus.OK);
    }

    //退全款
    @RequestMapping(value = "/refundWholeDeposit", method = RequestMethod.POST)
    @ApiOperation(value = "处理退全款请求", response = Response.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "refundRecordId", value = "支付记录id", required = true, paramType = "form"),
            @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true, paramType = "form"),
            @ApiImplicitParam(name = "amount", value = "支付金额", required = true, paramType = "form"),
            @ApiImplicitParam(name = "payPwd", value = "支付密码", required = true, paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "支付备注", paramType = "form", required = false),
    })
    public ResponseEntity refundPayment(@RequestParam("refundRecordId") long refundRecordId, @RequestParam("payPwd") String payPwd,
                                        @RequestParam("authCode") String authCode, @RequestParam(value = "remark", required = false) String remark,
                                        @CurrentAdmin AdminDTO admin) {
        AuthCodeService.AuthCodeErrorType authCodeResult = null;

        if ((authCodeResult = authCodeService.validateAuthCode(admin.getSecurePhone(), AuthCodeType.adminRefundDeposit, authCode)) != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else if (!adminService.validPayPasswordEquals(admin.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);
        } else {
            RefundRecord refundRecord = refundRecordService.selectByPrimaryKey(refundRecordId);
            if (refundRecord == null || refundRecord.getStatus() != RefundRecord.RefundStatus.waitTrigger.value) {
                throw new BusinessException("退款记录不存在!");
            }
            refundRecordService.refundWholeDeposit(refundRecordId, remark);
            authCodeService.updateVerifyCodeInvalid(admin.getSecurePhone(), authCode);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.GET)
    public String withdraw(@CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
        JMSelfBankCard bankCard = jmSelfBankCardService.loadJMSelfBankCard();
        if (bankCard == null) {
            return "redirect:/admin/finance/asset";
//            throw new BusinessException("您还未绑定银行卡,请先绑定银行卡!");
        } else if (bankCard.getStatus() != JMSelfBankCard.Status.success.value) {
            return "redirect:/admin/finance/asset";
//            throw new BusinessException("您好,银行卡还未绑定成功,请稍后再试...");
        } else if (bankCard.getStatus() == JMSelfBankCard.Status.success.value && !bankCard.getValidated()) {
            return "redirect:/admin/finance/asset";
//            throw new BusinessException("您好,银行卡已经绑定成功,请验证银行卡金额...");
        } else if (bankCard.getStatus() == JMSelfBankCard.Status.success.value && bankCard.getValidateCount() == 3) {
            return "redirect:/admin/finance/asset";
//            throw new BusinessException("您好,您的银行卡已经绑定成功,验证金额3次失败,请删除重新绑定银行卡...");
        }
        model.addAttribute("user", adminDTO);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("cashAccount", jmSelfBankCashAccount);
        return "/admin/pay/withdraw";
    }


    //发送提现验证码
    @RequestMapping(value = "/sendWithDrawAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送提现验证码")
    public ResponseEntity sendWithDrawAuthCode(@CurrentAdmin AdminDTO adminDTO) {
        return new ResponseEntity(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminWithDraw), HttpStatus.OK);
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true, paramType = "form"),
            @ApiImplicitParam(name = "bankCardId", value = "银行卡号id", required = true, paramType = "form"),
            @ApiImplicitParam(name = "amount", value = "提现金额", required = true, paramType = "form"),
            @ApiImplicitParam(name = "payPwd", value = "支付密码", required = true, paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "提现备注", paramType = "form"),
    })
    @ApiOperation(value = "处理提现请求", response = Response.class)
    public ResponseEntity withDraw(@RequestParam("amount") BigDecimal amount, @RequestParam("bankCardId") long bankCardId,
                                   @RequestParam("payPwd") String payPwd, @RequestParam("authCode") String authCode,
                                   @RequestParam("remark") String remark, @CurrentAdmin AdminDTO adminDTO) throws NoHasCashAccountException {
        AuthCodeService.AuthCodeErrorType authCodeResult = null;
        if ((authCodeResult = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.adminWithDraw, authCode)) != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else if (!adminService.validPayPasswordEquals(adminDTO.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);
        } else {
            JMSelfBankCard bankCard= jmSelfBankCardService.loadJMSelfBankCard();
            if (bankCard == null || bankCard.getStatus() != JMSelfBankCard.Status.success.value || !bankCard.getValidated()) {
                throw new BusinessException("非法请求!");
            }
            jmSelfWithDrawRecordService.withDraw(amount, bankCardId, remark,adminDTO);
            authCodeService.updateVerifyCodeInvalid(adminDTO.getSecurePhone(),authCode);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/tradeRecord", method = RequestMethod.GET)
    public String tradeRecord(TradeRecordDTO tradeRecordDTO, @CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
        tradeRecordDTO.setRecAccountNo(jmSelfBankCashAccount.getBankAccountNum());
        if(tradeRecordDTO.getCashType()!=null&&tradeRecordDTO.getCashType()==1){
           tradeRecordDTO.setType(1);
        }
        model.addAttribute("page", refundRecordService.findJMAllRefundRecord(tradeRecordDTO));
        model.addAttribute("searchParam", tradeRecordDTO);
        return "/admin/pay/tradeRecord";
    }

    @RequestMapping(value = "/bankBill", method = RequestMethod.GET)
    public String tradePay(PrintFlowParamDTO printFlowParamDTO, @CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
        model.addAttribute("searchParam", printFlowParamDTO);
        model.addAttribute("page", restPayClient.findPrintFlow(jmSelfBankCashAccount.getAccId(), jmSelfBankCashAccount.getBankAccountNum(), printFlowParamDTO));
        model.addAttribute("bankCashAccount", jmSelfBankCashAccount);
        return "/admin/pay/bankBill";
    }


}
