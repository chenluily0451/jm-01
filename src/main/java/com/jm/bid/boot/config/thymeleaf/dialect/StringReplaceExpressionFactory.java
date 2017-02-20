package com.jm.bid.boot.config.thymeleaf.dialect;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiangyang on 2016/11/16.
 */
public class StringReplaceExpressionFactory implements IExpressionObjectFactory {

    private static final String CUSTOMER_STRING_UTIL = "CustomerStringUtil";

    private static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(CUSTOMER_STRING_UTIL)));

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (CUSTOMER_STRING_UTIL.equals(expressionObjectName)) {
            return new StringReplaceUtil();
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return expressionObjectName != null && CUSTOMER_STRING_UTIL.equals(expressionObjectName);
    }

}
