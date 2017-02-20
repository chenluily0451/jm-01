package com.jm.bid.user.finance.controller;

import com.jm.bid.boot.annotation.CheckCompany;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.entity.SystemCashAccount;
import com.jm.bid.user.finance.service.CashAccountService;
import com.jm.bid.user.finance.service.PayApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by xiangyang on 2016/12/12.
 */
@Api(tags = "jm-finance-cashAccount")
@Controller
@RequestMapping("/account/finance")
@RequiresRoles(UserRole.TREASURER)
@CheckCompany
public class CashAccountController {

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private PayApplyService payApplyService;


    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation("财务账户概览路由")
    public String payAccount(@CurrentUser UserDTO userDTO, Model model) throws NoHasCashAccountException {
        model.addAttribute("user", userDTO);
        model.addAttribute("cashAccount", cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount));
        model.addAttribute("waitPayList", payApplyService.loadWaitPay(userDTO.getCompanyId()));
        return "/partials/pay/account";
    }

    @RequestMapping(value = "/payApply/deleteApply/{id}", method = RequestMethod.GET)
    @ApiOperation("删除缴纳保证金申请")
    public ResponseEntity deletePayApply(@PathVariable("id") long id, @CurrentUser UserDTO userDTO) {
        payApplyService.deletePayApply(id, userDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/openCashAccount", method = RequestMethod.GET)
    @ApiOperation("开通资金账户路由")
    public String openCashAccount(@CurrentUser UserDTO user, Model model) {
        model.addAttribute("securePhone", user.getSecurePhone());
        return "/partials/pay/openCashAccount";
    }

    @RequestMapping(value = "/hasCashAccount", method = RequestMethod.POST)
    @ApiOperation(value = "判断是否有资金账户", response = Response.class)
    public ResponseEntity hasCashAccount(@CurrentUser UserDTO userDTO) {
        try {
            cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        } catch (NoHasCashAccountException e) {
            return new ResponseEntity(Response.error("noHasCashAccount"), HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/sendOpenCashAccountAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "发送开通资金账户手机验证码", response = Response.class, notes = "response.error:sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    public ResponseEntity openCashAccount(@CurrentUser UserDTO user) {
        return new ResponseEntity(authCodeService.sendVerifyCode(user.getSecurePhone(), AuthCodeType.userOpenCashAccount), HttpStatus.OK);
    }

    @RequestMapping(value = "/openCashAccount", method = RequestMethod.POST)
    @ApiOperation(value = "处理开通资金账户请求", response = Response.class, notes = "response.error:payPwdError,payPwdNotEquals,loginPwdEquals,Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payPwd", value = "支付密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "payConfirmPwd", value = "确认密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "authCode", value = "验证码", paramType = "form", required = true),
    })
    public ResponseEntity openCashAccount(@RequestParam("payPwd") String payPwd, @RequestParam("payConfirmPwd") String payConfirmPwd, @RequestParam("authCode") String authCode, @CurrentUser UserDTO userDTO) {
        AuthCodeService.AuthCodeErrorType authCodeErrorType = null;
        if (StringUtils.isEmpty(payPwd) || StringUtils.length(StringUtils.trimToNull(payPwd)) < 6) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);
        } else if (!StringUtils.equals(payPwd, payConfirmPwd)) {
            return new ResponseEntity<>(Response.error("payPwdNotEquals"), HttpStatus.OK);
        } else if (userService.validPasswordEquals(userDTO.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("loginPwdEquals"), HttpStatus.OK);
        } else if ((authCodeErrorType = authCodeService.validateAuthCode(userDTO.getSecurePhone(), AuthCodeType.userOpenCashAccount, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity(Response.error(authCodeErrorType.name()), HttpStatus.OK);
        } else {
            SystemCashAccount systemCashAccount = cashAccountService.loadSysAccountByCompanyId(userDTO.getCompanyId());
            //开通系统资金账户
            if (systemCashAccount == null) {
                systemCashAccount = cashAccountService.openSystemCashAccount(userDTO.getCompanyId(), userDTO);
            }
            try {
                BankCashAccount bankCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
                if (bankCashAccount != null) {
                    throw new BusinessException("警告:您已经开通晋煤资金账户,请勿多次开通!");
                }
            } catch (NoHasCashAccountException ex) {
                //开通银行资金账户
                cashAccountService.openZXBankAccount(userDTO.getCompanyName(), systemCashAccount.getAccId(), userDTO);
            }
            userService.updatePayPassword(userDTO.getSecurePhone(), payPwd);
            authCodeService.updateVerifyCodeInvalid(userDTO.getSecurePhone(), authCode);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/setSuccess", method = RequestMethod.GET)
    @ApiOperation("资金账号开通成功路由")
    public String setSuccess(@CurrentUser UserDTO userDTO) {
        return "/partials/pay/setSuccess";
    }


}
