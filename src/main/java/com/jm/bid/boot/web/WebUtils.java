package com.jm.bid.boot.web;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.regex.Pattern;

/**
 * Created by xiangyang on 2016/11/18.
 */
public class WebUtils {

    public static final String SAVED_REQUEST_KEY = "JM_SAVED_REQUEST";

    public static Pattern p = Pattern.compile("^/admin/{1}.*$");

    public static void saveRequest(HttpServletRequest request) {

        SavedRequest savedRequest = new SavedRequest(request);
        HttpSession session = request.getSession();
        session.setAttribute(SAVED_REQUEST_KEY, savedRequest);
    }

    public static SavedRequest getSavedRequest(HttpServletRequest request) {
        SavedRequest savedRequest = null;
        HttpSession session = request.getSession();
        if (session != null) {
            savedRequest = (SavedRequest) session.getAttribute(SAVED_REQUEST_KEY);
            session.removeAttribute(SAVED_REQUEST_KEY);
        }
        return savedRequest;
    }

    public static void removeSaveRequest(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute(SAVED_REQUEST_KEY);
        }
    }

    public static boolean isFromAdminRequest(HttpServletRequest request) {
        return p.matcher(request.getRequestURI()).matches();
    }


    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
    }

    /**
     * 获取IP方法
     */
    public static String getIpAddress(HttpServletRequest request) {
        String IP = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isBlank(IP) && !"unKnown".equalsIgnoreCase(IP)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = IP.indexOf(",");
            if(index != -1) {
                return IP.substring(0, index);
            } else {
                return IP;
            }
        }
        IP = request.getHeader("X-Real-IP");
        if (!StringUtils.isBlank(IP) && !"unKnown".equalsIgnoreCase(IP)){
            return IP;
        }
        return request.getRemoteAddr();
    }
}
