CREATE TABLE jm_withdraw_record(
    id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    companyId VARCHAR(40) NOT NULL COMMENT '公司id',
    tradeId VARCHAR(40) NOT NULL COMMENT '交易Id',
    amount DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    status int(1) NOT NULL DEFAULT  1 COMMENT  '1 发起提现 2 银行处理中  3 银行处理失败 4 银行处理成功',
    bankCardId BIGINT(20) NOT NULL COMMENT '银行卡id',
    createBy VARCHAR(20) NOT NULL COMMENT '创建人',
    createDate DATETIME NOT NULL COMMENT '创建日期',
    remark VARCHAR(200) NULL COMMENT '提现备注',
    bankMsg VARCHAR(200) NULL COMMENT '银行信息',
    deleted TINYINT(1) COMMENT '1 删除 0 未删除'
);
ALTER  TABLE jm_tenderNotice MODIFY  lastUpdateDate DATETIME DEFAULT  now();


