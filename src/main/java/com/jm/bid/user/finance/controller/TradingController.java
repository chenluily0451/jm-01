package com.jm.bid.user.finance.controller;

import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.admin.finance.service.RefundRecordService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.annotation.CheckCompany;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.dto.TradeRecordDTO;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.finance.dto.PrintFlowParamDTO;
import com.jm.bid.user.finance.entity.BankCard;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.entity.PayRecord;
import com.jm.bid.user.finance.entity.PaymentApply;
import com.jm.bid.user.finance.service.*;
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
 * Created by xiangyang on 2016/12/18.
 */
@Api("jm-finance-trading")
@Controller
@RequestMapping("/account/finance")
@RequiresRoles(UserRole.TREASURER)
@CheckCompany
public class TradingController {


    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    @Autowired
    private PayApplyService payApplyService;

    @Autowired
    private PayRecordService payRecordService;

    @Autowired
    private RefundRecordService refundRecordService;

    @Autowired
    private WithDrawRecordService withDrawRecordService;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private TenderService tenderService;

    //发送支付保证金验证码
    @RequestMapping(value = "/sendPayDepositAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送支付保证金验证码")
    public ResponseEntity sendPayDepositAuthCode(@CurrentUser UserDTO userDTO) {
        return new ResponseEntity(authCodeService.sendVerifyCode(userDTO.getSecurePhone(), AuthCodeType.userPayDeposit), HttpStatus.OK);
    }

    //支付保证金付款页面
    @RequestMapping(value = "/payDeposit", method = RequestMethod.GET)
    @ApiOperation("付款页面路由")
    public String payment(@RequestParam("applyId") long applyId, @CurrentUser UserDTO userDTO, Model model) throws NoHasCashAccountException {
        PaymentApply paymentApply = payApplyService.loadByCompanyIdAndId(applyId, userDTO.getCompanyId());
        if (paymentApply == null) {
            throw new NotFoundException();
        }

        //判断支付请求存在
        Tender tender = tenderService.loadTenderById(paymentApply.getTenderId());
        if(tender==null){
           throw  new BusinessException("竞价记录不存在!");
        }
        if (tender.getStatus() != Tender.Status.predict.value) {

            if (tender.getStatus() == Tender.Status.processing.value) {
                throw new BusinessException("竞价项目已经开始,不能缴纳保证金!");
            }else{
                throw new BusinessException("竞价项目已经结束,不能缴纳保证金!");
            }
        }

        //加载保证金支付记录
        PayRecord payDepositRecord = payRecordService.loadPayRecordByTenderId(paymentApply.getTenderId(), userDTO.getCompanyId(), PayRecord.Type.deposit);
        if (payDepositRecord != null && payDepositRecord.getStatus() != PayRecord.PayStatus.fail.value) {
            throw new BusinessException("编号为:" + paymentApply.getTenderCode() + "的竞价保证金已经支付,请不要重复支付!");
        }

        model.addAttribute("user", userDTO);
        model.addAttribute("payApply", paymentApply);
        model.addAttribute("payAccount", cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount));
        model.addAttribute("receiveAccount", jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank));
        return "/partials/pay/payment";
    }


    // 支付保证金请求
    @RequestMapping(value = "/payDeposit", method = RequestMethod.POST)
    @ApiOperation(value = "/payDeposit", notes = "支付保证金")
    public ResponseEntity payDeposit(@RequestParam("applyId") long applyId,
                                     @RequestParam("authCode") String authCode,
                                     @RequestParam("payPwd") String payPwd,
                                     @RequestParam(value = "remark", required = false) String remark,
                                     @CurrentUser UserDTO user) throws NoHasCashAccountException {
        AuthCodeErrorType authCodeResult = null;
        if ((authCodeResult = authCodeService.validateAuthCode(user.getSecurePhone(), AuthCodeType.userPayDeposit, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else if (!userService.validPayPasswordEquals(user.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);
        } else {
            PaymentApply paymentApply = payApplyService.loadByCompanyIdAndId(applyId, user.getCompanyId());
            if (paymentApply == null) {
                throw new NotFoundException();
            }
            //判断支付请求存在
            Tender tender = tenderService.loadTenderById(paymentApply.getTenderId());
            if(tender==null){
                throw  new BusinessException("竞价记录不存在!");
            }
            if (tender.getStatus() != Tender.Status.predict.value) {

                if (tender.getStatus() == Tender.Status.processing.value) {
                    throw new BusinessException("竞价项目已经开始,不能缴纳保证金!");
                }else{
                    throw new BusinessException("竞价项目已经结束,不能缴纳保证金!");
                }
            }
            payRecordService.payDeposit(applyId, remark, user);
            authCodeService.updateVerifyCodeInvalid(user.getSecurePhone(), authCode);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }
    }


    //交易记录
    @RequestMapping(value = "/tradeRecord", method = RequestMethod.GET)
    @ApiOperation("交易记录路由")
    public String tradeRecord(TradeRecordDTO tradeRecordDTO, @CurrentUser UserDTO userDTO, Model model) throws NoHasCashAccountException {
        BankCashAccount bankCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        //查询缴纳记录
        if (tradeRecordDTO.getCashType() == null || tradeRecordDTO.getCashType() == 1) {
            tradeRecordDTO.setCashType(1);
            //类型默认是保证金
            tradeRecordDTO.setType(1);
            tradeRecordDTO.setRecAccountNo(tradeRecordDTO.getBankAccountNum());
            tradeRecordDTO.setPayAccountNo(bankCashAccount.getBankAccountNum());
            model.addAttribute("page", payRecordService.findAllPayRecord(tradeRecordDTO));
        } else if (tradeRecordDTO.getCashType() == 2) {
            //查询退款记录
            tradeRecordDTO.setCashType(2);
            //类型默认是保证金
            tradeRecordDTO.setType(1);
            tradeRecordDTO.setRecAccountNo(bankCashAccount.getBankAccountNum());
            tradeRecordDTO.setPayAccountNo(tradeRecordDTO.getBankAccountNum());
            model.addAttribute("page", refundRecordService.findAllRefundRecord(tradeRecordDTO));
        }
        model.addAttribute("searchParam", tradeRecordDTO);
        return "/partials/pay/tradeRecord";
    }

    //银行账单
    @RequestMapping(value = "/bankBill", method = RequestMethod.GET)
    @ApiOperation("银行流水路由")
    public String bankBill(PrintFlowParamDTO printFlowParamDTO, @CurrentUser UserDTO userDTO, Model model) throws NoHasCashAccountException {
        BankCashAccount bankCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        try{
            model.addAttribute("page", restPayClient.findPrintFlow(bankCashAccount.getAccId(), bankCashAccount.getBankAccountNum(), printFlowParamDTO));
        }catch (Exception e){
           throw  new BusinessException("银行网络异常,请稍后再试...");
        }

        model.addAttribute("searchParam", printFlowParamDTO);
        model.addAttribute("bankCashAccount", bankCashAccount);
        return "/partials/pay/bankBill";
    }



    //提现页面
    @RequestMapping(value = "/withdraw", method = RequestMethod.GET)
    @ApiOperation("提现页面路由")
    public String withdraw(@CurrentUser UserDTO userDTO, Model model) throws NoHasCashAccountException {

        BankCashAccount bankCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        BankCard bankCard = bankCardService.loadCardByCompanyId(userDTO.getCompanyId());
        if (bankCard == null) {
            throw new BusinessException("您还未绑定银行卡,请先绑定银行卡!");
        } else if (bankCard.getStatus() != BankCard.Status.success.value) {
            throw new BusinessException("您好,银行卡还未绑定成功,请稍后再试...");
        } else if (bankCard.getStatus() == BankCard.Status.success.value && !bankCard.getValidated()) {
            throw new BusinessException("您好,银行卡已经绑定成功,请验证银行卡金额...");
        } else if (bankCard.getStatus() == BankCard.Status.success.value && bankCard.getValidateCount() == 3) {
            throw new BusinessException("您好,您的银行卡已经绑定成功,验证金额3次失败,请删除重新绑定银行卡...");
        }
        model.addAttribute("user", userDTO);
        model.addAttribute("bankCard", bankCard);
        model.addAttribute("cashAccount", bankCashAccount);
        return "/partials/pay/withdraw";
    }

    //发送提现验证码
    @RequestMapping(value = "/sendWithDrawAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送提现验证码")
    public ResponseEntity sendWithDrawAuthCode(@CurrentUser UserDTO userDTO) {
        return new ResponseEntity(authCodeService.sendVerifyCode(userDTO.getSecurePhone(), AuthCodeType.userWithDraw), HttpStatus.OK);
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
                                   @RequestParam("remark") String remark, @CurrentUser UserDTO user) throws NoHasCashAccountException {
        AuthCodeErrorType authCodeResult = null;
        if ((authCodeResult = authCodeService.validateAuthCode(user.getSecurePhone(), AuthCodeType.userWithDraw, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else if (!userService.validPayPasswordEquals(user.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);
        } else {
            BankCard bankCard = bankCardService.selectByIdAndCompanyId(bankCardId, user.getCompanyId());
            if (bankCard == null || bankCard.getStatus() != BankCard.Status.success.value || !bankCard.getValidated()) {
                throw new BusinessException("非法请求!");
            }
            withDrawRecordService.withDraw(amount, bankCardId, remark, user);
            authCodeService.updateVerifyCodeInvalid(user.getSecurePhone(), authCode);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }
    }
}
