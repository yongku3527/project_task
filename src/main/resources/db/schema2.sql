

USE ding_db;

-- BOM信息主表
CREATE TABLE IF NOT EXISTS `bom_info` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `item_code` varchar(100) NOT NULL COMMENT '物料编码',
                                          `item_name` varchar(255) DEFAULT NULL COMMENT '物料名称',
                                          `item_spec` varchar(500) DEFAULT NULL COMMENT '规格型号',
                                          `status` int NOT NULL DEFAULT '1' COMMENT '状态（0：禁用，1：启用）',
                                          `create_by` varchar(100) DEFAULT NULL COMMENT '创建人',
                                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                          `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标识（0：未删除，1：已删除）',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `uk_item_code` (`item_code`),
                                          KEY `idx_status` (`status`),
                                          KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BOM信息主表';

-- BOM明细表
CREATE TABLE IF NOT EXISTS `bom_detail` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `bom_id` bigint NOT NULL COMMENT 'BOM主表ID',
                                            `item_code` varchar(100) NOT NULL COMMENT '物料编码',
                                            `item_name` varchar(255) DEFAULT NULL COMMENT '物料名称',
                                            `item_spec` varchar(500) DEFAULT NULL COMMENT '规格型号',
                                            `designators` text COMMENT '位号，多个以逗号分隔',
                                            `quantity` int NOT NULL DEFAULT '0' COMMENT '数量',
                                            `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
                                            `status` int NOT NULL DEFAULT '1' COMMENT '状态（0：禁用，1：启用）',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                            PRIMARY KEY (`id`),
                                            KEY `idx_bom_id` (`bom_id`),
                                            KEY `idx_item_code` (`item_code`),
                                            KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BOM明细表';

-- BOM操作日志表
CREATE TABLE IF NOT EXISTS `bom_operation_log` (
                                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                   `bom_id` bigint NOT NULL COMMENT 'BOM主表ID',
                                                   `detail_id` bigint DEFAULT NULL COMMENT 'BOM明细ID',
                                                   `operation_type` varchar(50) NOT NULL COMMENT '操作类型（CREATE/UPLOAD/UPDATE/DELETE）',
                                                   `field_name` varchar(100) DEFAULT NULL COMMENT '修改字段名',
                                                   `old_value` text COMMENT '修改前值',
                                                   `new_value` text COMMENT '修改后值',
                                                   `operate_by` varchar(100) DEFAULT NULL COMMENT '操作人',
                                                   `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                                   `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                                   PRIMARY KEY (`id`),
                                                   KEY `idx_bom_id` (`bom_id`),
                                                   KEY `idx_detail_id` (`detail_id`),
                                                   KEY `idx_operation_type` (`operation_type`),
                                                   KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BOM操作日志表';