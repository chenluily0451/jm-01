-- 支付申请表
create table jm_payment_apply(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  tenderId  BIGINT(20) NOT NULL COMMENT '招标id',
  tenderCode VARCHAR(30) NOT NULL COMMENT '招标编号',
  amount DECIMAL(10,2) NOT NULL  COMMENT '申请支付金额',
  type int(1) NOT NULL  COMMENT '支付款项类型 1 保证金',
  recAccountNo VARCHAR(40) NOT NULL    COMMENT   '收款资金账号',
  done TINYINT(1) NOT NULL DEFAULT 0 COMMENT   '是否已经处理',
  companyId VARCHAR(40) NOT NULL COMMENT '公司id',
  companyName VARCHAR(50) NOT NULL COMMENT '公司名称',
  createDate DATETIME   DEFAULT  NOW()  COMMENT '申请创建时间',
  createBy VARCHAR(20) NOT NULL COMMENT '操作者用户Id',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 已删除 0 未删除'
);

-- 资金账户支付记录表
create table jm_payRecord(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  tenderId  BIGINT(20) NOT NULL COMMENT '招标id',
  tenderCode VARCHAR(30) NOT NULL COMMENT '招标编号',
  tradeId VARCHAR(40)    NOT NULL COMMENT '交易流水号',
  status int(1) NOT NULL DEFAULT  1 COMMENT  '1 发起支付 2 支付中  3 支付失败 4 支付成功',
  type   int(2) NOT NULL COMMENT '支付类型  1  保证金',
  amount DECIMAL(10,2) NOT NULL  COMMENT '支付金额',
  bankId  INT(2) NOT NULL  COMMENT  '资金账户所属银行id',
  payRemark VARCHAR(200)    DEFAULT  NULL  COMMENT '付款备注',
  payCompanyId VARCHAR(40) NOT NULL COMMENT '付款公司id',
  paySystemNum VARCHAR(20) NOT NULL  COMMENT '付款系统分配账号',
  payAccountName VARCHAR(40) NOT NULL  COMMENT '付款资金账号名称',
  payAccountNo VARCHAR(40) NOT NULL  COMMENT '付款资金账户号',
  recSystemNum VARCHAR(20) NOT NULL    COMMENT   '收款系统分配账号',
  recAccountName VARCHAR(40) NOT NULL  COMMENT '收款资金账户名称',
  recAccountNo VARCHAR(40) NOT NULL    COMMENT   '收款资金账户号',
  createDate DATETIME   NOT NULL DEFAULT NOW() COMMENT '付款创建时间',
  createBy VARCHAR(40)  NOT NULL  COMMENT '付款创建人电话',
  deleted TINYINT(1) NOT NULL DEFAULT  0 COMMENT '0 未删除 1 已删除',
  bankMsg VARCHAR(200) NULL COMMENT '银行响应信息'
);

-- 投标表
create table jm_bid(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  tenderId  BIGINT(20) NOT NULL COMMENT '招标id',
  companyId VARCHAR(40) NOT NULL COMMENT '公司id',
  companyName VARCHAR(50) NOT NULL COMMENT '公司名称',
  quotePrice DECIMAL(6,2) NOT NULL COMMENT '报价',
  quoteAmount INT(7) NOT NULL  COMMENT '提投标量',
  checked  TINYINT(1) DEFAULT  NULL  COMMENT '是否中标',
  winBidAmount INT(7) DEFAULT NULL COMMENT '中标量',
  createDate DATETIME(6)   DEFAULT NOW(6)  COMMENT '创建时间',
  createBy VARCHAR(40)  NOT NULL  COMMENT '创建人'
);