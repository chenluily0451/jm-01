
-- 支付申请增加报价量字段
alter TABLE jm_payment_apply add COLUMN quantity Integer(11) NOT NULL AFTER amount;

alter TABLE  jm_payRecord add COLUMN payApplyId BIGINT(20) NOT NULL AFTER tenderCode;