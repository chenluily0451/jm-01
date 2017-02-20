package com.jm.bid.admin.finance.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.finance.entity.JMSelfBankCard;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.service.JMSelfBankCardService;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.CashAccountDTO;
import com.jm.bid.user.finance.entity.BankCard;
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

@Controller
@RequestMapping("/admin/finance/asset")
@RequiresRoles(UserRole.ADMIN_TREASURER)
public class AdminAssetController {
    @Autowired
    private JMSelfCashAccountService adminCashAccountService;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private JMSelfBankCardService bankCardService;

    @Autowired
    private RestPayClient restPayClient;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    //公司资产
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation("资金账户详情,绑定银行卡路由")
    public String myBankCard(@CurrentAdmin AdminDTO adminDTO, Model model) throws NoHasCashAccountException {
        model.addAttribute("bankCashAccount", adminCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank));
        model.addAttribute("bankCard", bankCardService.loadJMSelfBankCard());
        model.addAttribute("user", adminDTO);
        return "/admin/pay/asset";
    }

    //公司资产
    @RequestMapping(value = "/queryAccountBalance", method = RequestMethod.GET)
    @ApiOperation(value = "查询资金账户余额", response = CashAccountDTO.class)
    public ResponseEntity<CashAccountDTO> queryCashAccountBalance(@CurrentAdmin AdminDTO adminDTO) throws NoHasCashAccountException {
        JMSelfBankCashAccount bankCashAccount = adminCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
        if (bankCashAccount == null) {
            return new ResponseEntity(Response.error("noOpenCashAccount"), HttpStatus.OK);
        }
        CashAccountDTO cashAccountDTO = restPayClient.queryCashAccountBalance(bankCashAccount.getAccId(), bankCashAccount.getBankAccountNum());
        if (!cashAccountDTO.isSuccess()) {
            return new ResponseEntity<CashAccountDTO>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<CashAccountDTO>(cashAccountDTO, HttpStatus.OK);
    }


    @RequestMapping(value = "/sendBindCardAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送绑定银行卡验证码", notes = "response.error:sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    public ResponseEntity<Response> sendBindBankCardVerifyCode(@CurrentAdmin AdminDTO adminDTO) {
        return new ResponseEntity<>(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminBindBankCard), HttpStatus.OK);
    }

    @RequestMapping(value = "/addBindBankCard", method = RequestMethod.POST)
    @ApiOperation(value = "处理绑定提现银行卡请求", notes = "response.error:Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardNum", value = "银行卡号码", paramType = "form", required = true),
            @ApiImplicitParam(name = "cardAccountName", value = "银行卡账户名", paramType = "form", required = true),
            @ApiImplicitParam(name = "childBankCode", value = "支行联行号", paramType = "form", required = true),
            @ApiImplicitParam(name = "authCode", value = "手机验证码", paramType = "form", required = true)
    })
    public ResponseEntity<Response> bindBankCard(@RequestParam("cardNum") String cardNum, @RequestParam("cardAccountName") String cardAccountName,
                                                 @RequestParam("childBankCode") String childBankCode, @RequestParam("authCode") String authCode,
                                                 @CurrentAdmin AdminDTO adminDTO) {

        AuthCodeService.AuthCodeErrorType authCodeErrorType = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.adminBindBankCard, authCode);
        if (authCodeErrorType != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<Response>(Response.error(authCodeErrorType.name()), HttpStatus.OK);
        } else {
            JMSelfBankCard bankCard = new JMSelfBankCard();
            bankCard.setCardNum(cardNum);
            bankCard.setCardAccountName(cardAccountName);
            bankCard.setChildBankCode(childBankCode);
            bankCardService.addBindCard(bankCard, adminDTO);
            authCodeService.updateVerifyCodeInvalid(adminDTO.getSecurePhone(), authCode);
            return new ResponseEntity<Response>(Response.success(), HttpStatus.OK);
        }

    }


    @RequestMapping(value = "/validateBankCard", method = RequestMethod.POST)
    @ApiOperation(value = "处理验证提现银行卡请求", notes = "response.error= authFail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", paramType = "form", required = true),
            @ApiImplicitParam(name = "amount", value = "验证金额", paramType = "form", required = true)
    })
    public ResponseEntity<Response> validateBindCard(@RequestParam("id") long id, @RequestParam("amount") BigDecimal amount, @CurrentAdmin AdminDTO adminDTO) {
        JMSelfBankCard bankCard = bankCardService.loadById(id);
        if (bankCard == null || bankCard.getStatus() != JMSelfBankCard.Status.success.value) {
            throw new NotFoundException();
        }
        if (bankCard.getValidateCount() == 3) {
            return new ResponseEntity(Response.error("authLimitation"), HttpStatus.OK);
        }
        if (bankCard.getValidateAmount().compareTo(amount) != 0) {
            bankCard.setValidateCount(bankCard.getValidateCount() + 1);
            bankCardService.updateBankCard(bankCard);
            return new ResponseEntity(Response.error("authFail").setData(3 - bankCard.getValidateCount()), HttpStatus.OK);
        }
        bankCard.setValidated(true);
        bankCardService.updateBankCard(bankCard);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }


    @RequestMapping(value = "/deleteBankCard", method = RequestMethod.POST)
    @ApiOperation(value = "删除绑定的银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", paramType = "form", required = true)
    })
    public ResponseEntity<Response> deleteBankCard(@RequestParam("id") long id, @CurrentAdmin AdminDTO adminDTO) {
        bankCardService.deleteBankCard(id);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/hasValidCard", method = RequestMethod.GET)
    @ApiOperation("判断是否有有效的银行卡")
    public ResponseEntity hasCashAccount(@CurrentAdmin AdminDTO adminDTO) throws NoHasCashAccountException {
        JMSelfBankCashAccount jmSelfBankCashAccount = jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
        JMSelfBankCard bankCard = bankCardService.loadJMSelfBankCard();
        if (bankCard == null) {
            return new ResponseEntity(Response.error(""), HttpStatus.OK);
        } else if (bankCard.getStatus() != BankCard.Status.success.value) {
            return new ResponseEntity(Response.error(""), HttpStatus.OK);
        } else if (bankCard.getStatus() == BankCard.Status.success.value && !bankCard.getValidated()) {
            return new ResponseEntity(Response.error(""), HttpStatus.OK);
        } else if (bankCard.getStatus() == BankCard.Status.success.value && bankCard.getValidateCount() == 3) {
            return new ResponseEntity(Response.error(""), HttpStatus.OK);
        }
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }
}
