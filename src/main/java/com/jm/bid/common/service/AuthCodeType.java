package com.jm.bid.common.service;

/**
 * Created by xiangyang on 16/7/27.
 */
public enum AuthCodeType {
    register,
    regainPwd,


    adminResetPwd,
    adminResetPayPwd,
    adminResetBindOldPhone,
    adminResetBindNewPhone,
    adminRegainPwd,
    adminOpenCashAccount,
    adminBindBankCard,
    adminCutPaymentDeposit,
    adminRefundDeposit,
    adminWithDraw,

    userResetPwd,
    userResetPayPwd,
    userAddEmployee,
    userOpenCashAccount,
    userBindBankCard,
    userPayDeposit,
    userWithDraw;

}
