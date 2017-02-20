package com.jm.bid.user.account.controller;

import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.util.BeanMapper;
import com.jm.bid.boot.util.JsonMapper;
import com.jm.bid.boot.util.LocalStorage;
import com.jm.bid.boot.util.StoreUtil;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.controller.UserSession;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.Company;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.CompanyService;
import com.jm.bid.user.account.service.UserService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiangyang on 2016/11/14.
 */
@Api(tags = "jm-user-register", description = "用户注册")
@Controller
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private UserSession userSession;

    @Autowired
    private LocalStorage localStorage;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation(value = "页面跳转路由-跳转注册页面", notes = "页面跳转路由-跳转注册页面")
    public String register() {
        return "/partials/register/register";
    }

    @RequestMapping(value = "/checkSecurePhoneExists", method = RequestMethod.POST)
    @ApiOperation(value = "检查手机号存在", response = Boolean.class, notes = "名称存在返回true,不存在返回false")
    @ApiImplicitParam(name = "securePhone", value = "手机号码", paramType = "form", required = true)
    public ResponseEntity<?> checkSecurePhoneExits(@RequestParam("securePhone") String securePhone) {
        if (StringUtils.isEmpty(securePhone) || userService.securePhoneExists(securePhone)) {
            return new ResponseEntity(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/checkCompanyNameExists", method = RequestMethod.POST)
    @ApiOperation(value = "检查公司名称存在", response = Boolean.class, notes = "名称存在返回true,不存在返回false")
    public ResponseEntity registerCompany(@RequestParam("companyName") String companyName) {
        if (companyService.checkCompanyNameExists(companyName)) {
            return new ResponseEntity(true, HttpStatus.OK);
        }
        return new ResponseEntity(false, HttpStatus.OK);
    }

    @RequestMapping(value = "/sendRegisterAuthCode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "发送用户注册验证码", response = Response.class, notes = "response.error取值:userExisted,imgCodeError,sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "securePhone", value = "手机号码", paramType = "form", required = true),
            @ApiImplicitParam(name = "imgCode", value = "图片验证码", paramType = "form", required = true)
    })
    public ResponseEntity<?> sendCustomerVerifyCode(@RequestParam("securePhone") String securePhone, @RequestParam("imgCode") String imgCode) {
        if (userService.securePhoneExists(securePhone)) {
            logger.warn("手机号:{}已经存在,攻击发送注册验证码", securePhone);
            return new ResponseEntity<>(Response.error("userExisted"), HttpStatus.OK);
        } else if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity<>(Response.error("imgCodeError"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(authCodeService.sendVerifyCode(securePhone, AuthCodeType.register), HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "处理注册请求", response = String.class, notes = "responseText:securePhoneExists,pwdError,imgCodeError,Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire,success")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "securePhone", value = "手机号码", paramType = "form", required = true),
            @ApiImplicitParam(name = "password", value = "登陆密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "confirmPassword", value = "确认密码", paramType = "form", required = true),
            @ApiImplicitParam(name = "authCode", value = "手机验证码", paramType = "form", required = true),
            @ApiImplicitParam(name = "imgCode", value = "图片验证码", paramType = "form", required = true)
    })
    public ResponseEntity register(@RequestParam("securePhone") String securePhone, @RequestParam("password") String plainPassword, @RequestParam("confirmPassword") String plainConfirmPassword, @RequestParam("authCode") String authCode, @RequestParam("imgCode") String imgCode) {
        if (StringUtils.isAnyEmpty(imgCode, userSession.getImgCode()) || !StringUtils.equals(userSession.getImgCode(), imgCode)) {
            return new ResponseEntity("imgCodeError", HttpStatus.OK);
        } else if (userService.securePhoneExists(securePhone)) {
            return new ResponseEntity("securePhoneExists", HttpStatus.OK);
        } else if (!StringUtils.equals(plainPassword, plainConfirmPassword)) {
            return new ResponseEntity<>("pwdError", HttpStatus.OK);
        } else if (authCodeService.validateAuthCode(securePhone, AuthCodeType.register, authCode) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity(authCodeService.validateAuthCode(securePhone, AuthCodeType.register, authCode).name(), HttpStatus.OK);
        } else {
            User user = userService.registerUser(securePhone, plainPassword);
            userSession.login(BeanMapper.map(user, UserDTO.class));
            userSession.removeImgCode();
            authCodeService.updateVerifyCodeInvalid(securePhone, authCode);
            return new ResponseEntity("success", HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/registerCompany", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation(value = "页面跳转路由-跳转注册公司页面", notes = "页面跳转路由-跳转注册公司页面")
    public String registerCompany(@CurrentUser UserDTO userDTO) {
        if (userDTO.getCompanyId() != null) {
            throw new NotFoundException();
        }
        return "/partials/register/registerCompany";
    }


    @RequestMapping(value = "/registerCompany", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "处理注册公司请求", response = String.class, notes = "responseText:companyNameExists,success")
    public ResponseEntity registerCompany(@ApiParam(name = "company", value = "公司详情") @Validated @RequestBody Company company, @CurrentUser UserDTO userDTO) {
        if (companyService.checkCompanyNameExists(company.getName())) {
            return new ResponseEntity("companyNameExists", HttpStatus.OK);
        }
        companyService.registerCompany(company, userDTO);
        userSession.logout();
        return new ResponseEntity("success", HttpStatus.OK);
    }


    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "公司上传证件")
    public void saveFile(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException, InterruptedException {
        final BigInteger maxFileSize = BigInteger.valueOf(30);
        Map<String, String> maps = new HashMap();
        if (StoreUtil.calcFileSize(file.getSize(), StoreUtil.FileSizeUnits.MB).compareTo(maxFileSize) >= 1) {
            maps.put("error", "fileTooLarge");
            response.getWriter().write(JsonMapper.nonDefaultMapper().toJson(maps));
            return;
        } else {
            String filePath = "/files/upload/" + StoreUtil.save(localStorage, file, "/upload/");
            maps.put("filePath", filePath);
            maps.put("fileName", file.getOriginalFilename());
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(JsonMapper.nonDefaultMapper().toJson(maps));
    }


    @RequestMapping(value = "/registerSuccess", method = RequestMethod.GET)
    @ApiOperation(value = "注册成功页面", notes = "页面跳转路由-跳转注册成功页面")
    public String registerSuccess() {
        return "/partials/register/registerSuccess";
    }


}
