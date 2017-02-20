package com.jm.bid.admin.account.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.service.AdminCompanyService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.user.bid.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by xiangyang on 2016/11/9.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TenderService tenderService;

    @Autowired
    private AdminCompanyService adminCompanyService;

    @Autowired
    private BidService bidService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String adminIndex(@CurrentAdmin AdminDTO adminDTO, Model model) {
        if (adminDTO.getRole() == UserRole.ADMIN_TREASURER) {
            return "redirect:/admin/finance";
        }
        model.addAttribute("tenderInProcessCount", tenderService.loadAllTenderByStatus(3).stream().count());
        model.addAttribute("waitAuditCustomerCount", adminCompanyService.countCompanyByStatus(3));
        model.addAttribute("releaseTenderTotalCount", tenderService.loadAllTenderByStatus(2, 3, 4).stream().count());
        model.addAttribute("totalSaleAmount",bidService.selectTotalSaleAmount());
        model.addAttribute("totalSaleMoney",bidService.selectTotalSaleMoney());
        model.addAttribute("user",adminDTO);
        return "/admin/index";
    }


}
