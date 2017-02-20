package com.jm.bid.admin.account.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.dto.CompanyDTO;
import com.jm.bid.admin.account.service.AdminCompanyService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.user.account.entity.Company;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
 * Created by xiangyang on 2016/11/18.
 */
@Api(tags = "jm-admin-customer")
@Controller
@RequestMapping("/admin/customer")
@RequiresRoles(UserRole.ADMIN_SALESMAN)
public class AdminCustomerController {

    @Autowired
    private AdminCompanyService adminCompanyService;

    @RequestMapping(value = "/customerList", method = RequestMethod.GET)
    @ApiOperation(value = "跳转路由-admin审核客户列表页")
    public String customerList(CompanyDTO company, @CurrentAdmin AdminDTO adminDTO, Model model) {
        model.addAttribute("companyParam", company);
        model.addAttribute("page", adminCompanyService.findAllCompany(company));
        return "/admin/customer/customerList";
    }

    @RequestMapping(value = "/customerAudit", method = RequestMethod.GET)
    @ApiOperation(value = "跳转路由-审核客户详情页")
    public String customerAudit(@RequestParam("uid") String uid, @CurrentAdmin AdminDTO adminDTO, Model model) {
        Company company = adminCompanyService.loadByUUID(uid);
        if (company == null) {
            throw new NotFoundException();
        }
        model.addAttribute("company", company);
        model.addAttribute("auditList", adminCompanyService.loadAuditMsgByUUID(company.getUUID()));
        return "/admin/customer/customerAudit";
    }

    @RequestMapping(value = "/auditPass", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "处理审核通过客户请求", response = String.class, notes = "responseText:success")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recordId", value = "公司记录id", dataType = "long", required = true, paramType = "form"),
            @ApiImplicitParam(name = "auditMsg", value = "备注信息", dataType = "String", paramType = "form")
    })
    public ResponseEntity auditPass(@RequestParam("recordId") long recordId, @RequestParam(value = "auditMsg", required = false) String auditMsg, @CurrentAdmin AdminDTO adminDTO) {
        adminCompanyService.auditCompany(recordId, auditMsg, true, adminDTO);
        return new ResponseEntity("success", HttpStatus.OK);
    }

    @RequestMapping(value = "/auditNotPass", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "处理审核未通过客户请求", response = String.class, notes = "responseText:success")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recordId", value = "公司记录id", dataType = "long", required = true, paramType = "form"),
            @ApiImplicitParam(name = "auditMsg", value = "备注信息", dataType = "String", required = true, paramType = "form")
    })
    public ResponseEntity auditNotPass(@RequestParam("recordId") long recordId, @RequestParam("auditMsg") String auditMsg, @CurrentAdmin AdminDTO adminDTO) {
        adminCompanyService.auditCompany(recordId, auditMsg, false, adminDTO);
        return new ResponseEntity("success", HttpStatus.OK);
    }
}
