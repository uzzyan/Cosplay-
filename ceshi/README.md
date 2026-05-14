# U次元 Cosplay商城系统

> 作者: uzzyan

这是一个基于 Spring Boot 后端和 Vue.js 前端的全栈商城系统。

## 🛠 技术栈

### 后端 (`mall-server`)
- **语言**: Java 1.8
- **框架**: Spring Boot 2.5.6
- **数据库**: MySQL
- **ORM**: MyBatis Plus 3.5.1
- **缓存**: Redis
- **认证**: JWT
- **工具**: Maven, Swagger, Druid, Hutool

### 前端 (`mall-web`)
- **框架**: Vue.js 2
- **UI 库**: Element UI
- **状态管理**: Vuex
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **图表**: ECharts

## 📋 环境要求

在运行项目之前，请确保您的机器上已安装以下软件：
- **JDK 1.8**
- **Node.js** (推荐 v14+)
- **MySQL** (5.7 或 8.0)
- **Redis**
- **Maven**

## 🚀 快速开始

### 1. 数据库设置
1. 创建一个名为 `s003` 的 MySQL 数据库。
2. 将项目根目录下的 `s003.sql` 文件导入到 `s003` 数据库中。
3. **重要配置说明**:
   后端默认配置连接 **3308** 端口的 MySQL。
   如果您的 MySQL 运行在标准端口 **3306**，请修改 `mall-server/src/main/resources/application.yml` 文件：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/s003?...
   ```
   如果您的数据库用户名和密码不是 `root` / `123456`，也请一并更新。

### 2. Redis 设置
项目在 `redis` 目录下包含了一个 Windows 版的 Redis 服务器。
1. 进入 `redis` 目录。
2. 运行 `RedisStarter.bat` 启动 Redis 服务器。
   - 或者，确保您本地的 Redis 服务已在 `127.0.0.1:6379` 运行。

### 3. 后端设置 (`mall-server`)
1. 打开 `mall-server` 目录。
2. 如有必要，更新 `src/main/resources/application.yml` 中的数据库配置。
3. 运行应用程序：
   ```bash
   mvn spring-boot:run
   ```
   或者运行主类 `com.rabbiter.em.MallApplication`。
4. 后端服务器将在 **http://localhost:9191** 启动。
   - API 文档: http://localhost:9191/swagger-ui.html (如果已启用)

### 4. 前端设置 (`mall-web`)
1. 打开 `mall-web` 目录。
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run serve
   ```
4. 前端将在 **http://localhost:9192** 运行。

## 🔑 默认账号

| 角色 | 用户名 | 密码 |
|-------|----------|----------|
| 管理员 | admin | 123456 |
| 用户 | user | 123456 |

## 📂 项目结构

```
cosplay_mall/
├── mall-server/       # 后端源码 (Spring Boot)
├── mall-web/          # 前端源码 (Vue.js)
├── redis/             # Redis 服务 (Windows)
├── s003.sql           # 数据库初始化脚本
└── README.md          # 项目文档
```
