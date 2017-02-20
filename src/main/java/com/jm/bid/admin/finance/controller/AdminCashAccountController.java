package com.jm.bid.admin.finance.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.service.AdminService;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.entity.JMSelfSystemCashAccount;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeType;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by xiangyang on 2016/12/16.
 */
@Controller
@RequestMapping("/admin/finance")
@RequiresRoles(UserRole.ADMIN_TREASURER)
public class AdminCashAccountController {

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private JMSelfCashAccountService adminCashAccountService;

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "/openCashAccount", method = RequestMethod.GET)
    public String openCashAccount(@CurrentAdmin AdminDTO adminDTO, Model model) {
        model.addAttribute("securePhone", adminDTO.getSecurePhone());
        return "/admin/pay/openCashAccount";
    }

    @RequestMapping(value = "/hasCashAccount", method = RequestMethod.POST)
    @ApiOperation(value = "判断是否有资金账户", response = Response.class)
    public ResponseEntity hasCashAccount(@CurrentAdmin AdminDTO adminDTO) {
        try {
            adminCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        } catch (NoHasCashAccountException e) {
            return new ResponseEntity(Response.error("noHasCashAccount"), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/sendOpenCashAccountAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "发送开通资金账户手机验证码", response = Response.class, notes = "response.error:sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    public ResponseEntity openCashAccount(@CurrentAdmin AdminDTO adminDTO) {
        return new ResponseEntity(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminOpenCashAccount), HttpStatus.OK);
    }

    @RequestMapping(value = "/openCashAccount", method = RequestMethod.POST)
    @ApiOperation(value = "处理开通资金账户请求", response = Response.class, notes = "response.error:payPwdError,payPwdNotEquals,loginPwdEquals,Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payPwd", value = "支付密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "payConfirmPwd", value = "支付确认密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "authCode", value = "验证码", paramType = "form", required = true),
    })
    public ResponseEntity openCashAccount(@RequestParam("payPwd") String payPwd, @RequestParam("payConfirmPwd") String payConfirmPwd, @RequestParam("authCode") String authCode, @CurrentAdmin AdminDTO adminDTO) {
        AuthCodeService.AuthCodeErrorType authCodeErrorType = null;
        if (StringUtils.isEmpty(payPwd) || StringUtils.length(StringUtils.trimToNull(payPwd)) < 6) {
            return new ResponseEntity<>(Response.error("payPwdError"), HttpStatus.OK);

        } else if (!StringUtils.equals(payPwd, payConfirmPwd)) {
            return new ResponseEntity<>(Response.error("payPwdNotEquals"), HttpStatus.OK);

        } else if (adminService.validPasswordEquals(adminDTO.getSecurePhone(), payPwd)) {
            return new ResponseEntity<>(Response.error("loginPwdEquals"), HttpStatus.OK);

        } else if ((authCodeErrorType = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.adminOpenCashAccount, authCode)) != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity(Response.error(authCodeErrorType.name()), HttpStatus.OK);

        } else {
            try{
                if (adminCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank)!=null) {
                   throw  new BusinessException("晋煤已经开通资金账户,请不要重复开通!");
                }
            }catch (NoHasCashAccountException e){
                JMSelfSystemCashAccount jmSelfSystemCashAccount = adminCashAccountService.openSystemCashAccount(adminDTO);
                // 开通晋煤资金账户
                adminCashAccountService.openJMCashAccount(JMSelfCashAccountService.system_cashAccountName, jmSelfSystemCashAccount.getAccId(), JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank, adminDTO);
                // 开通晋煤保证金专用资金账户
                adminCashAccountService.openJMCashAccount(JMSelfCashAccountService.system_depositAccountName, jmSelfSystemCashAccount.getAccId(), JMSelfBankCashAccount.JMCashAccountType.depositAccount, Consts.BankType.ZXBank, adminDTO);
            }

            return new ResponseEntity(Response.success(), HttpStatus.OK);

        }
    }


    @RequestMapping(value = "/setSuccess", method = RequestMethod.GET)
    @ApiOperation("资金账号开通成功路由")
    public String setSuccess(@CurrentAdmin AdminDTO adminDTO) {
        return "/admin/pay/setSuccess";
    }


}
