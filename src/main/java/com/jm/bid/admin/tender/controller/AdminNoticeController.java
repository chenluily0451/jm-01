package com.jm.bid.admin.tender.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.tender.dto.TenderNoticeDTO;
import com.jm.bid.admin.tender.service.TenderNoticeService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.entity.TenderNotice;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

/**
 * Created by xiangyang on 2016/12/12.
 */
@Controller
@RequestMapping("/admin/notice")
@RequiresRoles(UserRole.ADMIN_SALESMAN)
public class AdminNoticeController {


    @Autowired
    private TenderNoticeService tenderNoticeService;

    @Autowired
    private TenderService tenderService;

    @RequestMapping(method = RequestMethod.GET)
    public String tenderNotice(TenderNoticeDTO tenderNoticeDTO, @CurrentAdmin AdminDTO admin, Model model) {
        model.addAttribute("page", tenderNoticeService.findTenderNotice(tenderNoticeDTO));
        model.addAttribute("searchParam", tenderNoticeDTO);
        return "/admin/notice/noticeList";
    }

    @RequestMapping(value = "/releaseNotice", method = RequestMethod.GET)
    public String releaseTenderNotice(HttpSession session, @CurrentAdmin AdminDTO adminDTO, Model model) {
        model.addAttribute("waitReleaseList", tenderService.loadWaitReleaseNotice());
        if (session.getAttribute("session_tenderNotice") != null) {
            TenderNotice tenderNotice = (TenderNotice) session.getAttribute("session_tenderNotice");
            model.addAttribute("tenderNotice", tenderNotice);
            model.addAttribute("tender",tenderService.loadTenderById(tenderNotice.getTenderId()));
            session.removeAttribute("session_tenderNotice");
        }
        return "/admin/notice/releaseNotice";
    }

    @RequestMapping(value = "/releaseTenderNotice", method = RequestMethod.POST)
    @ApiOperation("处理发布公示请求")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "tenderId", name = "招标id", paramType = "form"),
            @ApiImplicitParam(value = "noticeName", name = "公示名称", paramType = "form"),
            @ApiImplicitParam(value = "contentStr", name = "公示文本", paramType = "form"),
            @ApiImplicitParam(value = "noticeStartDate", name = "公示开始时间", paramType = "form"),
            @ApiImplicitParam(value = "noticeEndDate", name = "公示截止时间", paramType = "form"),
            @ApiImplicitParam(value = "servicePhone", name = "监督电话", paramType = "form"),
            @ApiImplicitParam(value = "serviceDepartment", name = "监督部门", paramType = "form"),
    })
    public ResponseEntity releaseNotice(TenderNotice tenderNotice, @CurrentAdmin AdminDTO adminDTO) {
        tenderNoticeService.releaseNotice(tenderNotice, adminDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }


    //打开预览页面
    @RequestMapping(value = "/openNoticePreview", method = RequestMethod.GET)
    public String noticePreview(HttpSession session, @CurrentAdmin AdminDTO adminDTO, Model model) {
        TenderNotice tenderNotice = (TenderNotice) session.getAttribute("session_tenderNotice");
        model.addAttribute("tenderNotice", tenderNotice);
        model.addAttribute("tender", tenderService.loadTenderById(tenderNotice.getTenderId()));
        return "/admin/notice/noticeDetail";
    }

    //设置预览
    @RequestMapping(value = "/setNoticePreview", method = RequestMethod.POST)
    public ResponseEntity noticePreview(TenderNotice tenderNotice, HttpSession session, @CurrentAdmin AdminDTO adminDTO, Model model) {
        session.setAttribute("session_tenderNotice", tenderNotice);
        return new ResponseEntity<>(Response.success(), HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String noticeDetail(@PathVariable long id, Model model) {
        TenderNotice tenderNotice = tenderNoticeService.loadNoticeById(id);
        if (tenderNotice == null) {
            throw new NotFoundException();
        }
        model.addAttribute("tender", tenderService.loadTenderById(tenderNotice.getTenderId()));
        model.addAttribute("tenderNotice", tenderNotice);
        return "/admin/notice/noticeDetail";
    }

    @RequestMapping(value = "/backOutNotice/{id}", method = RequestMethod.GET)
    public ResponseEntity backOut(@PathVariable long id) {
        tenderNoticeService.backOutNotice(id);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

}
