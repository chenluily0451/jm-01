package com.jm.bid.admin.account.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.entity.Admin;
import com.jm.bid.admin.account.service.AdminService;
import com.jm.bid.boot.util.BeanMapper;
import com.jm.bid.common.controller.AdminSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by xiangyang on 2016/11/22.
 */
@Api(tags = "jm-admin-login", description = "admin登录")
@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private AdminSession adminSession;

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ApiOperation(value = "admin登陆页面", notes = "跳转路由-跳转登陆页面,如果已经登陆跳转到admin首页")
    public String adminLogin(HttpServletRequest request) {
        if (adminSession.isLogined()) {
            return "redirect:/admin/";
        }
        return "/admin/account/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "处理登录请求", notes = "响应结果: userNotExists、passwordError、success")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "登录用户名", paramType = "form", required = true),
            @ApiImplicitParam(name = "plainPassword", value = "登录密码", paramType = "form", required = true)
    })
    public ResponseEntity adminLogin(String userName, String plainPassword) {
        Admin admin = adminService.loadBySecurePhone(userName);
        if (admin == null) {
            return new ResponseEntity("userNotExists", HttpStatus.OK);
        } else if (!adminService.validPasswordEquals(admin.getSecurePhone(), plainPassword)) {
            return new ResponseEntity("passwordError", HttpStatus.OK);
        } else if (!admin.isActive()) {
            return new ResponseEntity("accountLocked", HttpStatus.OK);
        } else {
            adminSession.login(BeanMapper.map(admin, AdminDTO.class));
            return new ResponseEntity("success", HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ApiOperation(value = "admin登出系统", notes = "登出当前用户,跳转到admin登录页面")
    public String adminLogout() {
        adminSession.logout();
        return "redirect:/admin/login";
    }

}
