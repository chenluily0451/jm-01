package com.jm.bid.admin.account.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.service.AdminService;
import com.jm.bid.admin.finance.entity.JMSelfBankCashAccount;
import com.jm.bid.admin.finance.service.JMSelfCashAccountService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.Consts;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.controller.AdminSession;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.user.finance.entity.BankCashAccount;
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
@Api(tags = "jm-admin-accountSetting")
@Controller
@RequestMapping("/admin/accountSetting")
public class AdminAccountSettingController {

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminSession adminSession;

    @Autowired
    private JMSelfCashAccountService jmSelfCashAccountService;

    //控制接口正确按步骤请求的key
    private static final String RESET_PWD_KEY = "admin_resetPwd_K";

    private static final String RESET_PAYPWD_KEY = "admin_resetPayPwd_K";

    private static final String BIND_OLDPHONE_AUTH_KEY = "admin_oldPhoneAuth_K";


    //账户设置
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation(value = "admin打开账户设置页面")
    public String accountSetting(@CurrentAdmin AdminDTO adminDTO, Model model) {
        if (adminDTO.getRole() == UserRole.ADMIN_TREASURER) {
            try {
                jmSelfCashAccountService.loadJMCashAccount(JMSelfBankCashAccount.JMCashAccountType.generalAccount, Consts.BankType.ZXBank);
                model.addAttribute("hasCashAccount", true);
            } catch (NoHasCashAccountException e) {
                model.addAttribute("hasCashAccount", false);
            }
        }
        model.addAttribute("admin", adminDTO);
        return "/admin/account/accountSetting";
    }

    //判断图形验证码是否正确
    @RequestMapping(value = "/verifyImgCode", method = RequestMethod.POST)
    @ApiOperation(value = "判断图形验证码是否正确", response = Response.class, notes = "response.error:imgCodeError")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity validationImgCode(@RequestParam(value = "imgCode") String imgCode, @CurrentAdmin AdminDTO adminDTO) {
        if (StringUtils.isAnyEmpty(imgCode, adminSession.getImgCode()) || !StringUtils.equals(adminSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }
    }

    //重置登陆密码
    @RequestMapping(value = "/sendResetPwdAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送重置登陆密码手机验证码", response = Response.class, notes = "response.error:imgCodeError,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity<?> sendResetPwdAuthCode(@RequestParam(value = "imgCode") String imgCode, @CurrentAdmin AdminDTO adminDTO) {
        if (StringUtils.isAnyEmpty(imgCode, adminSession.getImgCode()) || !StringUtils.equals(adminSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminResetPwd), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/verifyResetPwdAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证重置登陆密码手机验证码", response = Response.class, notes = "response.error:Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true)
    public ResponseEntity<?> verifyResetPwdAuthCode(@RequestParam(value = "authCode") String authCode, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        final AuthCodeErrorType authCodeResult = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.adminResetPwd, authCode);
        if (authCodeResult != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(adminDTO.getSecurePhone(), authCode);
            adminSession.removeImgCode();
            String k = Ids.randomBase62(10);
            session.setAttribute(RESET_PWD_KEY, k);
            return new ResponseEntity<>(Response.success().setData(k), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/verifyOriginPwdEquals", method = RequestMethod.POST)
    @ApiOperation(value = "检查和原登录密码是否相等", response = Boolean.class, notes = "相等true,不相等返回false")
    @ApiImplicitParam(name = "pwd", value = "登录密码", paramType = "form", required = true)
    public ResponseEntity<?> verifyOriginPwdEquals(@RequestParam("pwd") String pwd, @CurrentAdmin AdminDTO adminDTO) {
        if (adminService.validPasswordEquals(adminDTO.getSecurePhone(), pwd)) {
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
    public ResponseEntity<?> resetPwd(@RequestParam("oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd, @RequestParam("k") String k, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        Object sessionK = session.getAttribute(RESET_PWD_KEY);
        if (sessionK == null || !((String) sessionK).equals(k)) {
            throw new BusinessException("非法请求");
        } else if (!adminService.validPasswordEquals(adminDTO.getSecurePhone(), oldPwd)) {
            return new ResponseEntity<>(Response.error("oldPwdError"), HttpStatus.OK);
        } else if (adminService.validPasswordEquals(adminDTO.getSecurePhone(), newPwd)) {
            return new ResponseEntity<>(Response.error("prevPwdEquals"), HttpStatus.OK);
        } else {
            adminService.updatePassword(adminDTO.getSecurePhone(), newPwd);
            session.removeAttribute(RESET_PWD_KEY);
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }
    }


    //重置支付密码
    @RequestMapping(value = "/sendResetPayPwdAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送重置支付密码手机验证码", response = Response.class, notes = "response.error:imgCodeError,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity<?> sendResetPayPwdAuthCode(@RequestParam("imgCode") String imgCode, @CurrentAdmin AdminDTO adminDTO) {
        if (StringUtils.isAnyEmpty(imgCode, adminSession.getImgCode()) || !StringUtils.equals(adminSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminResetPayPwd), HttpStatus.OK);
        }

    }

    @RequestMapping(value = "/verifyResetPayPwdAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证重置支付密码手机验证码", response = Response.class, notes = "response.error:Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParam(name = "authCode", value = "手机验证码", required = true)
    public ResponseEntity<?> verifyResetPayPwdAuthCode(@RequestParam("authCode") String authCode, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        AuthCodeService.AuthCodeErrorType authCodeResult = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.adminResetPayPwd, authCode);
        if (authCodeResult != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(adminDTO.getSecurePhone(), authCode);
            String k = Ids.randomBase62(10);
            session.setAttribute(RESET_PAYPWD_KEY, k);
            adminSession.removeImgCode();
            return new ResponseEntity<>(Response.success().setData(k), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/verifyOriginPayPwdEquals", method = RequestMethod.POST)
    @ApiOperation(value = "检查和原支付密码是否相等", response = Boolean.class, notes = "相等true,不相等返回false")
    @ApiImplicitParam(name = "pwd", value = "登录密码", paramType = "form", required = true)
    public ResponseEntity<?> verifyOriginPayPwdEquals(@RequestParam("pwd") String pwd, @CurrentAdmin AdminDTO adminDTO) {
        if (adminService.validPayPasswordEquals(adminDTO.getSecurePhone(), pwd)) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/resetPayPwd", method = RequestMethod.POST)
    @ApiOperation(value = "处理重置支付密码", response = Response.class, notes = "response.error:oldPwdError,prevPwdEquals")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPwd", value = "老登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "newPwd", value = "新登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "k", value = "key", paramType = "form", required = true)
    })
    public ResponseEntity<?> resetPayPwd(@RequestParam("oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd, @RequestParam("k") String k, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        Object sessionK = session.getAttribute(RESET_PAYPWD_KEY);
        if (sessionK == null || !((String) sessionK).equals(k)) {
            throw new BusinessException("非法请求");
        } else if (!adminService.validPayPasswordEquals(adminDTO.getSecurePhone(), oldPwd)) {
            return new ResponseEntity<>(Response.error("oldPwdError"), HttpStatus.OK);
        } else if (adminService.validPayPasswordEquals(adminDTO.getSecurePhone(), newPwd)) {
            return new ResponseEntity<>(Response.error("prevPwdEquals"), HttpStatus.OK);
        } else {
            adminService.updatePayPassword(adminDTO.getSecurePhone(), newPwd);
            session.removeAttribute(RESET_PAYPWD_KEY);
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }
    }

    //更换手机号开始
    @RequestMapping(value = "/sendOldPhoneAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送更换手机号手机验证码", response = Response.class, notes = "response.error:imgCodeError,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true)
    public ResponseEntity<?> sendOldPhoneVerifyCode(@RequestParam("imgCode") String imgCode, @CurrentAdmin AdminDTO adminDTO) {
        if (StringUtils.isAnyEmpty(imgCode, adminSession.getImgCode()) || !StringUtils.equals(adminSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(authCodeService.sendVerifyCode(adminDTO.getSecurePhone(), AuthCodeType.adminResetBindOldPhone), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/verifyOldPhoneAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证老手机号验证码", response = Response.class, notes = "response.error:Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParam(name = "手机验证码", value = "authCode", required = true)
    public ResponseEntity<?> verifyOldPhoneVerifyCode(@RequestParam("authCode") String authCode, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        AuthCodeService.AuthCodeErrorType authCodeResult = authCodeService.validateAuthCode(adminDTO.getSecurePhone(), AuthCodeType.adminResetBindOldPhone, authCode);
        if (authCodeResult != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(adminDTO.getSecurePhone(), authCode);
            String k = Ids.randomBase62(10);
            session.setAttribute(BIND_OLDPHONE_AUTH_KEY, k);
            adminSession.removeImgCode();
            return new ResponseEntity<>(Response.success().setData(k), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/sendNewPhoneAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送新手机号手机验证码", response = Response.class, notes = "response.error:imgCodeError,securePhoneExists,oldPhoneEquals,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true),
            @ApiImplicitParam(name = "newPhone", value = "新手机号", required = true),
            @ApiImplicitParam(name = "k", value = "key", required = true)
    })
    public ResponseEntity<?> sendNewPhoneVerifyCode(@RequestParam("imgCode") String imgCode, @RequestParam("newPhone") String newPhone, @RequestParam("k") String k, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        Object sessionK = session.getAttribute(BIND_OLDPHONE_AUTH_KEY);
        if (sessionK == null || !((String) sessionK).equals(k)) {
            throw new BusinessException("非法请求");
        } else if (StringUtils.isAnyEmpty(imgCode, adminSession.getImgCode()) || !StringUtils.equals(adminSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else if (newPhone.equals(adminDTO.getSecurePhone())) {
            return new ResponseEntity<>(Response.error("oldPhoneEquals"), HttpStatus.OK);
        } else {
            Response response = authCodeService.sendVerifyCode(newPhone, AuthCodeType.adminResetBindNewPhone);
            return new ResponseEntity<Object>(response.isSuccess() ? response.setData(sessionK) : response, HttpStatus.OK);

        }
    }

    @RequestMapping(value = "/securePhoneExits", method = RequestMethod.POST)
    @ApiOperation(value = "admin检查手机号存在", response = Boolean.class, notes = "名称存在返回true,不存在返回false")
    @ApiImplicitParam(name = "securePhone", value = "手机号码", paramType = "form", required = true)
    public ResponseEntity<?> securePhoneExits(@RequestParam("phone") String phone, @CurrentAdmin AdminDTO adminDTO) {
        if (adminService.loadBySecurePhone(phone) != null) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/verifyNewPhoneAuthCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证新手机号手机验证码", response = Response.class, notes = "response.error:Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newPhone", value = "新手机号", required = true),
            @ApiImplicitParam(name = "authCode", value = "验证码", required = true),
            @ApiImplicitParam(name = "k", value = "key", required = true)
    })
    public ResponseEntity<?> verifyNewPhoneVerifyCode(@RequestParam("newPhone") String newPhone, @RequestParam("authCode") String authCode, @RequestParam("k") String k, HttpSession session, @CurrentAdmin AdminDTO adminDTO) {
        Object sessionK = session.getAttribute(BIND_OLDPHONE_AUTH_KEY);
        AuthCodeErrorType authCodeResult = authCodeService.validateAuthCode(newPhone, AuthCodeType.adminResetBindNewPhone, authCode);
        if (sessionK == null || !((String) sessionK).equals(k)) {
            throw new BusinessException("非法请求");
        } else if (authCodeResult != AuthCodeService.AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity<>(Response.error(authCodeResult.name()), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(newPhone, authCode);
            adminService.updatePhone(newPhone, adminDTO.getSecurePhone());
            adminSession.logout();
            session.removeAttribute(BIND_OLDPHONE_AUTH_KEY);
            adminSession.removeImgCode();
            return new ResponseEntity<>(Response.success(), HttpStatus.OK);
        }

    }


}
