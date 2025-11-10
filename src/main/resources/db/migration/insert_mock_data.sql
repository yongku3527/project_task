

-- 经验库表虚拟数据
INSERT INTO knowledge_info (
    product_model, 
    product_category, 
    failure_mode, 
    issue_source, 
    issue_description, 
    issue_attachments_id, 
    root_cause, 
    permanent_action, 
    action_attachments_id, 
    application_scene, 
    role, 
    create_time, 
    update_time
) VALUES
('X1 Pro', '智能手机', '不开机', '生产线', '设备无法正常开机，按下电源键无任何反应', 1, '电源管理芯片故障', '更换电源管理芯片并进行固件升级', 3, '生产线质检环节', 'gcsRole,csRole', NOW(), NOW()),
('X2 Max', '智能手机', '花屏', '市场', '屏幕显示异常，出现彩色条纹', 2, '显示驱动程序兼容性问题', '更新显示驱动程序并优化显示参数', 4, '用户日常使用', 'gcsRole,chejianRole,csRole', NOW(), NOW()),
('Y1 Series', '平板电脑', '烧屏', '客退', '长时间显示同一画面后出现残影', 3, 'OLED屏幕老化', '调整屏幕亮度自动调节功能，建议用户避免长时间静态显示', 5, '长时间办公场景', 'gcsRole,chejianRole,csRole', NOW(), NOW()),
('Z3 Ultra', '笔记本电脑', '不开机', 'IQC', '进料检验时发现部分设备无法开机', 4, '主板电源线路设计缺陷', '优化主板电源线路设计，增加过流保护', 6, '产品入库检验', 'gcsRole,csRole', NOW(), NOW()),
('X1 Pro', '智能手机', '触摸失灵', '可靠性实验', '高温高湿环境下触摸屏响应迟缓', 5, '触摸屏控制器散热不足', '增加散热片并优化触摸驱动算法', 7, '高温高湿环境', 'gcsRole,csRole', NOW(), NOW()),
('Y2 Plus', '平板电脑', '花屏', '生产线', '组装完成后屏幕显示异常', 6, '排线连接不良', '改进排线连接工艺，增加固定卡扣', 8, '产品组装环节', 'chejianRole,csRole', NOW(), NOW()),
('Z4 Pro', '笔记本电脑', '系统崩溃', '市场', '运行特定应用时系统蓝屏重启', 7, '内存管理驱动冲突', '更新内存管理驱动并优化内存分配策略', 1, '多任务办公场景', 'chejianRole,csRole', NOW(), NOW()),
('X3 Lite', '智能手机', '充电异常', '客退', '充电速度明显变慢，发热严重', 8, '充电管理IC参数配置错误', '重新校准充电管理IC参数，优化充电曲线', 2, '夜间充电场景', 'chejianRole,csRole',, NOW(), NOW());

-- 完成状态表虚拟数据
INSERT INTO completion_status (knowledge_info_id, user_id, completion_status, create_time) VALUES
-- 用户1001的完成状态
(1, '7', 2, NOW()),  -- 已掌握
(2, '7', 1, NOW()),  -- 已学习
(3, '7', 0, NOW()),  -- 待完成
(4, '7', 2, NOW()),  -- 已掌握
(5, '7', 1, NOW()),  -- 已学习

-- 用户1002的完成状态
(2, '8', 2, NOW()),  -- 已掌握
(3, '8', 2, NOW()),  -- 已掌握
(6, '8', 1, NOW()),  -- 已学习
(7, '8', 0, NOW()),  -- 待完成
(8, '8', 1, NOW()),  -- 已学习

-- 用户1003的完成状态（工程师角色）
(1, '9', 2, NOW()),  -- 已掌握
(3, '9', 2, NOW()),  -- 已掌握
(4, '9', 2, NOW()),  -- 已掌握
(5, '9', 1, NOW()),  -- 已学习
(6, '9', 2, NOW()),  -- 已掌握
(7, '9', 2, NOW()),  -- 已掌握
(8, '9', 1, NOW()),  -- 已学习

