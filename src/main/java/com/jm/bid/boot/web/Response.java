package com.jm.bid.boot.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xiangyang on 2016/11/28.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Response<T> implements Serializable {

    private boolean success;
    private String error;
    private T data;

    private Response setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    private Response setError(String error) {
        this.error = error;
        return this;
    }


    public Response setData(T data) {
        this.data = data;
        return this;
    }

    public static Response success() {
        return new Response().setSuccess(true);
    }

    public static Response error(String error) {
        return new Response().setSuccess(false).setError(error);
    }



}
