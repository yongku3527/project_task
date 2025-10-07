# 生产管理系统前端页面

## 功能概述

这个前端页面提供了一个现代化的生产管理系统界面，支持线路板、半成品和灯板插件半成品的增删改查操作。

## 技术栈

- **HTML5 + CSS3**: 现代化的界面设计
- **Bootstrap 5**: 响应式UI框架
- **JavaScript (ES6+)**: 前端逻辑处理
- **Fetch API**: 与后端API通信

## 功能特性

### 1. 线路板管理
- ✅ 新增线路板
- ✅ 编辑线路板信息
- ✅ 删除线路板（逻辑删除）
- ✅ 分页查询
- ✅ 多条件搜索（编码、名称、状态）

### 2. 半成品管理
- ✅ 新增半成品
- ✅ 编辑半成品信息
- ✅ 删除半成品（逻辑删除）
- ✅ 分页查询
- ✅ 关联线路板选择
- ✅ 按线路板筛选

### 3. 灯板插件管理
- ✅ 新增灯板插件
- ✅ 编辑灯板插件信息
- ✅ 删除灯板插件（逻辑删除）
- ✅ 分页查询
- ✅ 关联半成品选择
- ✅ 按半成品筛选

### 4. 用户体验
- 🎨 现代化渐变设计
- 📱 完全响应式布局
- ⚡ 流畅的动画效果
- 🔍 智能搜索功能
- 💾 实时数据加载
- 🔄 友好的错误处理

## 使用方法

### 1. 启动后端服务
确保Spring Boot应用已启动，运行在 `http://localhost:8083`

### 2. 访问前端页面
在浏览器中打开 `http://localhost:8083/index.html`

### 3. 基本操作
- **新增**: 点击"新增"按钮，填写表单后保存
- **编辑**: 点击编辑图标，修改信息后保存
- **删除**: 点击删除图标，确认后删除
- **搜索**: 在搜索框输入条件，按回车或点击搜索按钮
- **切换模块**: 点击左侧导航栏切换不同管理模块

## 界面截图

### 主界面
- 现代化侧边栏导航
- 渐变色彩设计
- 卡片式布局

### 数据表格
- 清晰的数据展示
- 状态标签显示
- 操作按钮集成

### 模态框表单
- 美观的表单设计
- 下拉选择关联数据
- 实时验证

## API接口

前端页面调用以下后端接口：

### 线路板接口
- `GET /circuit-board/list` - 分页查询
- `POST /circuit-board/add` - 新增
- `PUT /circuit-board/update` - 更新
- `DELETE /circuit-board/delete/{id}` - 删除
- `GET /circuit-board/get/{id}` - 查询详情

### 半成品接口
- `GET /semi-product/list` - 分页查询
- `POST /semi-product/add` - 新增
- `PUT /semi-product/update` - 更新
- `DELETE /semi-product/delete/{id}` - 删除
- `GET /semi-product/get/{id}` - 查询详情

### 灯板插件接口
- `GET /led-board-plugin-semi-product/list` - 分页查询
- `POST /led-board-plugin-semi-product/add` - 新增
- `PUT /led-board-plugin-semi-product/update` - 更新
- `DELETE /led-board-plugin-semi-product/delete/{id}` - 删除
- `GET /led-board-plugin-semi-product/get/{id}` - 查询详情

## 文件结构

```
static/
├── index.html      # 主页面
├── app.js         # JavaScript逻辑
└── README.md      # 说明文档
```

## 浏览器兼容性

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 11+
- ✅ Edge 79+

## 注意事项

1. 确保后端服务已正确启动
2. 检查浏览器控制台是否有错误信息
3. 如遇到跨域问题，请检查后端CORS配置
4. 数据加载失败时请检查网络连接

## 扩展功能建议

- [ ] 文件上传功能
- [ ] 数据导出功能
- [ ] 批量操作功能
- [ ] 高级搜索功能
- [ ] 数据可视化图表
- [ ] 用户权限管理