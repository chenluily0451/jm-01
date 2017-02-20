package com.jm.bid.boot.exception;

/**
 * Created by wangqi on 16/8/23.
 */
public class BankException extends RuntimeException {


    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
