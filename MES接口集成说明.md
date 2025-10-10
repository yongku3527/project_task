# MES系统钉钉接口集成说明

## 功能概述

根据接口文档要求，已实现MES系统钉钉接口集成，当用户输入线路板编号、半成品编号或灯板插件半成品编号时，自动调用MES接口获取物料信息，并将返回结果中的itemName字段值填充至对应名称表单。

## 接口实现

### 后端接口（Java）

创建了`MesController.java`，提供以下接口：

1. **单个物料查询接口**
   - URL: `GET /mes/item-info?itemCode={物料编码}`
   - 返回格式: 
   ```json
   {
     "code": 200,
     "msg": null,
     "data": {
       "itemName": "物料名称",
       "itemType": "物料类型",
       "unit": "单位",
       "itemCode": "物料编码",
       "description": "描述",
       "status": "ACTIVE"
     }
   }
   ```

2. **批量物料查询接口**
   - URL: `POST /mes/item-info/batch`
   - 请求格式: `{"物料编码1":"物料编码1","物料编码2":"物料编码2"}`
   - 返回格式: 包含itemInfoList数组的批量查询结果

### 前端集成（Vue）

在`CircuitBoardManager.vue`中实现了自动查询功能：

1. **输入监听**
   - 线路板编码输入框: `@input="handleBoardCodeInput"`
   - 半成品编码输入框: `@input="handleSemiProductCodeInput"`
   - 灯板插件编码输入框: `@input="handleLedBoardPluginCodeInput"`

2. **自动查询逻辑**
   - 用户输入后1秒延迟查询，避免频繁调用
   - 使用定时器管理，防止内存泄漏
   - 组件卸载时清理所有定时器

3. **数据填充**
   - 成功获取itemName后自动填充到对应名称字段
   - 显示成功提示信息
   - 接口调用失败时仅控制台警告，不影响用户体验

## 测试验证

### 测试页面

创建了专门的测试页面`MesTest.vue`，可通过导航菜单"MES测试"访问，提供：
- 单个物料查询测试
- 批量物料查询测试
- 跳转到线路板管理页面进行实际功能测试

### 接口测试

通过命令行验证接口正常工作：
```bash
# 单个查询
curl "http://localhost:8083/mes/item-info?itemCode=TEST001"

# 批量查询
Invoke-RestMethod -Uri "http://localhost:8083/mes/item-info/batch" -Method POST -Body '{"TEST001":"TEST001","TEST002":"TEST002"}' -ContentType "application/json"
```

## 使用说明

1. **访问线路板管理页面**: http://localhost:8081/circuit-board
2. **添加/编辑线路板**: 在"线路板编码"字段输入编号，系统自动查询MES接口并填充"线路板名称"
3. **添加/编辑半成品**: 在"半成品编码"字段输入编号，系统自动查询MES接口并填充"半成品名称"
4. **添加/编辑灯板插件**: 在"灯板插件半成品编号"字段输入编号，系统自动查询MES接口并填充"灯板插件名称"

## 技术特点

- **智能延迟**: 1秒输入延迟，避免频繁API调用
- **错误处理**: 接口失败时仅记录日志，不影响用户操作
- **内存管理**: 完善的定时器清理机制
- **用户体验**: 成功获取数据时显示友好提示

## 注意事项

- MES接口目前返回模拟数据，实际使用时需要替换为真实的MES系统接口
- 接口URL: `http://192.168.90.64:8083/mes/item-info`
- 前端开发服务器: `http://localhost:8081`
- 后端服务端口: `8083`