package com.jm.bid.common.controller;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import com.jm.bid.boot.web.SavedRequest;
import com.jm.bid.boot.web.WebUtils;
import com.jm.bid.common.entity.BankSite;
import com.jm.bid.user.finance.service.BankCardService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by xiangyang on 2016/11/10.
 */
@Controller
public class CommonController {

    private static Producer KAPTCHAPRODUCER = null;

    @Autowired
    private UserSession userSession;

    @Autowired
    private AdminSession adminSession;

    @Autowired
    private BankCardService bankCardService;

    static {
        ImageIO.setUseCache(false);
        Properties props = new Properties();
        props.put("kaptcha.border", "no");
        props.put("kaptcha.textproducer.font.color", "black");
        props.put("kaptcha.textproducer.char.space", "4");
        props.put("kaptcha.textproducer.char.length", "6");
        Config config = new Config(props);
        KAPTCHAPRODUCER = config.getProducerImpl();
    }

    @RequestMapping(value = "/admin/loadImgCode", method = RequestMethod.GET)
    public void geneAdminImageCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String capText = KAPTCHAPRODUCER.createText();
        adminSession.setImgCode(capText);
        geneImageCode(capText, request, response);
    }

    @RequestMapping(value = "/loadImgCode", method = RequestMethod.GET)
    public void geneUserImageCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String capText = KAPTCHAPRODUCER.createText();
        userSession.setImgCode(capText);
        geneImageCode(capText, request, response);
    }

    private void geneImageCode(String capText, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        BufferedImage bi = KAPTCHAPRODUCER.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
    }

    @RequestMapping(value = "/bank/loadAllBank", method = RequestMethod.GET)
    @ApiOperation("加载所有银行信息")
    public ResponseEntity<List<BankSite>> loadAllBank() {
        return new ResponseEntity(bankCardService.loadAllBank(), HttpStatus.OK);
    }

    @RequestMapping(value = "/bank/loadBankSiteProvinces", method = RequestMethod.GET)
    @ApiOperation("加载银行所有省份信息")
    public ResponseEntity<List<BankSite>> loadBankSiteProvinces() {
        return new ResponseEntity(bankCardService.loadBankSiteProvinces(), HttpStatus.OK);
    }

    @RequestMapping(value = "/bank/loadBankSiteCities/{provinceId}", method = RequestMethod.GET)
    @ApiOperation("根据省份id加载银行所有城市信息")
    @ApiImplicitParam(name = "provinceId", value = "省份code",paramType = "path",required = true)
    public ResponseEntity<List<BankSite>> loadBankSiteCities(@PathVariable("provinceId") int provinceId) {
        return new ResponseEntity(bankCardService.loadBankSiteCities(provinceId), HttpStatus.OK);
    }


    @RequestMapping(value = "/bank/loadAllChildBanks/{cityCode}/{bankCode}", method = RequestMethod.GET)
    @ApiOperation("根据城市code、银行code加载所有支行信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cityCode", value = "城市code", paramType = "path", required = true),
            @ApiImplicitParam(name = "bankCode", value = "省份code", paramType = "path", required = true)
    })
    public ResponseEntity<List<BankSite>> loadChildBanksByCityCodeBankCode(@PathVariable Integer bankCode, @PathVariable Integer cityCode) {
        return new ResponseEntity(bankCardService.loadChildBanksByCityCodeBankCode(cityCode, bankCode), HttpStatus.OK);
    }


    @RequestMapping(value = "/redirectOriginPage", method = RequestMethod.GET)
    public String redirectOriginalPage(HttpServletRequest request) {
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        if (savedRequest != null) {
            return "redirect:" + savedRequest.getRequestUrl();
        }
        //正常点击登陆按钮的情况,重定向到首页
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin")) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }
}
