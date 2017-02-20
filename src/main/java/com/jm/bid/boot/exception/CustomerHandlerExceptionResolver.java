package com.jm.bid.boot.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.bid.boot.web.Response;
import com.jm.bid.boot.web.WebUtils;
import com.jm.bid.common.service.ExceptionReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Created by joe on 1/15/15.
 */
@ControllerAdvice
public class CustomerHandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(CustomerHandlerExceptionResolver.class);


    @Autowired
    ExceptionReporter reporter;

    @Autowired
    private ObjectMapper om;

    private static final String userUnAuth_flag = "userUnauthorized";

    private static final String adminUnAuth_flag = "adminUnauthorized";

    @ExceptionHandler({BindException.class, TypeMismatchException.class, MissingServletRequestParameterException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void process400Error(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        logger.error("400Exception:", ex);
        if (WebUtils.isAjaxRequest(request)) {
            response.setStatus(400);
        } else if (WebUtils.isFromAdminRequest(request)) {
            response.sendRedirect("/admin/400");
        } else {
            response.sendRedirect("/400");
        }
    }

    //户登录异常处理
    @ExceptionHandler({UnauthorizedException.class, AdminUnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void process401Error(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        response.setStatus(401);
        if (!WebUtils.isAjaxRequest(request)) {
            if (ex instanceof UnauthorizedException) {
                response.sendRedirect("/login");
            } else {
                response.sendRedirect("/admin/login");
            }
        } else {

            if (ex instanceof UnauthorizedException) {
                om.writeValue(response.getOutputStream(), Response.error(userUnAuth_flag));
            } else {
                om.writeValue(response.getOutputStream(), Response.error(adminUnAuth_flag));
            }
        }
    }

    //权限校验异常
    @ExceptionHandler({ForbiddenException.class, CompanyStatusException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void process403Error(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        logger.error("403Exception:", ex);

        if (ex instanceof ForbiddenException) {
            if (WebUtils.isAjaxRequest(request)) {
                response.setStatus(403);
                om.writeValue(response.getOutputStream(), Response.error("FORBIDDEN"));
            } else if (WebUtils.isFromAdminRequest(request)) {
                response.sendRedirect("/admin/403");
            } else {
                response.sendRedirect("/403");
            }
        } else if (ex instanceof CompanyStatusException) {
            CompanyStatusException companyStatusException = (CompanyStatusException) ex;
            if (WebUtils.isAjaxRequest(request)) {
                response.setStatus(403);
                om.writeValue(response.getOutputStream(), Response.error(companyStatusException.getStatus().name()));
            } else {
                request.setAttribute("flag", companyStatusException.getStatus().name());
                request.getRequestDispatcher("/company403").forward(request, response);
            }
        }
    }

    @ExceptionHandler({NotFoundException.class, NoSuchRequestHandlingMethodException.class, NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class, MultipartException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void process404Error(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        logger.error("404Exception:", ex);
        if (WebUtils.isAjaxRequest(request)) {
            response.setStatus(404);
        } else if (WebUtils.isFromAdminRequest(request)) {
            response.sendRedirect("/admin/404");
        } else {
            response.sendRedirect("/404");
        }
    }


    //业务异常
    @ExceptionHandler({BusinessException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public void process409Error(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        response.setStatus(409);
        if (WebUtils.isAjaxRequest(request)) {
            response.setContentType("application/json; charset=UTF-8");
            om.writeValue(response.getOutputStream(), Response.error(ex.getMessage()));
        } else if (WebUtils.isFromAdminRequest(request)) {
            request.setAttribute("errMsg", ex.getMessage());
            request.getRequestDispatcher("/admin/409").forward(request, response);
        } else {
            request.setAttribute("errMsg", ex.getMessage());
            request.getRequestDispatcher("/409").forward(request, response);
        }
    }

    //没有资金账户异常
    @ExceptionHandler(NoHasCashAccountException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void processNoCashAccountError(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {

        NoHasCashAccountException noHasCashAccountException = (NoHasCashAccountException) ex;
        if (WebUtils.isAjaxRequest(request)) {
            response.setContentType("application/json; charset=UTF-8");
            om.writeValue(response.getOutputStream(), Response.error("抱歉,您还未开通资金账户,请先开通资金账户再使用。谢谢!"));
        } else {
            response.sendRedirect(noHasCashAccountException.getUrl());
        }
    }

    //请求aegis-pg出错 http statusCode 返回503
    @ExceptionHandler({HttpServerErrorException.class, ResourceAccessException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void processPgClientException(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        logger.error("pg error:" + ex);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void process500Error(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        logger.error("500Exception:", ex);
        handler500(request, ex);
        if (WebUtils.isAjaxRequest(request)) {
            response.setStatus(500);
        } else if (WebUtils.isFromAdminRequest(request)) {
            response.sendRedirect("/admin/500");
        } else {
            response.sendRedirect("/500");
        }
    }


    @Async
    private void handler500(HttpServletRequest request, Exception ex) {
        logger.warn("开始发送邮件");
        try {
            if ("application/json".equals(request.getContentType())) {
                reporter.handle(ex, request.getRequestURL().toString(), om.writeValueAsString(extractPostRequestBody(request)), loadRequestHeader(request));
            } else {
                reporter.handle(ex, request.getRequestURL().toString(), om.writeValueAsString(request.getParameterMap()), loadRequestHeader(request));
            }
        } catch (Exception e) {
            logger.warn("邮件发送失败", e);
        }
        logger.warn("邮件发送结束");
    }


    public String extractPostRequestBody(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = null;
            try {
                s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s.hasNext() ? s.next() : "";
        } else {
            return "{}";
        }
    }

    public String loadRequestHeader(HttpServletRequest request) throws JsonProcessingException {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        map.put("Request Method", request.getMethod());
        map.put("requestURL", request.getRequestURL().toString());
        return om.writeValueAsString(map);
    }


}
