package com.jm.bid.boot.exception;

import com.jm.bid.user.account.entity.Company;

/**
 * Created by xiangyang on 2016/12/28.
 */
public class CompanyStatusException extends RuntimeException {


    private Company.Status status;

    public CompanyStatusException(Company.Status status) {
        this.status = status;
    }

    public Company.Status getStatus() {
        return status;
    }

    public void setStatus(Company.Status status) {
        this.status = status;
    }
}
