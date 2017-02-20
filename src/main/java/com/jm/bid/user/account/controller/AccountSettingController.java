package com.jm.bid.user.account.controller;

import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.controller.UserSession;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.service.CashAccountService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;


/**
 * Created by xiangyang on 2016/11/28.
 */
@Api(tags = "jm-user-accountSetting")
@Controller
@RequestMapping("/account/accountSetting")
public class AccountSettingController {

    //控制接口正确按步骤请求的key
    private static final String RESET_PWD_KEY = "user_resetPwd_K";

    private static final String RESET_PAYPWD_KEY = "user_resetPayPwd_K";

    @Autowired
    private UserService userService;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private CashAccountService cashAccountService;

    //账户设置
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation(value = "打开账户设置页面", notes = "页面路由跳转-打开账户设置页面")
    public String accountSetting(@CurrentUser UserDTO user, Model model) {
        if (user.getRole() == UserRole.TREASURER) {
            try {
                cashAccountService.loadBankCashAccountByCompanyId(user.getCompanyId(), BankCashAccount.AccountType.generalAccount);
                model.addAttribute("hasCashAccount", true);
            } catch (NoHasCashAccountException e) {
                model.addAttribute("hasCashAccount", false);
            }
        }
        model.addAttribute("user", user);
        return "/partials/account/accountSetting";
    }

    //判断图形验证码是否正确
    @RequestMapping(value = "/verifyImgCode", method = RequestMethod.POST)
    @ApiOperation(value = "判断图形验证码是否正确", response = Response.class, notes = "response.error:imgCodeError")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity validationImgCode(@RequestParam("imgCode") String imgCode, @CurrentUser UserDTO userDTO) {
        if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }
    }

    //发送重置登陆密码验证码
    @RequestMapping(value = "/sendResetPwdAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送重置登陆密码手机验证码", response = Response.class, notes = "response.error:imgCodeError,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity<?> sendResetPwdAuthCode(@RequestParam("imgCode") String imgCode, @CurrentUser UserDTO userDTO) {
        if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(authCodeService.sendVerifyCode(userDTO.getSecurePhone(), AuthCodeType.userResetPwd), HttpStatus.OK);
        }
    }

    //验证重置登陆密码验证码
    @RequestMapping(value = "/verifyResetPwdAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证重置登陆密码手机验证码", response = Response.class, notes = "response.error:Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true),
            @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true)
    })
    public ResponseEntity<?> verifyResetPwdAuthCode(@RequestParam("imgCode") String imgCode, @RequestParam("authCode") String authCode, HttpSession session, @CurrentUser UserDTO userDTO) {
        AuthCodeErrorType authCodeResult = null;
        if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else if ((authCodeResult = authCodeService.validateAuthCode(userDTO.getSecurePhone(), AuthCodeType.userResetPwd, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(userDTO.getSecurePhone(), authCode);
            String k = Ids.randomBase62(10);
            session.setAttribute(RESET_PWD_KEY, k);
            userSession.removeImgCode();
            return new ResponseEntity<>(Response.success().setData(k), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/verifyOriginPwdEquals", method = RequestMethod.POST)
    @ApiOperation(value = "检查和原登录密码是否相等", response = Boolean.class, notes = "相等true,不相等返回false")
    @ApiImplicitParam(name = "pwd", value = "登录密码", paramType = "form", required = true)
    public ResponseEntity<?> verifyOriginPwdEquals(@RequestParam("pwd") String pwd, @CurrentUser UserDTO userDTO) {
        if (userService.validPasswordEquals(userDTO.getSecurePhone(), pwd)) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/resetPwd", method = RequestMethod.POST)
    @ApiOperation(value = "重置登录密码", response = Response.class, notes = "response.error:oldPwdError,prevPwdEquals")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPwd", value = "老登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "newPwd", value = "新登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "k", value = "key", paramType = "form", required = true)
    })
    public ResponseEntity<?> resetPwd(@RequestParam("oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd, @RequestParam("k") String k, HttpSession session, @CurrentUser UserDTO userDTO) {
        Object sessionK = session.getAttribute(RESET_PWD_KEY);
        if (sessionK == null || !((String) sessionK).equals(k)) {
            throw new BusinessException("非法请求");
        } else if (!userService.validPasswordEquals(userDTO.getSecurePhone(), oldPwd)) {
            return new ResponseEntity<>(Response.error("oldPwdError"), HttpStatus.OK);
        } else if (userService.validPasswordEquals(userDTO.getSecurePhone(), newPwd)) {
            return new ResponseEntity<>(Response.error("prevPwdEquals"), HttpStatus.OK);
        } else {
            userService.updatePassword(userDTO.getSecurePhone(), newPwd);
            session.removeAttribute(RESET_PWD_KEY);
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }
    }


    //发送重置支付密码验证码
    @RequiresRoles(UserRole.TREASURER)
    @RequestMapping(value = "/sendResetPayPwdAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送重置支付密码手机验证码", response = Response.class, notes = "response.error:imgCodeError,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity<?> sendResetPayPwdAuthCode(@RequestParam("imgCode") String imgCode, @CurrentUser UserDTO adminDTO) {
        if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.userResetPayPwd), HttpStatus.OK);
        }

    }

    //验证重置支付验证码
    @RequiresRoles(UserRole.TREASURER)
    @RequestMapping(value = "/verifyResetPayPwdAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证重置支付密码手机验证码", response = Response.class, notes = "response.error:imgCodeError,Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true),
            @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true)
    })
    public ResponseEntity<?> verifyResetPayPwdAuthCode(@RequestParam("imgCode") String imgCode, @RequestParam("authCode") String authCode, HttpSession session, @CurrentUser UserDTO adminDTO) {
        AuthCodeErrorType authCodeResult = null;
        if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else if ((authCodeResult = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.userResetPayPwd, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(adminDTO.getSecurePhone(), authCode);
            String k = Ids.randomBase62(10);
            session.setAttribute(RESET_PAYPWD_KEY, k);
            userSession.removeImgCode();
            return new ResponseEntity<>(Response.success().setData(k), HttpStatus.OK);
        }
    }

    @RequiresRoles(UserRole.TREASURER)
    @RequestMapping(value = "/verifyOriginPayPwdEquals", method = RequestMethod.POST)
    @ApiOperation(value = "检查和原支付密码是否相等", response = Boolean.class, notes = "相等true,不相等返回false")
    @ApiImplicitParam(name = "pwd", value = "登录密码", paramType = "form", required = true)
    public ResponseEntity<?> verifyOriginPayPwdEquals(@RequestParam("pwd") String pwd, @CurrentUser UserDTO userDTO) {
        if (userService.validPayPasswordEquals(userDTO.getSecurePhone(), pwd)) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    @RequiresRoles(UserRole.TREASURER)
    @RequestMapping(value = "/resetPayPwd", method = RequestMethod.POST)
    @ApiOperation(value = "处理重置支付密码", response = Response.class, notes = "response.error:oldPwdError,prevPwdEquals")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPwd", value = "老登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "newPwd", value = "新登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "k", value = "key", paramType = "form", required = true)
    })
    public ResponseEntity<?> resetPayPwd(@RequestParam("oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd, @RequestParam("k") String k, HttpSession session, @CurrentUser UserDTO userDTO) {
        Object sessionK = session.getAttribute(RESET_PAYPWD_KEY);
        if (sessionK == null || !((String) sessionK).equals(k)) {
            throw new BusinessException("非法请求");
        } else if (!userService.validPayPasswordEquals(userDTO.getSecurePhone(), oldPwd)) {
            return new ResponseEntity<>(Response.error("oldPwdError"), HttpStatus.OK);
        } else if (userService.validPayPasswordEquals(userDTO.getSecurePhone(), newPwd)) {
            return new ResponseEntity<>(Response.error("prevPwdEquals"), HttpStatus.OK);
        } else {
            userService.updatePayPassword(userDTO.getSecurePhone(), newPwd);
            session.removeAttribute(RESET_PAYPWD_KEY);
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }
    }


}
