package com.jm.bid.user.account.controller;

import com.jm.bid.boot.annotation.CheckCompany;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.AuthCodeService;
import com.jm.bid.common.service.AuthCodeService.AuthCodeErrorType;
import com.jm.bid.common.service.AuthCodeType;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.UserService;
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

/**
 * Created by xiangyang on 2016/12/9.
 */

@Api(tags = "jm-user-straffManager", description = "员工管理")
@Controller
@RequestMapping("/account/staffManage")
@RequiresRoles(UserRole.ADMIN)
@CheckCompany
public class StaffManageController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthCodeService authCodeService;

    //员工管理
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation(value = "打开员工管理路由")
    public String staffManage(@CurrentUser UserDTO user, Model model) {
        model.addAttribute("employeeList", userService.loadEmployeeByCompanyId(user.getCompanyId(), UserRole.SALESMAN, UserRole.TREASURER));
        return "/partials/account/staffManage";
    }

    @RequestMapping(value = "/sendAddEmployeeAuthCode", method = RequestMethod.GET)
    @ApiOperation(value = "发送添加员工手机验证码", response = Response.class, notes = "response.error:sendAuthCode_phoneIllegal,sendAuthCode_limitation,sendAuthCode_highRate")
    public ResponseEntity staffManage(@CurrentUser UserDTO user) {
        return new ResponseEntity(authCodeService.sendVerifyCode(user.getSecurePhone(), AuthCodeType.userAddEmployee), HttpStatus.OK);
    }

    //添加员工
    @RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
    @ApiOperation(value = "处理添加员工请求", response = Response.class, notes = "response.error:securePhoneIllegal,userExisted,Auth_PhoneIllegal,Auth_CodeError,Auth_CodeExpire")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "role", value = "角色名称  业务人员:TREASURER 财务人员:SALESMAN", paramType = "form", required = true),
            @ApiImplicitParam(name = "securePhone", value = "添加用户的手机号", paramType = "form", required = true),
            @ApiImplicitParam(name = "authCode", value = "验证码", paramType = "form", required = true),
            @ApiImplicitParam(name = "userName", value = "用户名", paramType = "form", required = true)
    })
    public ResponseEntity addEmployee(@RequestParam("role") UserRole userRole, @RequestParam("userName") String userName, @RequestParam("securePhone") String securePhone, @RequestParam("authCode") String authCode, @CurrentUser UserDTO userDTO) {
        AuthCodeErrorType authCodeErrorType = null;
        if (!authCodeService.isValidPhone(securePhone)) {
            return new ResponseEntity(Response.error("securePhoneIllegal"), HttpStatus.OK);
        } else if (userService.securePhoneExists(securePhone)) {
            return new ResponseEntity(Response.error("userExisted"), HttpStatus.OK);
        } else if ((authCodeErrorType = authCodeService.validateAuthCode(userDTO.getSecurePhone(), AuthCodeType.userAddEmployee, authCode)) != AuthCodeErrorType.Auth_Success) {
            return new ResponseEntity(Response.error(authCodeErrorType.name()), HttpStatus.OK);
        } else {
            if (userService.loadEmployeeByCompanyId(userDTO.getCompanyId(), userRole).size() > 0) {
                throw new BusinessException("您的公司已经存在" + userRole.value + ",请不要重复添加!");
            }
            User user = userService.addEmployee(securePhone, userName, userRole, userDTO);
            authCodeService.updateVerifyCodeInvalid(userDTO.getSecurePhone(),authCode);
            return new ResponseEntity(Response.success().setData(user), HttpStatus.OK);
        }
    }


    //禁用员工
    @RequestMapping(value = "/forbiddenEmployee", method = RequestMethod.POST)
    @ApiOperation(value = "禁用员工", response = Response.class, notes = "response.error:userNotExists,accountLocked")
    @ApiImplicitParam(name = "userId", value = "用户id", paramType = "form", required = true)
    public ResponseEntity forbiddenEmployee(@RequestParam("userId") long userId, @CurrentUser UserDTO userDTO) {
        User user = userService.loadById(userId);
        if (user == null || !StringUtils.equals(user.getCompanyId(), userDTO.getCompanyId())) {
            return new ResponseEntity(Response.error("userNotExists"), HttpStatus.OK);
        } else if (!user.isActive()) {
            return new ResponseEntity(Response.error("accountLocked"), HttpStatus.OK);
        } else {
            userService.forbiddenUser(userId);
            return new ResponseEntity(Response.success(), HttpStatus.OK);
        }

    }

}
