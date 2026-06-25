-- BOM功能权限初始化脚本
-- 执行前请确保已创建 sys_permission 表

-- 生产材料模块
INSERT INTO sys_permission (perm_name, perm_code, parent_id, perm_type, status) 
VALUES ('生产材料', 'material:model', 0, 1, 1);

SET @material_model_id = LAST_INSERT_ID();

-- BOM信息菜单
INSERT INTO sys_permission (perm_name, perm_code, parent_id, perm_type, status) 
VALUES ('BOM信息', 'bom:menu', @material_model_id, 1, 1);

-- BOM操作按钮权限
INSERT INTO sys_permission (perm_name, perm_code, parent_id, perm_type, status) VALUES
('BOM查看', 'bom:view', 0, 2, 1),
('BOM新增', 'bom:add', 0, 2, 1),
('BOM编辑', 'bom:update', 0, 2, 1),
('BOM删除', 'bom:delete', 0, 2, 1),
('BOM上传', 'bom:upload', 0, 2, 1);
