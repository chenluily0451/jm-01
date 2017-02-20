package com.jm.bid.boot.exception;

import lombok.Data;

/**
 * Created by xiangyang on 16/8/17.
 */
@Data
public class NoHasCashAccountException extends Exception {


    public String url;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public NoHasCashAccountException(String url) {
        this.url = url;
    }

}
