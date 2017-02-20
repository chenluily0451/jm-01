package com.jm.bid.user.finance.controller;

import com.jm.bid.boot.annotation.CheckCompany;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.restpay.RestPayClient;
import com.jm.bid.restpay.dto.CashAccountDTO;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.finance.entity.BankCard;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.service.BankCardService;
import com.jm.bid.user.finance.service.CashAccountService;
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
 * Created by xiangyang on 2016/12/15.
 */
@Api("jm-finance-asset")
@Controller
@RequestMapping("/account/finance/asset")
@RequiresRoles(UserRole.TREASURER)
@CheckCompany
public class AssetController {

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private RestPayClient restPayClient;

    //公司资产
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation("资金账户详情,绑定银行卡路由")
    public String myBankCard(@CurrentUser UserDTO userDTO, Model model) throws NoHasCashAccountException {
        model.addAttribute("bankCashAccount", cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount));
        model.addAttribute("bankCard", bankCardService.loadCardByCompanyId(userDTO.getCompanyId()));
        model.addAttribute("user", userDTO);
        return "/partials/pay/asset";
    }

    //公司资产
    @RequestMapping(value = "/queryAccountBalance", method = RequestMethod.GET)
    @ApiOperation(value = "查询资金账户余额", response = CashAccountDTO.class)
    public ResponseEntity<CashAccountDTO> queryCashAccountBalance(@CurrentUser UserDTO userDTO) throws NoHasCashAccountException {
        BankCashAccount bankCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        if (bankCashAccount == null) {
            return new ResponseEntity(Response.error("noOpenCashAccount"), HttpStatus.OK);
        }
        CashAccountDTO cashAccountDTO = restPayClient.queryCashAccountBalance(bankCashAccount.getAccId(), bankCashAccount.getBankAccountNum());
        if(!cashAccountDTO.isSuccess()){
            return new ResponseEntity<CashAccountDTO>(HttpStatus.SERVICE_UNAVAILABLE);
        }else{
            return new ResponseEntity<CashAccountDTO>(cashAccountDTO,HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/sendBindCardAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送绑定银行卡验证码", notes = "response.error:sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    public ResponseEntity<Response> sendBindBankCardVerifyCode(@CurrentUser UserDTO userDTO) {
        return new ResponseEntity<>(authCodeService.sendVerifyCode(userDTO.getSecurePhone(), AuthCodeType.userBindBankCard), HttpStatus.OK);
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
                                                 @CurrentUser UserDTO userDTO) {

        AuthCodeService.AuthCodeErrorType authCodeErrorType = authCodeService.validateAuthCode(userDTO.getSecurePhone(), AuthCodeType.userBindBankCard, authCode);
        if (authCodeErrorType != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<Response>(Response.error(authCodeErrorType.name()), HttpStatus.OK);
        } else {
            BankCard bankCard = new BankCard();
            bankCard.setCardNum(cardNum);
            bankCard.setCardAccountName(cardAccountName);
            bankCard.setChildBankCode(childBankCode);
            bankCardService.addBindCard(bankCard, userDTO);
            authCodeService.updateVerifyCodeInvalid(userDTO.getSecurePhone(),authCode);
            return new ResponseEntity<Response>(Response.success(), HttpStatus.OK);
        }

    }


    @RequestMapping(value = "/validateBankCard", method = RequestMethod.POST)
    @ApiOperation(value = "处理验证提现银行卡请求", notes = "response.error= authFail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", paramType = "form", required = true),
            @ApiImplicitParam(name = "amount", value = "验证金额", paramType = "form", required = true)
    })
    public ResponseEntity<Response> validateBindCard(@RequestParam("id") long id, @RequestParam("amount") BigDecimal amount, @CurrentUser UserDTO userDTO) {
        BankCard bankCard = bankCardService.selectByIdAndCompanyId(id, userDTO.getCompanyId());
        if (bankCard == null || bankCard.getStatus() != BankCard.Status.success.value) {
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
    public ResponseEntity<Response> deleteBankCard(@RequestParam("id") long id, @CurrentUser UserDTO userDTO) {
        bankCardService.deleteBankCard(id, userDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/hasValidCard", method = RequestMethod.GET)
    @ApiOperation("判断是否有有效的银行卡")
    public ResponseEntity hasCashAccount(@CurrentUser UserDTO userDTO) throws NoHasCashAccountException {
        BankCashAccount bankCashAccount = cashAccountService.loadBankCashAccountByCompanyId(userDTO.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        BankCard bankCard = bankCardService.loadCardByCompanyId(userDTO.getCompanyId());
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
