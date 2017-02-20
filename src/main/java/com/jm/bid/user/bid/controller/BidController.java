package com.jm.bid.user.bid.controller;

import com.jm.bid.admin.tender.service.TenderService;
import com.jm.bid.boot.annotation.CheckCompany;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.annotation.RequiresRoles;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NoHasCashAccountException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.web.Response;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.entity.Tender;
import com.jm.bid.common.service.SMS;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.service.UserService;
import com.jm.bid.user.bid.dto.BidDTO;
import com.jm.bid.user.bid.service.BidService;
import com.jm.bid.user.finance.entity.BankCashAccount;
import com.jm.bid.user.finance.entity.PayRecord;
import com.jm.bid.user.finance.entity.PaymentApply;
import com.jm.bid.user.finance.service.CashAccountService;
import com.jm.bid.user.finance.service.PayApplyService;
import com.jm.bid.user.finance.service.PayRecordService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/13.
 */
@Api("jm-user-bid")
@Controller
@RequestMapping("/account/bid")
@RequiresRoles({UserRole.SALESMAN, UserRole.ADMIN})
@CheckCompany
public class BidController {

    @Autowired
    private PayApplyService payApplyService;

    @Autowired
    private PayRecordService payRecordService;

    @Autowired
    private CashAccountService cashAccountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private BidService bidService;

    @Autowired
    private SMS sms;

    private static final String openCashAccount_sms = "您好,贵公司尚未开通资金账户,请先开通晋煤资金账号--缴纳保证金。再进行竞价。谢谢!";

    private static final String noHasTreasurer_sms = "您好,贵公司尚未分配财务人员,请添加财务人员--开通晋煤资金账号--缴纳保证金。再进行竞价。谢谢!";

    @RequestMapping(method = RequestMethod.GET)
    public String myBid(BidDTO bidDTO, @CurrentUser UserDTO user, Model model) {
        model.addAttribute("page", bidService.findByCompanyId(bidDTO, user.getCompanyId()));
        return "/partials/bid/myBid";
    }

    @RequestMapping(value = "/{tenderId}", method = RequestMethod.GET)
    public String bidDetail(@PathVariable("tenderId") long tenderId, @CurrentUser UserDTO user, Model model) {
        Tender tender = tenderService.loadTenderById(tenderId);
        if (tender == null) {
            throw new NotFoundException("投标不存在!");
        }
        if (!tenderService.isProcessingTender(tender.getStatus())) {
            throw new NotFoundException("投标不存在!");
        }
        model.addAttribute("tender", tender);
        model.addAttribute("myBid", bidService.selectByCompanyIdAndTenderId(tenderId, user.getCompanyId()));
        model.addAttribute("otherBids", bidService.selectBidByTenderId(tenderId));
        return "/partials/bid/bidList";
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public ResponseEntity apply(@RequestParam("tenderId") long tenderId, @RequestParam("quantity") int quantity, @CurrentUser UserDTO user) {
        if (userService.loadEmployeeByCompanyId(user.getCompanyId(), UserRole.TREASURER).size() == 0) {
            User admin = userService.loadEmployeeByCompanyId(user.getCompanyId(), UserRole.ADMIN).get(0);
            sms.sendSMS(admin.getSecurePhone(), noHasTreasurer_sms);
            return new ResponseEntity(Response.error("noHasTreasurer"), HttpStatus.OK);
        }
        try {
            cashAccountService.loadBankCashAccountByCompanyId(user.getCompanyId(), BankCashAccount.AccountType.generalAccount);
        } catch (NoHasCashAccountException e) {
            User treasurer = userService.loadEmployeeByCompanyId(user.getCompanyId(), UserRole.TREASURER).get(0);
            sms.sendSMS(treasurer.getSecurePhone(), openCashAccount_sms);
            return new ResponseEntity(Response.error("noOpenCashAccount"), HttpStatus.OK);
        }
        Tender tender = tenderService.loadTenderById(tenderId);
        if (tender == null) {
            throw new NotFoundException();
        }


        if (quantity > tender.getSaleAmount()) {
            throw new BusinessException("提报吨数不能大于竞价发布总吨数!");
        }
        if (tender.getTenderStartDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("竞价项目已经过了预告, 不能报名缴纳保证金...");
        }
        if (quantity % 1000 != 0) {
            throw new BusinessException("提报吨数应该为1000的倍数,请重新填写!");
        }
        payApplyService.addPayApply(tenderId, quantity, user);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }


    @RequestMapping(value = "/releaseBid", method = RequestMethod.POST)
    public ResponseEntity releaseBid(@RequestParam("tenderId") long tenderId, @RequestParam("quotePrice") BigDecimal quotePrice, @RequestParam("quoteQuantity") Integer quoteQuantity, @CurrentUser UserDTO user) {
        Tender tender = tenderService.loadTenderById(tenderId);
        if (tender == null) {
            throw new BusinessException("竞价记录不存在!");
        }
        if (tender.getStatus() == Tender.Status.predict.value) {
            throw new BusinessException("竞价项目尚未开始,敬请期待!");
        }

        if (tender.getStatus() != Tender.Status.processing.value) {
            throw new BusinessException("竞价项目已经结束!");
        }

        if (quoteQuantity.compareTo(tender.getSaleAmount()) == 1) {
            throw new BusinessException("抱歉,库存不足!");
        }

        if (quotePrice.compareTo(tender.getSaleBasePrice()) == -1) {
            throw new BusinessException("报价不能低于竞买底价!");
        }

        if (quoteQuantity.compareTo(tender.getMinimumSaleAmount()) == -1) {
            throw new BusinessException("竞买量不能低于最低竞买量!");
        }
        //判断是否缴纳保证金
        if (!payRecordService.isPaySuccess(tenderId, PayRecord.Type.deposit, user.getCompanyId())) {
            return new ResponseEntity(Response.error("noPayDeposit"), HttpStatus.OK);
        }

//        加载上次报价吨数做比较
//        BidDTO bidDTO = bidService.selectByCompanyIdAndTenderId(tenderId, user.getCompanyId());

//        if (bidDTO != null && quoteQuantity.compareTo(bidDTO.getQuoteAmount()) == -1) {
//            throw new BusinessException("报价吨数不能比上次报价吨数少!");
//        }
        //加载当前报价最高价验证
        BigDecimal maxQuotePrice = bidService.selectMaxPriceByTenderId(tenderId);
        if (maxQuotePrice != null && quotePrice.compareTo(maxQuotePrice) == -1) {
            throw new BusinessException("报价不能低于当前最新的报价,最新报价为:" + maxQuotePrice);
        }
        PaymentApply paymentApply = payApplyService.loadPayApplyByTenderId(tenderId, user.getCompanyId());
        bidService.insertBid(tenderId, quotePrice, paymentApply.getQuantity(), user);
        return new ResponseEntity(Response.success(), HttpStatus.OK);
    }


}
