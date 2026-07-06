-- =====================================================
-- 宜搭管理模块 - 流程创建记录表 & 邮件发送记录表
-- =====================================================

-- 流程创建记录表
CREATE TABLE IF NOT EXISTS `ding_db`.`yi_da_prcs_creation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `item_code` VARCHAR(100) NOT NULL COMMENT '物料编码',
    `prcs_instance_code` VARCHAR(200) NOT NULL COMMENT '原流程实例号',
    `new_instance_id` VARCHAR(200) DEFAULT NULL COMMENT '新创建的流程实例ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待创建, 1=创建成功, 2=创建失败',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_item_code` (`item_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宜搭流程创建记录表';

-- 邮件发送记录表
CREATE TABLE IF NOT EXISTS `ding_db`.`yi_da_mail_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    `file_name` VARCHAR(200) DEFAULT NULL COMMENT '文件名',
    `pcb_name` VARCHAR(200) DEFAULT NULL COMMENT 'PCB名称',
    `gang_wang_name` VARCHAR(200) DEFAULT NULL COMMENT '钢网名称',
    `gang_wang_code` VARCHAR(200) DEFAULT NULL COMMENT '钢网编号',
    `originator_user` VARCHAR(100) DEFAULT NULL COMMENT '发起用户ID',
    `user_name` VARCHAR(100) DEFAULT NULL COMMENT '责任工程师姓名',
    `recipient` VARCHAR(200) DEFAULT NULL COMMENT '收件人',
    `subject` VARCHAR(200) DEFAULT NULL COMMENT '邮件主题',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 1=发送成功, 2=发送失败',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    INDEX `idx_pcb_name` (`pcb_name`),
    INDEX `idx_send_time` (`send_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宜搭钢网邮件发送记录表';

-- =====================================================
-- 存量数据迁移：将 code_and_prcs_ins 已有数据迁移到日志表
-- =====================================================
INSERT IGNORE INTO `ding_db`.`yi_da_prcs_creation_log` (`item_code`, `prcs_instance_code`, `status`, `create_time`)
SELECT `item_code`, `prcs_instance_code`, 0, NOW()
FROM `ding_db`.`code_and_prcs_ins`;
