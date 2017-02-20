package com.jm.bid.common.controller;

import com.jm.bid.admin.tender.dto.TenderNoticeDTO;
import com.jm.bid.admin.tender.service.TenderNoticeService;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.boot.web.WebUtils;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.dto.TenderDTO;
import com.jm.bid.common.dto.TenderParamDTO;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.entity.TenderNotice;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.bid.service.BidService;
import com.jm.bid.user.finance.entity.PayRecord;
import com.jm.bid.user.finance.service.PayApplyService;
import com.jm.bid.user.finance.service.PayRecordService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by xiangyang on 2016/11/7.
 */
@Controller
public class IndexController {

    @Autowired
    private TenderService tenderService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private PayRecordService payRecordService;

    @Autowired
    private BidService bidService;

    @Autowired
    private PayApplyService payApplyService;

    @Autowired
    private TenderNoticeService tenderNoticeService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(TenderParamDTO tenderParamDTO, TenderNoticeDTO tenderNoticeDTO, Model model) {
        model.addAttribute("coalTypeList", TenderService.coalTypeDic);
        model.addAttribute("coalZoneList", TenderService.coalZoneDic);
        model.addAttribute("forwardStationList", TenderService.forwardStationDic);
        model.addAttribute("transportModeList", TenderService.transportModeDic);
        model.addAttribute("page", tenderService.findAllTenderInProcessing(tenderParamDTO));
        model.addAttribute("totalSaleAmount", bidService.selectTotalSaleAmount());
        tenderNoticeDTO.setStatus(1);
        model.addAttribute("noticePage", tenderNoticeService.findTenderNotice(tenderNoticeDTO));
        return "/index";
    }


    @RequestMapping(value = "/tender", method = RequestMethod.GET)
    public String tenderIndex(TenderParamDTO tenderParamDTO, Model model) {
        model.addAttribute("coalTypeList", TenderService.coalTypeDic);
        model.addAttribute("coalZoneList", TenderService.coalZoneDic);
        model.addAttribute("forwardStationList", TenderService.forwardStationDic);
        model.addAttribute("transportModeList", TenderService.transportModeDic);
        model.addAttribute("tenderParam", tenderParamDTO);
        model.addAttribute("page", tenderService.findAllTenderInProcessing(tenderParamDTO));
        return "/partials/tender/tenderIndex";
    }

    @RequestMapping(value = "/tender/{id}", method = RequestMethod.GET)
    public String tenderIndex(@PathVariable("id") long id, Model model) {
        TenderDTO tenderDTO = tenderService.loadTenderDTOById(id);
        if (tenderDTO == null || !tenderService.isProcessingTender(tenderDTO.getStatus())) {
            throw new NotFoundException();
        } else {
            UserDTO user = userSession.getUser();
            model.addAttribute("paid", false);
            //判断是否支付
            if (tenderDTO.getStatus() == 2 || tenderDTO.getStatus() == 3) {
                if (userSession.isLogined() && (user.getRole() == UserRole.SALESMAN || user.getRole() == UserRole.ADMIN)
                        && payRecordService.isPaySuccess(id, PayRecord.Type.deposit, user.getCompanyId())) {
                    model.addAttribute("paid", true);
                }
            }
            //预约电话
            if (userSession.isLogined()) {
                tenderService.updateVisitorsCount(id);
                model.addAttribute("subscribePhone", user.getSecurePhone());
                model.addAttribute("bidApply", payApplyService.loadPayApplyByTenderId(id, userSession.getUser().getCompanyId()));
                model.addAttribute("visitorsCount", tenderDTO.getVisitorsCount() + 1);
            }
            model.addAttribute("subscribeCount", bidService.bidSubscribeCount(id));
            model.addAttribute("currentPrice", bidService.selectMaxPriceByTenderId(id));
            model.addAttribute("bidRecordList", bidService.selectBidByTenderId(id));
            model.addAttribute("coalTypeList", TenderService.coalTypeDic);
            model.addAttribute("coalZoneList", TenderService.coalZoneDic);
            model.addAttribute("forwardStationList", TenderService.forwardStationDic);
            model.addAttribute("transportModeList", TenderService.transportModeDic);
            model.addAttribute("tender", tenderDTO);
        }
        return "/partials/tender/tenderDetail";
    }

    @RequestMapping(value = "/tender/subscribeTender", method = RequestMethod.POST)
    public ResponseEntity subscribeTender(HttpServletRequest request, @RequestParam("tenderId") long tenderId, @RequestParam("phone") String phone) {
        Tender tender = tenderService.loadTenderById(tenderId);
        if (tender == null) {
            throw new NotFoundException();
        }
        if (tender.getStatus() == 3) {
            throw new BusinessException("竞价项目已经开始!");
        }
        bidService.addBidSubscribe(tenderId, phone, WebUtils.getIpAddress(request), userSession.isLogined());
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/helpCenter", method = RequestMethod.GET)
    @ApiOperation(value = "帮助中心")
    public String helpCenter() {
        return "/partials/helpcenter/helpCenter";
    }

    @RequestMapping(value = "/tender/notice", method = RequestMethod.GET)
    @ApiOperation(value = "招标公告首页")
    public String tenderNotice(TenderNoticeDTO tenderNoticeDTO, Model model) {
        tenderNoticeDTO.setStatus(1);
        model.addAttribute("page", tenderNoticeService.findTenderNotice(tenderNoticeDTO));
        return "/partials/tender/tenderNotice";
    }

    @RequestMapping(value = "/tender/notice/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "招标公告详情")
    public String noticeDetail(@PathVariable long id, Model model) {
        TenderNotice tenderNotice = tenderNoticeService.loadNoticeById(id);
        if (tenderNotice == null || tenderNotice.getStatus() != 1) {
            throw new NotFoundException();
        }
        model.addAttribute("tender", tenderService.loadTenderById(tenderNotice.getTenderId()));
        model.addAttribute("notice", tenderNotice);
        return "/partials/tender/noticeDetail";
    }

    @RequestMapping(value = "/payLicence", method = RequestMethod.GET)
    @ApiOperation("支付许可页面路由")
    public String payLicence() {
        return "/partials/pay/index";
    }

}
