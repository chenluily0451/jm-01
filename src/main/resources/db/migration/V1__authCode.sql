CREATE TABLE jm_authCode
(
    id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    phone VARCHAR(12) NOT NULL,
    code VARCHAR(10) NOT NULL,
    userId BIGINT(20) unsigned,
    expireTime DATETIME NOT NULL,
    validateTime DATETIME,
    validated TINYINT(1) DEFAULT '0' NOT NULL,
    validateType VARCHAR(50),
    createDate DATETIME NOT NULL COMMENT '创建时间'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `jm_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `securePhone` varchar(12) NOT NULL COMMENT '手机号',
  `companyId` varchar(40) DEFAULT NULL COMMENT '公司Id',
  `isActive` tinyint(1) DEFAULT '1' COMMENT '1 可用 0 禁用',
  `role` varchar(20) NOT NULL COMMENT '用户角色',
  `password` varchar(40) NOT NULL COMMENT '密码',
  `passwordSalt` varchar(40) NOT NULL COMMENT '密码盐',
  `payPassword` varchar(40) DEFAULT NULL COMMENT '支付密码',
  `payPasswordSalt` varchar(40) DEFAULT NULL COMMENT '支付密码盐',
  `createDate` datetime NOT NULL COMMENT '创建时间',
  `createBy` varchar(45) NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`securePhone`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `jm_company` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '公司名称',
  `status` int(1) NOT NULL COMMENT '1 审核通过  2 审核未通过 3 待审核',
  `registerAddress` varchar(60) NOT NULL COMMENT '注册地址',
  `registerCode` varchar(30) NOT NULL COMMENT '注册号',
  `legalPerson` varchar(20) DEFAULT NULL COMMENT '法人',
  `proxyPerson` varchar(20) NOT NULL COMMENT '委托人',
  `companyPhone` varchar(15) NOT NULL COMMENT '公司电话',
  `businessLicensePic` varchar(200) DEFAULT NULL COMMENT '营业执照',
  `taxRegistrationPic` varchar(200) DEFAULT NULL COMMENT '税务登记证',
  `organizationCodePic` varchar(200) DEFAULT NULL COMMENT '组织机构代码证',
  `openingLicensePic` varchar(200) DEFAULT NULL COMMENT '开户许可证',
  `creditCodePic` varchar(200) DEFAULT NULL COMMENT '信用证',
  `proxyPic` varchar(200) DEFAULT NULL COMMENT '委托人证件',
  `idCardPic` varchar(200) DEFAULT NULL COMMENT '身份证',
  `createDate` datetime NOT NULL COMMENT '创建时间',
  `createBy` varchar(45) NOT NULL COMMENT '创建人',
  `auditMsg` varchar(200) DEFAULT NULL COMMENT '审核意见',
  `auditDate` datetime DEFAULT NULL COMMENT '审核时间',
  `auditBy` varchar(40) DEFAULT NULL COMMENT '审核人',
  `UUID` varchar(40) DEFAULT NULL COMMENT '公司唯一ID',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `jm_findPwdLog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `securePhone` varchar(12) NOT NULL COMMENT '手机号',
  `UUID` varchar(32) NOT NULL COMMENT 'UUID',
  `success` tinyint(1) NOT NULL COMMENT '创建成功标识',
  `createDate` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE `jm_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userName` varchar(40) NOT NULL COMMENT '用户名',
  `securePhone` varchar(12) NOT NULL COMMENT '手机号',
  `role` varchar(20) NOT NULL COMMENT '角色',
  `password` varchar(40) NOT NULL COMMENT '密码',
  `passwordSalt` varchar(40) NOT NULL COMMENT '密码盐',
  `payPassword` varchar(40) NOT NULL COMMENT '支付密码',
  `payPasswordSalt` varchar(40) NOT NULL COMMENT '支付密码盐',
  `isActive` tinyint(1) DEFAULT '1' COMMENT '1 可用 0 禁用',
  `createDate` datetime NOT NULL COMMENT '创建时间',
  `createBy` varchar(45) NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`securePhone`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE `jm_dateSeq` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL DEFAULT '',
  `date` varchar(8) NOT NULL DEFAULT '',
  `cnt` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP FUNCTION IF EXISTS `dateSeq_next_value`;

DELIMITER ||
CREATE FUNCTION dateSeq_next_value(p_name VARCHAR(30)) returns VARCHAR(200)
BEGIN
 DECLARE cur_val VARCHAR(30);
 DECLARE cur_date int(11);
  SELECT CURDATE() + 0 INTO cur_date;
  INSERT jm_dateSeq (`name`,`date`, cnt) VALUES (p_name, cur_date, 1) ON DUPLICATE KEY UPDATE cnt=cnt+1;

  SELECT CASE LENGTH(cnt)
     WHEN 1 THEN CONCAT('000', cnt)
     WHEN 2 THEN CONCAT('00', cnt)
     WHEN 3 THEN CONCAT('0', cnt)
     ELSE cnt
     END AS cur_value INTO cur_val FROM jm_dateSeq WHERE `name`=p_name AND `date`=cur_date limit 1;
  return CONCAT(p_name, cur_date,  cur_val);
END ||