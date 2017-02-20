package com.jm.bid.admin.tender.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.dto.TenderDTO;
import com.jm.bid.common.dto.TenderParamDTO;
import com.jm.bid.user.bid.service.BidService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created by xiangyang on 2016/12/5.
 */
@Api(tags = "jm-admin-tender")
@Controller
@RequestMapping("/admin/tender")
@RequiresRoles(UserRole.ADMIN_SALESMAN)
public class AdminTenderController {

    @Autowired
    private TenderService tenderService;

    @Autowired
    private BidService bidService;

    @RequestMapping(value = "/releaseTender", method = RequestMethod.GET)
    @ApiOperation(value = "打开发布竞价页面")
    public String releaseTender(@CurrentAdmin AdminDTO adminDTO, Model model) {
        return "/admin/tender/releaseTender";
    }

    @RequestMapping(value = "/releaseTender", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "处理发布竞价页面", response = Response.class)
    public ResponseEntity releaseTender(@ApiParam(name = "tender", value = "竞价信息") @Validated @RequestBody TenderDTO tenderDTO, @CurrentAdmin AdminDTO adminDTO) {
        tenderService.addRealTender(tenderDTO, adminDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/tempSaveTender", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "处理暂存发布竞价页面", response = Response.class)
    public ResponseEntity tempSaveTender(@ApiParam(name = "tender", value = "竞价信息") @Validated @RequestBody TenderDTO tenderDTO, @CurrentAdmin AdminDTO adminDTO) {
        tenderService.addTempTender(tenderDTO, adminDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/myTenderList", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-竞价列表页面")
    public String tenderList(TenderParamDTO tenderParamDTO, @CurrentAdmin AdminDTO adminDTO, Model model) {
        model.addAttribute("tenderParam", tenderParamDTO);
        model.addAttribute("tenderCountMap", tenderService.loadStatusCount());
        model.addAttribute("page", tenderService.findAllTender(tenderParamDTO));
        return "/admin/tender/myTenderList";
    }

    @RequestMapping(value = "/updateTender/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-修改竞价页面")
    public String updateTender(@PathVariable Long id, @CurrentAdmin AdminDTO adminDTO, Model model) {
        TenderDTO tender = tenderService.loadTenderDTOById(id);
        if (tender == null) {
            throw new NotFoundException();
        }
        model.addAttribute("tender", tender);
        return "/admin/tender/releaseTender";
    }

    @RequestMapping(value = "/updateForRealRelease/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "处理正式发布竞价请求的修改", notes = "处理正式发布竞价请求的修改", response = Response.class)
    @ApiImplicitParam(name = "id", dataType = "long", paramType = "path")
    public ResponseEntity updateTenderForRealRelease(@PathVariable("id") long id, @ApiParam(name = "tender", value = "竞价详情") @Validated @RequestBody TenderDTO tender, @CurrentAdmin AdminDTO adminDTO) {
        tender.setId(id);
        tenderService.updateTenderForRealRelease(tender, adminDTO);
        return new ResponseEntity<Object>(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/updateForTempSave/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "处理暂存发布竞价请求的修改", notes = "处理暂存发布竞价请求的修改", response = Response.class)
    @ApiImplicitParam(name = "id", dataType = "long", paramType = "path")
    public ResponseEntity updateTenderForTempSave(@PathVariable("id") long id, @ApiParam(name = "tender", value = "竞价详情") @Validated @RequestBody TenderDTO tender, @CurrentAdmin AdminDTO adminDTO) {
        tender.setId(id);
        tenderService.updateTenderForTempSave(tender, adminDTO);
        return new ResponseEntity<Object>(Response.success(), HttpStatus.OK);
    }


    @RequestMapping(value = "/deleteTender/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "处理删除竞价请求", response = Response.class)
    @ApiImplicitParam(name = "id", dataType = "long", paramType = "path")
    public ResponseEntity deleteTender(@PathVariable("id") long id, @CurrentAdmin AdminDTO adminDTO) {
        tenderService.deleteTender(id, adminDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/backOut/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "处理撤销价请求", response = Response.class)
    @ApiImplicitParam(name = "id", dataType = "long", paramType = "path")
    public ResponseEntity backOut(@PathVariable("id") long id, @CurrentAdmin AdminDTO adminDTO) {
        tenderService.backOutTender(id, adminDTO);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-竞价详情页面")
    public String tenderDetail(@PathVariable("id") long tenderId, @CurrentAdmin AdminDTO adminDTO, Model model) {
        TenderDTO tenderDTO = tenderService.loadTenderDTOById(tenderId);
        if (tenderDTO == null) {
            throw new NotFoundException();
        }
        model.addAttribute("tender", tenderDTO);
        return "/admin/tender/tenderDetail";
    }

    @RequestMapping(value = "/{id}/bidDetail", method = RequestMethod.GET)
    @ApiOperation(value = "路由跳转-竞价详情页面")
    public String bidDetail(@PathVariable("id") long tenderId, @CurrentAdmin AdminDTO adminDTO, Model model) {
        TenderDTO tenderDTO = tenderService.loadTenderDTOById(tenderId);
        if (tenderDTO == null) {
            throw new NotFoundException();
        }
        model.addAttribute("tender", tenderDTO);
        model.addAttribute("bidList", bidService.selectBidByTenderId(tenderId));
        model.addAttribute("noBidList", bidService.selectNoBidCompany(tenderId));
        return "/admin/bid/bidList";
    }

    @ModelAttribute
    public void initCoalDic(Model model) {
        model.addAttribute("coalTypeList", TenderService.coalTypeDic);
        model.addAttribute("coalZoneList", TenderService.coalZoneDic);
        model.addAttribute("forwardStationList", TenderService.forwardStationDic);
        model.addAttribute("transportModeList", TenderService.transportModeDic);
    }
}
