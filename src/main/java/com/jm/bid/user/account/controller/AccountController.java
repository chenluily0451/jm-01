package com.jm.bid.user.account.controller;

import com.jm.bid.boot.annotation.CheckCompany;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.user.account.dto.UserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by xiangyang on 2016/11/28.
 */
@Api(tags = "jm-user-account")
@Controller
@RequestMapping("/account")
@CheckCompany
public class AccountController {


    //公司信息
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-跳转到我的账户")
    public String accountIndex(@CurrentUser UserDTO user, Model model) {
        if (user.getRole() == UserRole.TREASURER) {
            return "redirect:/account/finance";
        } else if (user.getRole() == UserRole.SALESMAN) {
            return "redirect:/account/bid";
        } else {
            return "redirect:/account/company";
        }

    }


}
