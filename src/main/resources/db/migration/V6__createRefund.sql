CREATE TABLE jm_refundRecord (
  id             BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  tenderId       BIGINT(20)              NOT NULL COMMENT '招标id',
  tenderCode     VARCHAR(30)             NOT NULL COMMENT '招标编号',
  payTradeId     VARCHAR(40)             NOT NULL COMMENT '付款流水号-退款资金来源',
  tradeId        VARCHAR(40)             NULL COMMENT '退款流水号',
  status         INT(1)                  NOT NULL COMMENT '1 发起退款 2 退款中  3 退款失败 4 退款成功',
  type           INT(1)                  NOT NULL COMMENT '退款类型   1  保证金',
  auto           TINYINT(1)              NOT NULL DEFAULT 0 COMMENT '0 自动 1 需要财务确认',
  fulledRefund   TINYINT(1)              NOT NULL COMMENT '1 全额退款  2 部分退款',
  paySystemNum   VARCHAR(20)             NOT NULL COMMENT '付款系统分配账号',
  payAccountName VARCHAR(40)             NOT NULL COMMENT '付款资金账户名称',
  payAccountNo   VARCHAR(40)             NOT NULL COMMENT '付款资金账户号',
  recSystemNum   VARCHAR(20)             NOT NULL COMMENT '收款系统分配账号',
  recAccountName VARCHAR(40)             NOT NULL COMMENT '收款资金账户名称',
  recAccountNo   VARCHAR(40)             NOT NULL COMMENT '收款资金账户号',
  refundAmount   DECIMAL(10, 2)          NULL COMMENT '退款金额',
  payTotalAmount DECIMAL(10, 2)          NOT NULL  COMMENT '支付总金额',
  createDate     DATETIME                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  createBy       VARCHAR(40)             NOT NULL COMMENT '退款创建人',
  deleted        TINYINT(1)              NOT NULL DEFAULT 0 COMMENT '0 未删除 1 已删除',
  bankMsg        VARCHAR(200)            NULL COMMENT '银行备注',
  payRemark      VARCHAR(200)            NULL  COMMENT '付款备注',
  UNIQUE KEY `tradeId_UNIQUE` (`tradeId`)
);
-- 提现表
CREATE TABLE jm_self_withdraw_record (
  id         BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  tradeId    VARCHAR(19)            NOT NULL COMMENT '交易Id',
  amount     DECIMAL(10, 2)         NOT NULL COMMENT '提现金额',
  status     INT(1)                 NOT NULL COMMENT '1 发起提现 2 银行处理中  3 银行处理失败 4 银行处理成功',
  bankCardId BIGINT(20)             NOT NULL COMMENT '银行卡id',
  createBy   VARCHAR(20)            NOT NULL COMMENT '创建人',
  createDate DATETIME               NOT NULL COMMENT '创建日期',
  remark     VARCHAR(200)           NULL COMMENT '提现备注',
  bankMsg    VARCHAR(200)           NULL COMMENT '银行信息',
  deleted    TINYINT(1) COMMENT '1 删除 0 未删除',
  UNIQUE KEY `tradeId_UNIQUE` (`tradeId`)
);

CREATE TABLE jm_tenderNotice (
  id                BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  tenderId          BIGINT(20)             NOT NULL COMMENT '招标Id',
  tenderCode        VARCHAR(14)            NOT NULL COMMENT '招标编号',
  tenderTitle       VARCHAR(50)            NOT NULL COMMENT '招标名称',
  noticeName        VARCHAR(50)            NOT NULL COMMENT '公告名称',
  status            TINYINT(1)             NOT NULL COMMENT '公告状态 1 发布  2 编辑 ',
  hasWinBid            TINYINT(1)             NOT NULL COMMENT '公示类型 1 中标公示  0 流标公示 ',
  contentStr        TEXT COMMENT '公告内容html',
  noticeStartDate   DATETIME COMMENT '公示开始时间',
  noticeEndDate     DATETIME COMMENT '公示结束时间',
  servicePhone      VARCHAR(20) COMMENT '监督电话',
  serviceDepartment VARCHAR(50) COMMENT '监督部门',
  createDate        DATETIME   NOT NULL COMMENT '创建时间',
  lastUpdateDate    DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  createBy          VARCHAR(45) COMMENT '创建人'
);