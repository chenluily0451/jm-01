package com.jm.bid.boot.config;

import com.jm.bid.boot.persistence.BaseFilter;

/**
 * Created by xiangyang on 16/8/29.
 */
public class PageContext {
    private static final ThreadLocal<BaseFilter> pageParamThreadLocal = new ThreadLocal<>();

    public static BaseFilter getPageParam() {
        return pageParamThreadLocal.get();
    }

    public static void setPageParam(BaseFilter page) {
        pageParamThreadLocal.set(page);
    }

    public static void cleanPageParam() {
        pageParamThreadLocal.remove();
    }
}
