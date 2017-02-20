
alter table jm_tender ADD COLUMN releaseDate DATETIME DEFAULT now() NOT NULL;

alter table jm_tender ADD COLUMN visitorsCount  BIGINT(20) DEFAULT 0;


create table jm_bid_subscribe(
id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
tenderId BIGINT(20) NOT NULL COMMENT '招标id',
phone VARCHAR(12) NOT NULL COMMENT '电话',
logined TINYINT(1) NOT NULL COMMENT '是否登陆',
ip VARCHAR(4) NOT NULL COMMENT 'ip地址',
createDate  DATETIME NOT NULL DEFAULT  now() COMMENT '创建时间',
noticeDate  DATETIME DEFAULT NULL  COMMENT '通知时间'
);