package com.jm.bid.common.consts;

/**
 * Created by xiangyang on 2016/11/22.
 */
public enum UserRole {

    /*系统管理员*/
    ADMIN("管理人员"),

    /*业务员*/
    SALESMAN("业务人员"),

    /*财务员*/
    TREASURER("财务人员"),

    ADMIN_SALESMAN("业务人员"),

    ADMIN_TREASURER("财务人员");

    public String value;

    UserRole(String value) {
        this.value = value;
    }
}
