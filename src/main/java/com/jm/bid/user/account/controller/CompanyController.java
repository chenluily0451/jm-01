package com.jm.bid.user.account.controller;

import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.Company;
import com.jm.bid.user.account.service.CompanyService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by xiangyang on 2016/12/15.
 */
@Api(tags = "jm-user-account")
@Controller
@RequestMapping("/account")
@RequiresRoles(UserRole.ADMIN)
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    //公司信息
    @RequestMapping(value = "/company", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-我的公司详情页面", notes = "路由跳转-我的公司详情页面")
    @RequiresRoles({UserRole.TREASURER, UserRole.SALESMAN})
    public String myCompany(@CurrentUser UserDTO user, Model model) {
        if (user.getCompanyId() != null) {
            model.addAttribute("company", companyService.loadLastCompanyByUUID(user.getCompanyId()));
            return "/partials/account/companyView";
        } else {
            return "redirect:/account/updateCompany";
        }
    }

    @RequestMapping(value = "/updateCompany", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-公司注册页面", notes = "路由跳转-公司注册页面")
    public String update(@CurrentUser UserDTO user, Model model) {
        if (user.getCompanyId() != null) {
            model.addAttribute("company", companyService.loadLastCompanyByUUID(user.getCompanyId()));
        }
        return "/partials/account/myCompany";
    }

    @RequestMapping(value = "/checkCompanyNameExists", method = RequestMethod.POST)
    @ApiOperation(value = "检查公司名称是否存在", response = Boolean.class, notes = "true 存在 false 不存在")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公司记录的UUID", required = true),
            @ApiImplicitParam(name = "companyName", value = "公司名称", required = true)
    })
    public ResponseEntity registerCompany(@RequestParam("id") String UUID, @RequestParam("companyName") String companyName) {
        if (StringUtils.isEmpty(UUID) || StringUtils.isEmpty(companyName)) {
            return new ResponseEntity(false, HttpStatus.OK);
        } else {
            return new ResponseEntity(companyService.checkCompanyNameExists2(UUID, companyName), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/updateCompany", method = RequestMethod.POST)
    @ApiOperation(value = "处理修改公司信息请求", response = String.class, notes = "success")
    public ResponseEntity<?> updateCompany(@ApiParam(name = "company", value = "公司详情") @Validated @RequestBody Company company, @CurrentUser UserDTO user) {
        if (user.getCompanyId() == null) {
            throw new BusinessException("公司信息不存在...");
        }
        companyService.updateCompany(company, user);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @RequestMapping(value = "/registerCompany", method = RequestMethod.POST)
    @ApiOperation(value = "处理注册公司请求", response = String.class, notes = "success")
    public ResponseEntity<?> registerCompany(@ApiParam(name = "company", value = "公司详情") @Validated @RequestBody Company company, @CurrentUser UserDTO user) {
        if (user.getCompanyId() != null) {
            throw new BusinessException("公司信息已经存在...");
        } else {
            String uuid = companyService.registerCompany(company, user);
            user.setCompanyId(uuid);
            user.setCompanyName(company.getName());
            return new ResponseEntity<>("success", HttpStatus.OK);
        }
    }

}
