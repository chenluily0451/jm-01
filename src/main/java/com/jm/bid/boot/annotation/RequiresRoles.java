package com.jm.bid.boot.annotation;


import com.jm.bid.common.consts.UserRole;

import java.lang.annotation.*;

/**
 * Created by xiangyang on 2016/12/15.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {
    UserRole[] value();
}
