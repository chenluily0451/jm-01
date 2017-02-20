package com.jm.bid.boot.config.thymeleaf.dialect;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by xiangyang on 2016/11/16.
 */
public class StringReplaceUtil {

    public String phoneFormat(String phone) {
        if (StringUtils.isEmpty(phone)) {
            throw new RuntimeException("format phone maybe is null or empty");
        }

        return phone.replaceAll("(\\d{3})\\d{5}(\\d{3})", "$1*****$2");
    }


    public String bankCardNumFormat(String bankNum) {
        if (StringUtils.isEmpty(bankNum)) {
            throw new RuntimeException("format phone maybe is null or empty");
        }

        return bankNum.replaceAll("([0-9a-zA-Z]{4})(?=[0-9a-zA-Z])", "$1 ");
    }
}
