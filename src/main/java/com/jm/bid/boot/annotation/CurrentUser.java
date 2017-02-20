package com.jm.bid.boot.annotation;

import java.lang.annotation.*;

/**
 * Created by xiangyang on 16/8/17.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
