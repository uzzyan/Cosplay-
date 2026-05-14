# Cosplay商城系统 - 完整架构图集

> 文档版本：v1.0  
> 更新日期：2026-04-15  
> 系统版本：v3.0（Vue 3重构完成）

---

## 📋 目录

1. [系统架构图](#一系统架构图)
2. [数据库ER图](#二数据库er图)
3. [部署架构图](#三部署架构图)
4. [功能模块图](#四功能模块图)

---

## 一、系统架构图

### 1.1 整体架构概览

```mermaid
graph TB
    subgraph 客户端层
        Browser[浏览器]
        Mobile[移动端浏览器]
    end
    
    subgraph 接入层
        Nginx[Nginx反向代理<br/>端口: 80/443]
    end
    
    subgraph 前端层
        VueFront[Vue 3 用户端<br/>Element Plus<br/>端口: 9194]
        VueAdmin[Vue 3 管理端<br/>Element Plus<br/>端口: 9194]
    end
    
    subgraph 后端层
        SpringBoot[Spring Boot 2.5.6<br/>REST API<br/>端口: 9191]
        
        subgraph 后端模块
            Controller[Controller层<br/>16个控制器]
            Service[Service层<br/>18个服务]
            Mapper[Mapper层<br/>18个数据访问]
            Interceptor[拦截器<br/>JWT认证]
        end
    end
    
    subgraph 数据层
        MySQL[(MySQL 8.0<br/>数据库: s003<br/>端口: 3309)]
        Redis[(Redis 7.x<br/>缓存服务<br/>端口: 6379)]
        FileStorage[文件存储<br/>本地文件系统]
    end
    
    Browser --> Nginx
    Mobile --> Nginx
    Nginx --> VueFront
    Nginx --> VueAdmin
    VueFront --> SpringBoot
    VueAdmin --> SpringBoot
    SpringBoot --> Controller
    Controller --> Service
    Service --> Mapper
    Controller --> Interceptor
    Mapper --> MySQL
    Service --> Redis
    Controller --> FileStorage
    
    style Browser fill:#87CEEB
    style Mobile fill:#87CEEB
    style Nginx fill:#FFD700
    style VueFront fill:#90EE90
    style VueAdmin fill:#90EE90
    style SpringBoot fill:#FFA07A
    style MySQL fill:#DDA0DD
    style Redis fill:#DDA0DD
    style FileStorage fill:#DDA0DD
```

### 1.2 技术栈架构

```mermaid
graph LR
    subgraph 前端技术栈
        Vue3[Vue 3.0<br/>Composition API]
        ElementPlus[Element Plus 2.9]
        VueRouter[Vue Router 4]
        Vuex[Vuex 4]
        Axios[Axios HTTP]
        ECharts[ECharts 5.x]
    end
    
    subgraph 后端技术栈
        SpringBoot[Spring Boot 2.5.6]
        MyBatisPlus[MyBatis-Plus 3.5.5]
        JWT[JWT认证]
        Redis[Redis缓存]
        Hutool[Hutool工具]
        BCrypt[BCrypt加密]
    end
    
    subgraph 基础设施
        MySQL_DB[(MySQL 8.0)]
        Redis_DB[(Redis 7.x)]
        Nginx_Srv[Nginx]
        Maven[Maven构建]
        NodeJS[Node.js 14+]
    end
    
    Vue3 --> ElementPlus
    Vue3 --> VueRouter
    Vue3 --> Vuex
    Vue3 --> Axios
    Vue3 --> ECharts
    
    SpringBoot --> MyBatisPlus
    SpringBoot --> JWT
    SpringBoot --> Redis
    SpringBoot --> Hutool
    SpringBoot --> BCrypt
    
    SpringBoot --> MySQL_DB
    SpringBoot --> Redis_DB
    Vue3 --> Nginx_Srv
    SpringBoot --> Maven
    Vue3 --> NodeJS
    
    style Vue3 fill:#42B883
    style SpringBoot fill:#6DB33F
    style MySQL_DB fill:#4479A1
    style Redis_DB fill:#DC382D
```

### 1.3 数据流架构

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Nginx as Nginx
    participant Front as Vue前端
    participant API as Spring Boot API
    participant Cache as Redis缓存
    participant DB as MySQL数据库
    participant File as 文件存储
    
    Client->>Nginx: HTTP请求
    Nginx->>Front: 静态资源/代理
    Front->>API: REST API请求<br/>(带Token)
    
    API->>API: JWT拦截器验证
    API->>Cache: 查询缓存
    
    alt 缓存命中
        Cache-->>API: 返回缓存数据
    else 缓存未命中
        API->>DB: 查询数据库
        DB-->>API: 返回数据
        API->>Cache: 写入缓存
    end
    
    API-->>Front: JSON响应
    Front-->>Nginx: 渲染页面
    Nginx-->>Client: 展示结果
    
    Note over API,File: 文件上传流程
    Client->>API: 上传文件
    API->>File: 保存到本地
    File-->>API: 返回文件路径
    API-->>Client: 返回URL
```

---

## 二、数据库ER图

### 2.1 核心实体关系图

```mermaid
erDiagram
    USER ||--o{ ADDRESS : "拥有"
    USER ||--o{ CART : "拥有"
    USER ||--o{ ORDERS : "下单"
    USER ||--o{ COMMENT : "发表"
    USER ||--o{ MESSAGE : "留言"
    USER ||--o{ AFTER_SALE : "申请"
    
    CATEGORY ||--o{ GOOD : "包含"
    CATEGORY ||--o{ ICON : "关联"
    
    GOOD ||--o{ GOOD_STANDARD : "规格"
    GOOD ||--o{ CAROUSEL : "轮播"
    GOOD ||--o{ COMMENT : "评论"
    GOOD ||--o{ CART : "加入购物车"
    
    ORDERS ||--o{ ORDER_GOODS : "包含"
    ORDERS ||--o{ AFTER_SALE : "售后"
    
    ORDER_GOODS }o--|| GOOD : "引用"
    
    USER {
        bigint id PK "用户ID"
        string username "用户名"
        string password "密码(BCrypt)"
        string nickname "昵称"
        string email "邮箱"
        string phone "手机"
        string address "默认地址"
        string avatar_url "头像URL"
        string role "角色(admin/user)"
    }
    
    ADDRESS {
        bigint id PK "地址ID"
        string link_user "收货人"
        string link_address "详细地址"
        string link_phone "联系电话"
        bigint user_id FK "用户ID"
    }
    
    CATEGORY {
        bigint id PK "分类ID"
        string name "分类名称"
    }
    
    GOOD {
        bigint id PK "商品ID"
        string name "商品名称"
        string description "商品描述"
        double discount "折扣"
        bigint sales "销量"
        double sale_money "销售额"
        bigint category_id FK "分类ID"
        string imgs "图片URL"
        datetime create_time "创建时间"
        tinyint recommend "是否推荐"
        tinyint is_delete "逻辑删除"
    }
    
    GOOD_STANDARD {
        bigint id PK "规格ID"
        bigint good_id FK "商品ID"
        string value "规格名称"
        decimal price "价格"
        bigint store "库存"
    }
    
    CART {
        bigint id PK "购物车ID"
        bigint user_id FK "用户ID"
        bigint good_id FK "商品ID"
        int count "数量"
        string standard "规格"
        datetime create_time "加入时间"
    }
    
    ORDERS {
        bigint id PK "订单ID"
        string order_no "订单编号"
        decimal total_price "总价"
        bigint user_id FK "用户ID"
        string link_user "联系人"
        string link_phone "联系电话"
        string link_address "收货地址"
        string state "订单状态"
        datetime create_time "创建时间"
    }
    
    ORDER_GOODS {
        bigint id PK "关联ID"
        bigint order_id FK "订单ID"
        bigint good_id FK "商品ID"
        int count "数量"
        string standard "规格"
    }
    
    COMMENT {
        bigint id PK "评论ID"
        string content "评论内容"
        bigint user_id FK "用户ID"
        bigint good_id FK "商品ID"
        datetime create_time "评论时间"
        string reply "管理员回复"
    }
    
    MESSAGE {
        bigint id PK "留言ID"
        bigint user_id FK "用户ID"
        string title "标题"
        string content "内容"
        string contact "联系方式"
        boolean custom_intent "定制意图"
        string create_time "创建时间"
        string reply "回复内容"
        string reply_time "回复时间"
    }
    
    AFTER_SALE {
        bigint id PK "售后ID"
        string order_no "订单编号"
        bigint user_id FK "用户ID"
        string type "类型(退款/售后)"
        string status "状态"
        string reason "原因"
        string create_time "申请时间"
        string handle_time "处理时间"
    }
    
    CAROUSEL {
        bigint id PK "轮播图ID"
        bigint good_id FK "商品ID"
        int show_order "显示顺序"
        string img "图片URL"
    }
    
    NOTICE {
        bigint id PK "公告ID"
        string title "标题"
        text content "内容"
        datetime create_time "发布时间"
    }
    
    ICON {
        bigint id PK "图标ID"
        string name "图标名称"
        bigint category_id FK "分类ID"
        string img "图标URL"
    }
```

### 2.2 数据库表统计

| 序号 | 表名 | 中文名 | 记录类型 | 关键字段数 |
|------|------|--------|---------|-----------|
| 1 | user | 用户表 | 核心表 | 9 |
| 2 | address | 地址表 | 业务表 | 4 |
| 3 | category | 分类表 | 配置表 | 2 |
| 4 | good | 商品表 | 核心表 | 11 |
| 5 | good_standard | 商品规格表 | 业务表 | 5 |
| 6 | cart | 购物车表 | 业务表 | 6 |
| 7 | orders | 订单表 | 核心表 | 10 |
| 8 | order_goods | 订单商品关联表 | 关联表 | 5 |
| 9 | comment | 评论表 | 业务表 | 6 |
| 10 | message | 留言表 | 业务表 | 9 |
| 11 | after_sale | 售后表 | 业务表 | 7 |
| 12 | carousel | 轮播图表 | 配置表 | 4 |
| 13 | notice | 公告表 | 配置表 | 4 |
| 14 | icon | 图标表 | 配置表 | 4 |
| 15 | sys_file | 文件表 | 系统表 | 6 |
| 16 | avatar | 头像表 | 系统表 | 5 |
| 17 | income | 收入统计表 | 统计表 | 5 |
| 18 | role | 角色表 | 系统表 | 3 |

**总计**：18张表，包含核心表4张、业务表8张、配置表4张、系统表2张

### 2.3 数据表关系说明

```mermaid
graph TB
    subgraph 用户体系
        U[user用户表]
        A[address地址表]
    end
    
    subgraph 商品体系
        C[category分类表]
        G[good商品表]
        GS[good_standard规格表]
        I[icon图标表]
    end
    
    subgraph 交易体系
        CT[cart购物车表]
        O[orders订单表]
        OG[order_goods订单商品表]
        AS[after_sale售后表]
    end
    
    subgraph 互动体系
        CM[comment评论表]
        M[message留言表]
    end
    
    subgraph 运营体系
        CR[carousel轮播图表]
        N[notice公告表]
    end
    
    U --> A
    U --> CT
    U --> O
    U --> CM
    U --> M
    U --> AS
    
    C --> G
    C --> I
    G --> GS
    G --> CR
    
    CT --> G
    O --> OG
    OG --> G
    O --> AS
    G --> CM
    
    style U fill:#FFD700
    style G fill:#FFD700
    style O fill:#FFD700
    style C fill:#87CEEB
    style CR fill:#98FB98
```

---

## 三、部署架构图

### 3.1 生产环境部署架构

```mermaid
graph TB
    subgraph 用户访问层
        User1[PC浏览器]
        User2[移动浏览器]
        CDN[CDN加速<br/>静态资源]
    end
    
    subgraph 负载均衡层
        NginxLB[Nginx负载均衡<br/>端口: 80/443<br/>SSL证书]
    end
    
    subgraph 应用服务层
        subgraph 前端服务
            VueDist1[Vue前端实例1<br/>静态文件]
            VueDist2[Vue前端实例2<br/>静态文件]
        end
        
        subgraph 后端服务
            App1[Spring Boot实例1<br/>端口: 9191]
            App2[Spring Boot实例2<br/>端口: 9191]
        end
    end
    
    subgraph 数据存储层
        MySQLMaster[(MySQL主库<br/>端口: 3306<br/>读写)]
        MySQLSlave[(MySQL从库<br/>端口: 3306<br/>只读)]
        RedisMaster[(Redis主节点<br/>端口: 6379)]
        RedisSlave[(Redis从节点<br/>端口: 6379)]
    end
    
    subgraph 文件存储层
        LocalFile[本地文件存储<br/>/avatar<br/>/file]
        OSS[对象存储OSS<br/>可选]
    end
    
    subgraph 监控运维层
        Monitor[监控告警<br/>Prometheus + Grafana]
        Log[日志系统<br/>ELK Stack]
        Backup[数据备份<br/>定时任务]
    end
    
    User1 --> CDN
    User2 --> CDN
    CDN --> NginxLB
    NginxLB --> VueDist1
    NginxLB --> VueDist2
    NginxLB --> App1
    NginxLB --> App2
    
    App1 --> MySQLMaster
    App2 --> MySQLMaster
    MySQLMaster --> MySQLSlave
    
    App1 --> RedisMaster
    App2 --> RedisMaster
    RedisMaster --> RedisSlave
    
    App1 --> LocalFile
    App2 --> LocalFile
    App1 -.可选.-> OSS
    App2 -.可选.-> OSS
    
    Monitor -.监控.-> App1
    Monitor -.监控.-> App2
    Monitor -.监控.-> MySQLMaster
    Monitor -.监控.-> RedisMaster
    
    Log -.收集.-> App1
    Log -.收集.-> App2
    
    Backup -.备份.-> MySQLMaster
    Backup -.备份.-> RedisMaster
    
    style User1 fill:#87CEEB
    style User2 fill:#87CEEB
    style NginxLB fill:#FFD700
    style App1 fill:#FFA07A
    style App2 fill:#FFA07A
    style MySQLMaster fill:#DDA0DD
    style RedisMaster fill:#DC382D
    style Monitor fill:#90EE90
```

### 3.2 开发环境部署架构

```mermaid
graph TB
    subgraph 开发机
        DevPC[开发者电脑<br/>Windows/Mac/Linux]
        
        subgraph 前端开发
            NodeJS[Node.js 14+]
            VueCLI[Vue CLI]
            DevServer[开发服务器<br/>http://localhost:9194<br/>热重载]
        end
        
        subgraph 后端开发
            JDK[JDK 1.8]
            Maven[Maven 3.6+]
            IDE[IDEA/Eclipse]
            SpringDev[Spring Boot Dev<br/>http://localhost:9191<br/>自动重启]
        end
        
        subgraph 数据库服务
            MySQLDev[(MySQL 8.0<br/>localhost:3309<br/>数据库: s003)]
            RedisDev[(Redis 7.x<br/>localhost:6379)]
        end
        
        subgraph 工具
            Git[Git版本控制]
            Postman[Postman测试]
            BrowserDev[Chrome DevTools]
        end
    end
    
    DevPC --> NodeJS
    DevPC --> JDK
    DevPC --> MySQLDev
    DevPC --> RedisDev
    
    NodeJS --> VueCLI
    VueCLI --> DevServer
    
    JDK --> Maven
    Maven --> IDE
    IDE --> SpringDev
    
    DevServer --> SpringDev
    SpringDev --> MySQLDev
    SpringDev --> RedisDev
    
    Git -.代码管理.-> IDE
    Git -.代码管理.-> VueCLI
    
    Postman -.API测试.-> SpringDev
    BrowserDev -.前端调试.-> DevServer
    
    style DevPC fill:#87CEEB
    style DevServer fill:#90EE90
    style SpringDev fill:#FFA07A
    style MySQLDev fill:#DDA0DD
    style RedisDev fill:#DC382D
```

### 3.3 Docker容器化部署（推荐）

```mermaid
graph TB
    subgraph Docker Host
        subgraph 前端容器
            NginxDocker[Nginx容器<br/>nginx:alpine<br/>端口: 80]
            VueFiles[Vue dist文件<br/>挂载卷]
        end
        
        subgraph 后端容器
            SpringDocker[Spring Boot容器<br/>openjdk:8-jre<br/>端口: 9191]
            AppConfig[application.yml<br/>环境变量]
        end
        
        subgraph 数据库容器
            MySQLDocker[MySQL容器<br/>mysql:8.0<br/>端口: 3306]
            MySQLData[数据持久化<br/>挂载卷: /var/lib/mysql]
        end
        
        subgraph 缓存容器
            RedisDocker[Redis容器<br/>redis:7-alpine<br/>端口: 6379]
            RedisData[数据持久化<br/>挂载卷: /data]
        end
        
        DockerNetwork[Docker网络<br/>bridge模式]
    end
    
    NginxDocker --> VueFiles
    NginxDocker --> DockerNetwork
    SpringDocker --> DockerNetwork
    MySQLDocker --> DockerNetwork
    RedisDocker --> DockerNetwork
    
    SpringDocker --> MySQLDocker
    SpringDocker --> RedisDocker
    SpringDocker --> AppConfig
    MySQLDocker --> MySQLData
    RedisDocker --> RedisData
    
    style NginxDocker fill:#FFD700
    style SpringDocker fill:#FFA07A
    style MySQLDocker fill:#DDA0DD
    style RedisDocker fill:#DC382D
    style DockerNetwork fill:#90EE90
```

### 3.4 Nginx配置示例

```nginx
# nginx.conf
server {
    listen 80;
    server_name yourdomain.com;
    
    # HTTPS重定向
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    # SSL证书
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        
        # 静态资源缓存
        location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 后端API代理
    location /api {
        proxy_pass http://localhost:9191;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_read_timeout 120s;
        proxy_send_timeout 60s;
    }
    
    # 文件上传大小限制
    client_max_body_size 30M;
    
    # Gzip压缩
    gzip on;
    gzip_types text/plain application/json application/javascript text/css;
    gzip_min_length 1024;
}
```

### 3.5 Docker Compose配置

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: cosplay-mall-mysql
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: s003
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql/s003.sql:/docker-entrypoint-initdb.d/init.sql
    command:
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-time-zone=+08:00
    networks:
      - mall-network
    restart: always

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: cosplay-mall-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - mall-network
    restart: always

  # 后端服务
  backend:
    build: ./mall-server
    container_name: cosplay-mall-backend
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: s003
      DB_USER: root
      DB_PASS: 123456
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: your-secret-key-here
    ports:
      - "9191:9191"
    depends_on:
      - mysql
      - redis
    networks:
      - mall-network
    restart: always

  # 前端服务
  frontend:
    build: ./mall-web
    container_name: cosplay-mall-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - mall-network
    restart: always

volumes:
  mysql-data:
  redis-data:

networks:
  mall-network:
    driver: bridge
```

---

## 四、功能模块图

### 4.1 系统功能模块总览

```mermaid
graph TB
    subgraph Cosplay商城系统
        subgraph 用户端模块
            U1[首页模块]
            U2[商品模块]
            U3[购物车模块]
            U4[订单模块]
            U5[用户中心]
            U6[互动模块]
        end
        
        subgraph 管理端模块
            M1[数据统计]
            M2[商品管理]
            M3[订单管理]
            M4[用户管理]
            M5[运营管理]
            M6[系统管理]
        end
    end
    
    U1 --> U1_1[轮播图展示]
    U1 --> U1_2[推荐商品]
    U1 --> U1_3[热门分类]
    U1 --> U1_4[公告展示]
    
    U2 --> U2_1[商品列表]
    U2 --> U2_2[商品搜索]
    U2 --> U2_3[商品详情]
    U2 --> U2_4[商品评论]
    U2 --> U2_5[个性化推荐]
    
    U3 --> U3_1[添加商品]
    U3 --> U3_2[修改数量]
    U3 --> U3_3[删除商品]
    U3 --> U3_4[清空购物车]
    U3 --> U3_5[选择结算]
    
    U4 --> U4_1[创建订单]
    U4 --> U4_2[选择地址]
    U4 --> U4_3[支付订单]
    U4 --> U4_4[订单列表]
    U4 --> U4_5[订单详情]
    U4 --> U4_6[确认收货]
    U4 --> U4_7[售后申请]
    
    U5 --> U5_1[个人信息]
    U5 --> U5_2[地址管理]
    U5 --> U5_3[我的订单]
    U5 --> U5_4[我的收藏]
    U5 --> U5_5[我的留言]
    U5 --> U5_6[售后记录]
    
    U6 --> U6_1[商品评论]
    U6 --> U6_2[在线留言]
    U6 --> U6_3[定制咨询]
    
    M1 --> M1_1[收入图表]
    M1 --> M1_2[周收入统计]
    M1 --> M1_3[月收入统计]
    M1 --> M1_4[商品排行]
    M1 --> M1_5[用户统计]
    
    M2 --> M2_1[商品列表]
    M2 --> M2_2[新增商品]
    M2 --> M2_3[编辑商品]
    M2 --> M2_4[删除商品]
    M2 --> M2_5[分类管理]
    M2 --> M2_6[规格管理]
    M2 --> M2_7[推荐设置]
    M2 --> M2_8[上下架管理]
    
    M3 --> M3_1[订单列表]
    M3 --> M3_2[订单详情]
    M3 --> M3_3[订单发货]
    M3 --> M3_4[订单导出]
    M3 --> M3_5[售后处理]
    
    M4 --> M4_1[用户列表]
    M4 --> M4_2[用户搜索]
    M4 --> M4_3[编辑用户]
    M4 --> M4_4[删除用户]
    M4 --> M4_5[重置密码]
    
    M5 --> M5_1[轮播图管理]
    M5 --> M5_2[公告管理]
    M5 --> M5_3[评论管理]
    M5 --> M5_4[留言回复]
    
    M6 --> M6_1[文件管理]
    M6 --> M6_2[头像管理]
    M6 --> M6_3[管理员信息]
    M6 --> M6_4[权限管理]
    
    style U1 fill:#87CEEB
    style U2 fill:#87CEEB
    style U3 fill:#87CEEB
    style U4 fill:#87CEEB
    style U5 fill:#87CEEB
    style U6 fill:#87CEEB
    style M1 fill:#FFD700
    style M2 fill:#FFD700
    style M3 fill:#FFD700
    style M4 fill:#FFD700
    style M5 fill:#FFD700
    style M6 fill:#FFD700
```

### 4.2 用户端功能架构图

```mermaid
graph LR
    subgraph 用户端Front
        A[登录注册]
        B[商品浏览]
        C[购物车]
        D[订单流程]
        E[个人中心]
        F[互动功能]
    end
    
    A --> A1[用户登录]
    A --> A2[用户注册]
    A --> A3[密码找回]
    
    B --> B1[首页浏览]
    B --> B2[商品列表]
    B --> B3[商品搜索]
    B --> B4[商品详情]
    B --> B5[分类筛选]
    B --> B6[个性化推荐]
    
    C --> C1[添加商品]
    C --> C2[修改数量]
    C --> C3[删除商品]
    C --> C4[结算选择]
    
    D --> D1[确认订单]
    D --> D2[选择地址]
    D --> D3[提交订单]
    D --> D4[模拟支付]
    D --> D5[订单查询]
    D --> D6[确认收货]
    D --> D7[售后申请]
    
    E --> E1[个人信息]
    E --> E2[修改密码]
    E --> E3[地址管理]
    E --> E4[订单历史]
    E --> E5[留言记录]
    
    F --> F1[商品评论]
    F --> F2[在线留言]
    F --> F3[查看回复]
    
    style A fill:#FFB6C1
    style B fill:#90EE90
    style C fill:#87CEEB
    style D fill:#FFD700
    style E fill:#DDA0DD
    style F fill:#FFA07A
```

### 4.3 管理端功能架构图

```mermaid
graph TB
    subgraph 管理端Manage
        subgraph 首页仪表盘
            H1[收入统计]
            H2[订单统计]
            H3[用户统计]
            H4[商品统计]
        end
        
        subgraph 商品管理
            G1[商品列表]
            G2[新增商品]
            G3[编辑商品]
            G4[分类管理]
            G5[规格管理]
            G6[轮播图管理]
        end
        
        subgraph 订单管理
            O1[订单列表]
            O2[订单详情]
            O3[订单发货]
            O4[售后处理]
            O5[订单导出]
        end
        
        subgraph 用户管理
            U1[用户列表]
            U2[用户搜索]
            U3[编辑用户]
            U4[删除用户]
            U5[重置密码]
        end
        
        subgraph 互动管理
            I1[评论管理]
            I2[留言管理]
            I3[留言回复]
        end
        
        subgraph 数据统计
            S1[收入图表]
            S2[周收入]
            S3[月收入]
            S4[商品排行]
        end
        
        subgraph 系统管理
            SY1[文件管理]
            SY2[头像管理]
            SY3[公告管理]
            SY4[个人信息]
        end
    end
    
    style H1 fill:#FFD700
    style G1 fill:#90EE90
    style O1 fill:#87CEEB
    style U1 fill:#DDA0DD
    style I1 fill:#FFA07A
    style S1 fill:#FFB6C1
    style SY1 fill:#E6E6FA
```

### 4.4 功能模块统计

| 模块类别 | 子模块数 | 功能点数 | 前端页面数 | 后端接口数 |
|---------|---------|---------|-----------|-----------|
| 用户端-首页 | 4 | 8 | 2 | 6 |
| 用户端-商品 | 6 | 15 | 3 | 12 |
| 用户端-购物车 | 5 | 10 | 1 | 6 |
| 用户端-订单 | 7 | 18 | 3 | 10 |
| 用户端-个人中心 | 6 | 12 | 2 | 8 |
| 用户端-互动 | 3 | 6 | 2 | 5 |
| 管理端-数据统计 | 5 | 10 | 2 | 4 |
| 管理端-商品管理 | 8 | 20 | 3 | 15 |
| 管理端-订单管理 | 5 | 12 | 1 | 8 |
| 管理端-用户管理 | 5 | 10 | 1 | 6 |
| 管理端-互动管理 | 3 | 8 | 1 | 5 |
| 管理端-系统管理 | 4 | 8 | 2 | 6 |
| **总计** | **61** | **137** | **23** | **91** |

### 4.5 模块依赖关系

```mermaid
graph TB
    subgraph 基础模块
        Auth[认证模块<br/>JWT登录]
        User[用户模块<br/>用户管理]
    end
    
    subgraph 核心业务模块
        Product[商品模块<br/>商品管理]
        Cart[购物车模块<br/>购物车管理]
        Order[订单模块<br/>订单管理]
    end
    
    subgraph 支撑模块
        Address[地址模块<br/>地址管理]
        Comment[评论模块<br/>评论管理]
        Message[留言模块<br/>留言管理]
        AfterSale[售后模块<br/>售后管理]
    end
    
    subgraph 运营模块
        Category[分类模块<br/>分类管理]
        Carousel[轮播图模块<br/>轮播管理]
        Notice[公告模块<br/>公告管理]
        Income[收入模块<br/>数据统计]
    end
    
    Auth --> User
    Auth --> Product
    Auth --> Cart
    Auth --> Order
    
    User --> Address
    User --> Order
    User --> Comment
    User --> Message
    User --> AfterSale
    
    Product --> Category
    Product --> Cart
    Product --> Order
    Product --> Comment
    Product --> Carousel
    
    Cart --> Order
    Order --> AfterSale
    Order --> Income
    
    style Auth fill:#FFD700
    style Product fill:#90EE90
    style Order fill:#87CEEB
    style Income fill:#FFB6C1
```

---

## 📊 架构特色总结

### 技术亮点

| 特性 | 实现方式 | 优势 |
|------|---------|------|
| 前后端分离 | Vue 3 + Spring Boot | 独立部署、易于维护 |
| 无状态认证 | JWT Token | 支持水平扩展 |
| 高性能缓存 | Redis缓存 | 响应速度快 |
| 密码安全 | BCrypt加密 | 防彩虹表攻击 |
| 组件化开发 | Vue Composition API | 代码复用性高 |
| 响应式设计 | Element Plus | 适配多端 |
| 个性化推荐 | 协同过滤算法 | 提升用户体验 |
| 自动化部署 | Docker Compose | 一键启动 |

### 架构优势

1. ✅ **高可扩展性** - 微服务友好架构，易于拆分
2. ✅ **高性能** - Redis缓存 + 数据库优化
3. ✅ **高安全性** - 多层安全防护机制
4. ✅ **易维护性** - 清晰的代码分层和文档
5. ✅ **易部署** - Docker容器化部署
6. ✅ **用户体验好** - 个性化推荐 + 响应式设计

---

## 📝 使用说明

### 图表查看

1. **Markdown编辑器** - 直接查看Mermaid图表
2. **在线渲染** - 使用 https://mermaid.live/ 渲染
3. **导出图片** - 右键保存为PNG/SVG
4. **插入文档** - 截图插入PPT/论文

### 文档用途

- 📖 **毕业设计** - 系统架构说明
- 🎓 **项目答辩** - 展示系统设计
- 🔧 **开发参考** - 新人快速了解
- 📊 **项目汇报** - 向导师/领导展示
- 📝 **技术文档** - 团队知识沉淀

---

**文档生成时间**：2026-04-15  
**系统版本**：v3.0（Vue 3 + Spring Boot）  
**维护团队**：开发团队
