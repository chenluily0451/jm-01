package com.jm.bid.boot.web;

import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.boot.annotation.CurrentUser;
import com.jm.bid.boot.exception.AdminUnauthorizedException;
import com.jm.bid.boot.exception.ForbiddenException;
import com.jm.bid.boot.exception.UnauthorizedException;
import com.jm.bid.common.controller.AdminSession;
import com.jm.bid.common.controller.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by xiangyang on 15-6-4.
 */
@Service
public class CurrentUserMethodArgumentHandler implements HandlerMethodArgumentResolver {


    @Autowired
    private UserSession userSession;

    @Autowired
    private AdminSession adminSession;

    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(CurrentUser.class) || parameter.hasParameterAnnotation(CurrentAdmin.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.getNativeRequest();
        if (!userSession.isLogined() && parameter.hasParameterAnnotation(CurrentUser.class)) {
            if(!WebUtils.isAjaxRequest(httpServletRequest)){
                WebUtils.saveRequest(httpServletRequest);
            }
            throw new UnauthorizedException();
        }
        if (!adminSession.isLogined() && parameter.hasParameterAnnotation(CurrentAdmin.class)) {
            if(!WebUtils.isAjaxRequest(httpServletRequest)){
                WebUtils.saveRequest(httpServletRequest);
            }
            throw new AdminUnauthorizedException();
        }
        return parameter.hasParameterAnnotation(CurrentUser.class) ? userSession.getUser() : adminSession.getAdmin();
    }
}
