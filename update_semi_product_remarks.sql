-- 为半成品表添加备注字段
ALTER TABLE semi_product ADD COLUMN remarks VARCHAR(500) DEFAULT NULL COMMENT '备注信息';

