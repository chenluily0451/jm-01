package com.jm.bid.user.account.controller;

import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.controller.UserSession;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.common.service.FindPwdLogService;
import com.jm.bid.common.entity.FindPwdLog;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by xiangyang on 16/8/17.
 */
@Api(tags = "jm-user-regainPwd")
@Controller
@RequestMapping("/regainPwd")
public class RegainPasswordController {

    @Autowired
    private UserSession session;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private FindPwdLogService findPwdLogService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation(value = "(1)页面跳转路由-验证账号")
    public String regainIdentity() {
        return "/partials/regainpwd/regainPwd";
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "(1)验证账号", response = Response.class, notes = "response.error:userExisted,imgCodeError,accountLocked")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "securePhone", value = "手机号码", paramType = "form", required = true),
            @ApiImplicitParam(name = "imgCode", value = "图片验证码", paramType = "form", required = true)
    })
    public ResponseEntity regainIdentity(@RequestParam("securePhone") String securePhone, @RequestParam("imgCode") String imgCode) {
        User user = userService.loadBySecurePhone(securePhone);
        if (!StringUtils.contains(session.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else if (user == null) {
            return new ResponseEntity<>(Response.error("userNotExists"), HttpStatus.OK);
        } else if (!user.isActive()) {
            return new ResponseEntity<>(Response.error("accountLocked"), HttpStatus.OK);
        } else {
            String UUId = findPwdLogService.addFindPwdLog(securePhone);
            userSession.removeImgCode();
            return new ResponseEntity<>(Response.success().setData(UUId), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/regainIdentity", method = RequestMethod.GET)
    @ApiOperation(value = "(2)页面跳转路由-验证手机验证码", notes = "打开页面路由-验证手机验证码")
    public String regainPwd(@RequestParam("k") String uuId, Model model) {
        FindPwdLog log = findPwdLogService.loadFindPwdLogByUUId(uuId);
        if (log == null) {
            throw new NotFoundException("访问的网页不存在");
        }
        model.addAttribute("k", uuId);
        model.addAttribute("securePhone", log.getSecurePhone());
        return "/partials/regainpwd/regainIdentity";
    }

    @RequestMapping(value = "/sendRegainPwdAuthCode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "(2)发送找回密码手机验证码", response = Response.class, notes = "response.error:userNotExists,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParam(name = "k", value = "参数key", required = true)
    public ResponseEntity<?> sendCustomerVerifyCode(@RequestParam("k") String uuId) {
        FindPwdLog log = findPwdLogService.loadFindPwdLogByUUId(uuId);
        if (log == null) {
            return new ResponseEntity(Response.error("userNotExists"), HttpStatus.OK);
        }
        return new ResponseEntity<>(authCodeService.sendVerifyCode(log.getSecurePhone(), AuthCodeType.regainPwd), HttpStatus.OK);
    }

    @RequestMapping(value = "/regainIdentity", method = RequestMethod.POST)
    @ApiOperation(value = "(2)验证找回密码手机验证码", response = String.class, notes = "验证找回密码手机验证码,responseText:userNotExists、Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire,success")
    @ApiImplicitParam(name = "k", value = "找回密码key", required = true)
    public ResponseEntity regainPwd(@RequestParam("k") String uuId, String authCode) {
        FindPwdLog log = findPwdLogService.loadFindPwdLogByUUId(uuId);
        final AuthCodeErrorType authCodeErrorType;
        if (log == null) {
            return new ResponseEntity("userNotExists", HttpStatus.OK);
        } else if ((authCodeErrorType = authCodeService.validateAuthCode(log.getSecurePhone(), AuthCodeType.regainPwd, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity(authCodeErrorType.name(), HttpStatus.OK);
        } else {
            authCodeService.updateVerifyCodeInvalid(log.getSecurePhone(), authCode);
            return new ResponseEntity("success", HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/resetPwd", method = RequestMethod.GET)
    @ApiOperation(value = "(3)页面跳转路由-重定向重置密码页面", notes = "(3)页面跳转路由-重定向重置密码页面")
    public String resetPwd(@RequestParam("k") String uuId, Model model) {
        FindPwdLog log = findPwdLogService.loadFindPwdLogByUUId(uuId);
        if (log == null) {
            throw new NotFoundException("访问的网页不存在");
        }
        model.addAttribute("k", uuId);
        model.addAttribute("securePhone", log.getSecurePhone());
        return "/partials/regainpwd/resetPwd";
    }

    @RequestMapping(value = "/resetPwd", method = RequestMethod.POST)
    @ApiOperation(value = "(3)处理重置密码请求", response = String.class, notes = "response.error:userNotExists,passWordError,prevPasswordEquals")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "k", value = "key", required = true),
            @ApiImplicitParam(name = "plainPassword", value = "密码", required = true),
            @ApiImplicitParam(name = "confirmPassword", value = "确认密码", required = true),
    })
    public ResponseEntity resetPwd(@RequestParam("k") String uuId, @RequestParam("plainPassword") String plainPassword, @RequestParam("confirmPassword") String confirmPassword) {
        FindPwdLog log = findPwdLogService.loadFindPwdLogByUUId(uuId);
        if (log == null) {
            return new ResponseEntity("userNotExists", HttpStatus.OK);
        }
        if (!StringUtils.equals(plainPassword, confirmPassword)) {
            return new ResponseEntity("passWordError", HttpStatus.OK);
        }
        if (userService.validPasswordEquals(log.getSecurePhone(), plainPassword)) {
            return new ResponseEntity("prevPasswordEquals", HttpStatus.OK);
        }
        userService.updatePassword(log.getSecurePhone(), plainPassword);
        findPwdLogService.updateFindPwdLogSuccess(uuId);
        return new ResponseEntity("success", HttpStatus.OK);
    }

    @RequestMapping(value = "/resetPwdSuccess", method = RequestMethod.GET)
    @ApiOperation(value = "(4)页面跳转路由-修改密码成功页面", notes = "(4)页面跳转路由-修改密码成功页面")
    public String resetPwdSuccess() {
        return "/partials/regainpwd/resetPwdSuccess";
    }


}
