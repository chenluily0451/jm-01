ALTER  TABLE  jm_user  ADD COLUMN  userName       VARCHAR(20)  COMMENT  '用户名';


-- 系统资金账号
create table jm_system_cashAccount(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  accId varchar(18) NOT NULL COMMENT '系统内资金账户',
  companyId VARCHAR(40) NOT NULL COMMENT '公司Id',
  createDate DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  createBy VARCHAR(20)   NOT NULL COMMENT '操作者用户Id',
  UNIQUE KEY `accId_companyId_UNIQUE` (`accId`,`companyId`)
);


-- 银行资金账户
create table jm_bankCashAccount(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  accId VARCHAR(18) NOT NULL COMMENT '系统资金账户id',
  bankAccountName varchar(50) NOT NULL COMMENT '银行资金账户名称',
  bankAccountNum varchar(30) NOT NULL COMMENT '银行资金账户号',
  accountType INT(1) NOT NULL  COMMENT  '资金账号类型 1 交易账户 ',
  bankId  INT(2) NOT NULL  COMMENT  '资金账号所属银行id',
  available  TINYINT(1) NOT NULL  COMMENT '1 可用 0 不可用',
  createDate DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  createBy   VARCHAR(20)   NOT NULL COMMENT '操作者用户Id',
  UNIQUE KEY `accId_accountType_UNIQUE` (`accId`,`accountType`)
);
--  绑定银行卡
CREATE TABLE jm_bankCard (
    id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    cardNum VARCHAR(45) NOT NULL COMMENT '银行卡号码',
    childBankCode BIGINT(20) NOT NULL COMMENT '开户支行联行号',
    childBankName VARCHAR(30) NOT NULL COMMENT '开户支行联行号',
    cardAccountName VARCHAR(50) NOT NULL COMMENT '账户名称',
    companyId VARCHAR(40) NOT NULL COMMENT '公司id',
    status INT(1)    DEFAULT  NULL   COMMENT '1 已打款 2 绑定成功  3 绑定失败',
    validateCount  INT(1) DEFAULT  0   COMMENT '验证次数',
    validated  TINYINT(1) DEFAULT  0   COMMENT '1 已验证  0 未验证',
    validateAmount DECIMAL(3,2) DEFAULT  NULL   COMMENT '验证金额',
    tradeId VARCHAR(19)   NULL COMMENT '交易Id',
    createBy VARCHAR(20) NOT NULL COMMENT '创建人',
    createDate DATETIME NOT NULL COMMENT '创建日期',
    deleted TINYINT(1) DEFAULT  0 COMMENT '1 删除 0 未删除',
    bankMsg VARCHAR(200) NULL  COMMENT  '银行备注'
);

--  晋煤自己系统账户
create table jm_self_systemCashAccount(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  accId varchar(18) NOT NULL COMMENT '系统内资金账户',
  createDate DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  createBy VARCHAR(20)   NOT NULL COMMENT '操作者用户Id',
  UNIQUE KEY `accId__UNIQUE` (`accId`)
);

--  晋煤自己资金账户
create table jm_self_bankCashAccount(
  id BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  accId varchar(18) NOT NULL COMMENT '系统内资金账户id',
  bankAccountName varchar(50) NOT NULL COMMENT '银行资金账户名称',
  bankAccountNum varchar(30) NOT NULL COMMENT '银行资金账户号',
  bankId  INT(2) NOT NULL  COMMENT  '资金账号所属银行id',
  accountType INT(1) NOT NULL  COMMENT  '资金账号类型 1 交易账户 2 保证金账户',
  available  TINYINT(1) NOT NULL  COMMENT '1 可用 0 不可用',
  createDate DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  createBy   VARCHAR(20)   NOT NULL COMMENT '操作者用户Id',
   UNIQUE KEY `accId_accountType_UNIQUE` (`bankId`,`accountType`)
);

--  晋煤自己银行卡
CREATE TABLE jm_self_bankCard (
    id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    cardNum VARCHAR(45) NOT NULL COMMENT '银行卡号码',
    childBankCode BIGINT(20) NOT NULL COMMENT '开户支行联行号',
    childBankName VARCHAR(30) NOT NULL COMMENT '开户支行联行号',
    cardAccountName VARCHAR(50) NOT NULL COMMENT '账户名称',
    status INT(1)    DEFAULT  NULL   COMMENT '1 已打款 2 绑定成功  3 绑定失败',
    validateCount  INT(1) DEFAULT  0   COMMENT '验证次数',
    validated  TINYINT(1) DEFAULT  0   COMMENT '1 已验证  0 未验证',
    validateAmount DECIMAL(3,2) DEFAULT  NULL   COMMENT '验证金额',
    tradeId VARCHAR(19)   NULL COMMENT '交易Id',
    createBy VARCHAR(20) NOT NULL COMMENT '创建人',
    createDate DATETIME NOT NULL COMMENT '创建日期',
    deleted TINYINT(1) DEFAULT  0 COMMENT '1 删除 0 未删除',
    bankMsg VARCHAR(200) NULL  COMMENT  '银行备注'
);