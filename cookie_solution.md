# Cookie问题解决方案

## 问题描述
后端无法获取到请求中的cookie，导致Sa-Token无法正确验证用户身份。

## 问题分析
1. 前端发送请求时默认不包含cookie
2. 后端登录接口没有设置cookie到响应中
3. Sa-Token配置没有启用从cookie中读取token

## 解决方案

### 1. 前端修改
- 修改`request.ts`文件，在baseConfig中添加`credentials: 'include'`配置，使前端请求包含cookie
- 修改`vite.config.ts`文件，在代理配置中添加`credentials: true`，确保代理服务器正确处理cookie

### 2. 后端修改
- 修改`AuthController.java`的登录方法，在响应中添加cookie
- 修改`AuthController.java`的注销方法，清除cookie
- 修改`application.properties`文件，添加`sa-token.is-read-cookie=true`配置，使Sa-Token从cookie中读取token
- 创建`CookieTestController.java`，用于测试cookie功能

### 3. 测试验证
- 创建`CookieTest.vue`前端页面，用于测试登录和cookie功能
- 添加路由配置，访问路径为`/cookie-test`

## 修改文件列表
1. `project_task/src/utils/request.ts` - 添加credentials配置
2. `project_task/vite.config.ts` - 添加代理credentials配置
3. `src/main/java/com/quanhai/dingdingdemo/satoken/controller/AuthController.java` - 添加cookie设置和清除
4. `src/main/resources/application.properties` - 添加Sa-Token cookie读取配置
5. `src/main/java/com/quanhai/dingdingdemo/satoken/controller/CookieTestController.java` - 新增cookie测试接口
6. `project_task/src/views/CookieTest.vue` - 新增cookie测试页面
7. `project_task/src/router/index.ts` - 添加cookie测试页面路由

## 测试步骤
1. 启动后端服务：`mvn spring-boot:run`
2. 启动前端服务：`npm run dev`
3. 访问cookie测试页面：http://localhost:8081/cookie-test
4. 输入用户名和密码（admin/admin123）进行登录
5. 点击"测试Cookie"按钮，查看后端是否能正确获取cookie
6. 点击"退出"按钮，测试cookie清除功能

## 技术要点
1. 跨域请求中浏览器默认不发送cookie，需要在前端请求配置中添加`credentials: 'include'`
2. Spring Boot 3.x使用jakarta.servlet替代javax.servlet
3. Sa-Token可以通过配置从cookie中读取token
4. 前端代理服务器需要配置credentials以支持cookie传输

## 注意事项
1. 确保后端CORS配置允许credentials
2. cookie的path应设置为"/"，确保在整个应用中可用
3. cookie的maxAge应与Sa-Token的timeout保持一致
4. 前端和后端的token名称应保持一致（satoken）