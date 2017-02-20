package com.jm.bid.user.account.controller;

import com.jm.bid.boot.util.BeanMapper;
import com.jm.bid.common.controller.UserSession;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.CompanyService;
import com.jm.bid.user.account.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Created by xiangyang on 16/8/17.
 */
@Api(tags = "jm-user-login", description = "用户登录")
@Controller
public class LoginController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation(value = "登陆页面", notes = "跳转路由-跳转登陆页面,如果已经登陆跳转到首页")
    public String login(HttpServletRequest request) {
        if (userSession.isLogined()) {
            return "redirect:/";
        }
        return "/partials/login/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "处理登录请求", notes = "响应结果: userNotExists、passwordError、success")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "登录用户名", paramType = "form", required = true),
            @ApiImplicitParam(name = "plainPassword", value = "登录密码", paramType = "form", required = true)
    })
    public ResponseEntity<String> login(@RequestParam("userName") String userName, @RequestParam("plainPassword") String plainPassword) {
        User user = userService.loadBySecurePhone(userName);
        if (user == null) {
            return new ResponseEntity("userNotExists", HttpStatus.OK);
        } else if (!userService.validPasswordEquals(user.getSecurePhone(), plainPassword)) {
            return new ResponseEntity("passwordError", HttpStatus.OK);
        } else if (!user.isActive()) {
            return new ResponseEntity("accountLocked", HttpStatus.OK);
        } else {
            UserDTO userDTO = BeanMapper.map(user, UserDTO.class);
            Optional.ofNullable(companyService.loadLastCompanyByUUID(user.getCompanyId()))
                    .ifPresent(c -> {
                        userDTO.setCompanyId(c.getUUID());
                        userDTO.setCompanyName(c.getName());
                    });
            userSession.login(userDTO);
            return new ResponseEntity("success", HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation(value = "登出系统", notes = "登出当前用户,跳转到登录页面")
    public String logout() {
        userSession.logout();
        return "redirect:/login";
    }


}
