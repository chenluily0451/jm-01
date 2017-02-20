package com.jm.bid.common.controller;

import com.jm.bid.user.bid.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by xiangyang on 16/7/29.
 */
@ApiIgnore
@Controller
public class ErrorController {

    @Autowired
    public BidService bidService;

    @RequestMapping("/400")
    public String error400() {
        return "/partials/error/400";
    }

    @RequestMapping("/403")
    public String error403() {
        return "/partials/error/403";
    }

    @RequestMapping("/company403")
    public String company403() {
        return "/partials/error/403_company";
    }
    @RequestMapping("/404")
    public String error404() {
        return "/partials/error/404";
    }


    @RequestMapping("/409")
    public String error409() {
        return "/partials/error/409";
    }

    @RequestMapping("/500")
    public String error500() {
        return "/partials/error/500";
    }


    @RequestMapping("/admin/400")
    public String adminError400() {
        return "/admin/error/400";
    }

    @RequestMapping("/admin/403")
    public String adminError403() {
        return "/admin/error/403";
    }

    @RequestMapping("/admin/404")
    public String adminError404() {
        return "/admin/error/404";
    }


    @RequestMapping("/admin/409")
    public String adminError409() {
        return "/admin/error/409";
    }

    @RequestMapping("/admin/500")
    public String adminError500() {
        return "/admin/error/500";
    }

    @ModelAttribute
    public void fillTotalSaleAmount(Model model) {
        model.addAttribute("totalSaleAmount",bidService.selectTotalSaleAmount());
    }

}
