# 📚 Library System — 图书管理系统

基于 **Spring Boot + MySQL + Redis** 的图书管理系统，支持管理员和读者双角色，具备图书管理、借阅追踪、逾期检测、公告发布等功能，采用 Docker 容器化部署。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.15-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-1.8-orange)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ 功能特性

### 🔐 用户认证
- 管理员 / 读者 双角色登录
- Session 会话管理 + 自定义拦截器鉴权
- 密码 MD5 加密存储
- 个人资料修改与头像上传

### 📖 图书管理
- 图书 CRUD（增删改查）
- 按分类筛选 + 关键词搜索
- 图书库存与借出数量追踪
- 批量删除操作

### 📋 借阅管理
- 图书借阅 / 归还流程
- 借阅状态追踪（未还 / 已还 / 逾期 / 损坏）
- 读者罚款机制
- 逾期自动检测（定时任务）

### 📊 管理仪表盘
- 图书总数、读者总数统计
- 借阅统计（按状态、性别、院系维度）
- 分类统计
- 最近借阅记录

### 📢 其他
- 系统公告发布与管理
- 读者消息通知
- 前端 SPA 架构（Thymeleaf 片段动态加载）

---

## 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 2.7.15 |
| **编程语言** | Java | 1.8 |
| **数据库** | MySQL | 8.0 |
| **缓存** | Redis | 7.0 (Alpine) |
| **ORM** | Spring Data JPA (Hibernate) | — |
| **连接池** | Druid | 1.2.16 |
| **模板引擎** | Thymeleaf | — |
| **前端框架** | Bootstrap + jQuery | 4.6.0 / 3.6.0 |
| **构建工具** | Maven | — |
| **容器化** | Docker + Docker Compose | — |
| **工具库** | Lombok | — |

---

## 📁 项目结构

```
library-system/
├── src/main/java/com/yx/
│   ├── cache/
│   │   └── CacheManager.java              # Redis 缓存管理器
│   ├── config/
│   │   ├── DataConsistencyRunner.java      # 启动时数据一致性检查
│   │   ├── RedisConfig.java               # Redis 序列化配置
│   │   └── WebMvcConfig.java              # MVC 拦截器 & 静态资源映射
│   ├── controller/
│   │   ├── AdminAnnouncementController.java # 公告管理 API
│   │   ├── AdminController.java           # 管理员登录 & 统计 API
│   │   ├── BookInfoController.java        # 图书 CRUD API
│   │   ├── LendListController.java        # 借阅管理 API
│   │   ├── MessageController.java         # 消息 API
│   │   ├── NoticeController.java          # 公告查询 API
│   │   ├── PageController.java            # 页面路由 (Thymeleaf)
│   │   ├── ReaderInfoController.java      # 读者管理 API
│   │   └── TypeInfoController.java        # 分类管理 API
│   ├── dto/
│   │   ├── ApiResponse.java               # 统一响应体
│   │   └── PageRequest.java               # 分页请求体
│   ├── entity/
│   │   ├── Admin.java                     # 管理员实体
│   │   ├── BookInfo.java                  # 图书实体
│   │   ├── LendList.java                  # 借阅记录实体
│   │   ├── Message.java                   # 消息实体
│   │   ├── Notice.java                    # 公告实体
│   │   ├── ReaderInfo.java                # 读者实体
│   │   └── TypeInfo.java                  # 分类实体
│   ├── exception/
│   │   ├── BusinessException.java         # 业务异常
│   │   └── GlobalExceptionHandler.java    # 全局异常处理
│   ├── interceptor/
│   │   └── AuthenticationInterceptor.java # 登录认证拦截器
│   ├── repository/                        # Spring Data JPA 仓库 (7个)
│   ├── service/                           # 业务逻辑层 (7个)
│   ├── task/
│   │   └── OverdueCheckTask.java          # 逾期检测定时任务
│   ├── utils/
│   │   └── CommonUtils.java               # 通用工具类
│   └── LibrarySystemApplication.java      # Spring Boot 启动类
├── src/main/resources/
│   ├── static/
│   │   ├── css/                           # 样式文件
│   │   └── js/                            # JavaScript 文件
│   ├── templates/
│   │   ├── admin/                         # 管理端模板 (6个)
│   │   │   ├── dashboard.html
│   │   │   ├── fragment_announcements.html
│   │   │   ├── fragment_books.html
│   │   │   ├── fragment_dashboard.html
│   │   │   ├── fragment_lends.html
│   │   │   └── fragment_readers.html
│   │   ├── reader/                        # 读者端模板 (5个)
│   │   │   ├── dashboard.html
│   │   │   ├── fragment_books.html
│   │   │   ├── fragment_lends.html
│   │   │   ├── fragment_messages.html
│   │   │   └── fragment_profile.html
│   │   └── login.html                     # 登录页
│   ├── application.yml                    # 应用配置
│   └── library_db.sql                     # 数据库初始化脚本
├── Dockerfile                             # Docker 镜像构建文件
├── docker-compose.yml                     # 多容器编排
├── pom.xml                                # Maven 依赖管理
└── .gitignore
```

---

## 🚀 快速开始

### 环境要求

| 依赖 | 版本要求 |
|------|---------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |

### 1. 克隆项目

```bash
git clone https://github.com/Clam0428/library-system.git
cd library-system
```

### 2. 初始化数据库

在 MySQL 中执行初始化脚本：

```bash
mysql -u root -p < src/main/resources/library_db.sql
```

或者通过 MySQL 客户端导入 `src/main/resources/library_db.sql`。

### 3. 修改配置

编辑 `src/main/resources/application.yml`，根据实际情况配置数据库和 Redis 连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:library_db}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
```

支持通过环境变量覆盖：
- `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
- `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`

### 4. 启动项目

```bash
mvn clean package -DskipTests
java -jar target/library-system-2.0.0.jar
```

启动成功后访问：**http://localhost:8888**

---

## 🐳 Docker 部署

使用 Docker Compose 一键启动所有服务（MySQL + Redis + App）：

```bash
docker-compose up -d
```

服务说明：

| 服务 | 容器名 | 端口 |
|------|--------|------|
| MySQL 8.0 | `library-mysql` | `3306` |
| Redis 7 | `library-redis` | `6379` |
| 图书管理系统 | `library-app` | `8888` |

查看日志：

```bash
docker-compose logs -f library-app
```

停止服务：

```bash
docker-compose down
```

---

## 📡 API 概览

### 公共接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/` | 重定向到登录页 |
| `GET` | `/login` | 登录页 |
| `POST` | `/api/admin/login` | 管理员登录 |
| `POST` | `/api/reader/login` | 读者登录 |
| `POST` | `/api/reader/register` | 读者注册 |
| `GET` | `/api/notice/published` | 已发布公告列表 |

### 管理员接口（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/admin/currentUser` | 当前管理员信息 |
| `POST` | `/api/admin/logout` | 登出 |
| `GET` | `/api/admin/stats` | 仪表盘统计数据 |
| `GET` | `/api/admin/recentLends` | 最近借阅记录 |
| `GET` | `/api/admin/list` | 管理员列表（分页） |
| `POST` | `/api/admin/add` | 添加管理员 |
| `POST` | `/api/admin/updatePassword` | 修改密码 |
| `POST` | `/api/admin/updateProfile` | 更新信息 |
| `GET/POST/DELETE` | `/api/book/*` | 图书 CRUD |
| `GET/POST` | `/api/lend/*` | 借阅管理 |
| `GET/POST/DELETE` | `/api/reader/*` | 读者管理 |
| `POST` | `/api/announcement/*` | 公告管理 |

> 所有 API 响应统一封装为 `ApiResponse<T>` 格式：`{ "code": 0, "msg": "success", "count": null, "data": {...} }`

---

## 🗄️ 数据库设计

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `admin` | 管理员 | id, username, password, email, phone, job_number |
| `reader_info` | 读者信息 | id, username, password, reader_no, phone, email, fine(罚款) |
| `book_info` | 图书信息 | id, name, author, publish, isbn, price, type_id, status, stock |
| `type_info` | 图书分类 | id, name |
| `lend_list` | 借阅记录 | id, book_id, reader_id, lend_date, back_date, status |
| `notice` | 公告 | id, title, content, admin_id, status(草稿/发布) |
| `message` | 消息 | id, reader_id, title, content, is_read |

---

## 👤 默认账户

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `admin` | 超级管理员 |
| 读者 | `reader001` | `123` | 测试读者 |
| 读者 | `reader002` | `123` | 测试读者 |

> ⚠️ **安全提醒**：首次部署后请立即修改默认密码！

---

## 📸 界面预览

系统采用 SPA 单页应用架构，分为两大端：

- **管理端** (`/admin/dashboard`) — 仪表盘 · 图书管理 · 读者管理 · 借阅记录 · 发布公告
- **读者端** (`/reader/dashboard`) — 概览 · 图书查询 · 借阅历史 · 个人信息 · 消息中心

---

## 🏗️ 架构亮点

- **SPA 前端架构**：使用 Thymeleaf 片段 + jQuery 动态加载，避免整页刷新
- **Redis 缓存**：自定义 `CacheManager` 实现数据缓存加速
- **定时任务**：`@Scheduled` 自动检测逾期借阅，更新逾期状态
- **数据一致性**：`DataConsistencyRunner` 在应用启动时校验数据完整性
- **统一异常处理**：`GlobalExceptionHandler` 全局捕获并返回规范化错误信息
- **容器化部署**：Docker 多阶段构建 + Compose 一键编排

---

## 📄 License

MIT License © 2025 [Clam0428](https://github.com/Clam0428)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request
