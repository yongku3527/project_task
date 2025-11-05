-- 基础表结构SQL脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ding_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ding_db;

-- 文件信息表（不继承base类）
CREATE TABLE IF NOT EXISTS `file_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name` varchar(255) NOT NULL COMMENT '文件名',
    `original_name` varchar(255) NOT NULL COMMENT '原名',
    `file_suffix` varchar(50) NOT NULL COMMENT '后缀名',
    `file_url` varchar(500) NOT NULL COMMENT 'URL地址',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态（0：禁用，1：启用）',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 线路板表
CREATE TABLE IF NOT EXISTS `circuit_board` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `board_code` varchar(100) NOT NULL COMMENT '线路板编号',
    `board_name` varchar(200) NOT NULL COMMENT '线路板名称',
    `file_id` bigint DEFAULT NULL COMMENT '线路板文件ID',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态（0：禁用，1：启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标识（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_board_code` (`board_code`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_circuit_board_file` FOREIGN KEY (`file_id`) REFERENCES file_info (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线路板表';

-- 半成品表
CREATE TABLE IF NOT EXISTS `semi_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `circuit_board_id` bigint NOT NULL COMMENT '线路板主键（逻辑外键）',
    `semi_product_code` varchar(100) NOT NULL COMMENT '半成品编号',
    `semi_product_name` varchar(200) NOT NULL COMMENT '半成品名称',
    `schematic_file_id` bigint DEFAULT NULL COMMENT '原理图文件ID',
    `smt_file_id` bigint DEFAULT NULL COMMENT '贴片图文件ID',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态（0：禁用，1：启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标识（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_semi_product_code` (`semi_product_code`),
    KEY `idx_circuit_board_id` (`circuit_board_id`),
    KEY `idx_schematic_file_id` (`schematic_file_id`),
    KEY `idx_smt_file_id` (`smt_file_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_semi_product_circuit_board` FOREIGN KEY (`circuit_board_id`) REFERENCES `circuit_board` (`id`),
    CONSTRAINT `fk_semi_product_schematic_file` FOREIGN KEY (`schematic_file_id`) REFERENCES file_info (`id`),
    CONSTRAINT `fk_semi_product_smt_file` FOREIGN KEY (`smt_file_id`) REFERENCES file_info (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='半成品表';

-- 灯板插件半成品表
CREATE TABLE IF NOT EXISTS `led_board_plugin_semi_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `semi_product_id` bigint NOT NULL COMMENT '半成品主键（逻辑外键）',
    `led_board_plugin_code` varchar(100) NOT NULL COMMENT '灯板插件半成品编号',
    `led_board_plugin_name` varchar(200) NOT NULL COMMENT '灯板插件半成品名称',
    `file_id` bigint DEFAULT NULL COMMENT '文件ID',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态（0：禁用，1：启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标识（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_led_board_plugin_code` (`led_board_plugin_code`),
    KEY `idx_semi_product_id` (`semi_product_id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_led_board_plugin_semi_product` FOREIGN KEY (`semi_product_id`) REFERENCES `semi_product` (`id`),
    CONSTRAINT `fk_led_board_plugin_file` FOREIGN KEY (`file_id`) REFERENCES file_info (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灯板插件半成品表';