package com.jm.bid.boot.web;

import javax.servlet.http.HttpServletRequest;

/**
 * Maintains request data for a request that was redirected, so that after authentication
 * the user can be redirected to the originally requested page.
 * <p>
 * Created by xiangyang on 2016/11/18.
 */
public class SavedRequest {


    private String method;
    private String queryString;
    private String requestURI;

    /**
     * Constructs a new instance from the given HTTP request.
     *
     * @param request the current request to save.
     */
    public SavedRequest(HttpServletRequest request) {
        this.method = request.getMethod();
        this.queryString = request.getQueryString();
        this.requestURI = request.getRequestURI();
    }

    public String getMethod() {
        return method;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getRequestUrl() {
        StringBuilder requestUrl = new StringBuilder(getRequestURI());
        if (getQueryString() != null) {
            requestUrl.append("?").append(getQueryString());
        }
        return requestUrl.toString();
    }


}
