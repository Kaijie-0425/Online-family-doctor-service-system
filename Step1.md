
# 🩺 家庭医生在线服务系统 - 技术变更与使用说明

> **文档版本**：V1.1
> **更新时间**：2026-03-22
> **项目作者**：雷凯杰

本文档记录了项目中已完成的所有基础设施与功能性改造、关键文件说明、运行与测试方法，以及若干注意事项与建议。本文档保存在项目根目录下，便于后续开发与团队参考。

- 补充了打包与直接运行的快速指引；微调若干说明以贴合当前代码结构。

## 一、 总体概况
本项目旨在优化医疗资源配置、赋能基层医疗服务，实现居民全生命周期的健康管理。
- **基础架构**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3 + MySQL 8.0 + Java 8。
- **基础包名**：`com.kaijie`
* **基础架构**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3 + MySQL 8.0 + Java 8。
* **基础包名**：`com.kaijie`
* **已完成进度**：Maven 依赖添加、MyBatis-Plus 代码生成器运行、Mapper XML 位置优化、Spring Security + JWT 无状态认证集成、居民电子健康档案（EHR）主表的查询与保存接口实现、文章管理模块（发布、列表、详情）、合同管理模块（申请、审核、列表）。

## 二、 关键依赖 (pom.xml)
在现有 `pom.xml` 中新增/调整了如下核心依赖：
- `spring-boot-starter-security`：Spring 安全框架。
* `spring-boot-starter-security`：Spring 安全框架。
* `io.jsonwebtoken:jjwt:0.9.1`：用于生成与解析 JWT。
* `io.swagger:swagger-annotations:1.6.8` 及 `io.swagger.core.v3:swagger-annotations:2.2.8`：消除代码生成器产出的实体类中 Swagger 注解缺失报错。
* **保留的核心依赖**：Spring Boot Web、MyBatis-Plus、MySQL Connector、Lombok、Freemarker（代码生成器引擎）。

> **注意**：依赖扫描器可能会提示若干第三方库的安全告警（信息性）。建议后续在升级 Spring Boot 时或逐条升级依赖以消除告警。

## 三、 配置文件 (application.yml)
`src/main/resources/application.yml` 已创建，包含数据库连接与 MyBatis-Plus 的核心配置：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/family_doctor_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root  # 请填写本地实际账号
    password: your_password  # 请填写本地实际密码

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl # 开启控制台 SQL 打印
```

## 四、 已新增/修改的重要源码文件

## 四、 已新增/修改的重要源码文件（概要）

### 1. 安全与认证模块 (`com.kaijie.security`)

## 1. 安全与认证模块 (`com.kaijie.security`)
## 2. 控制器模块 (`com.kaijie.controller`)
- `SecurityUtils.java`：静态工具方法（如 `getCurrentUsername()`）从 `SecurityContextHolder` 快速读取当前登录用户名。

### 2. 控制器模块 (`com.kaijie.controller`)

- `AuthController.java`
  - `POST /api/auth/login`：使用 BCrypt 校验密码并返回 JWT（返回字段示例：`{"token":"...","tokenType":"Bearer"}`）。
  - `POST /api/auth/register`：用户注册，密码使用 BCrypt 加密后保存。
- `EhrPatientProfileController.java`
  - `GET /api/ehr/profile/my`：返回当前登录用户的电子健康档案（无则返回 null）。
  - `POST /api/ehr/profile/my`：为当前用户创建或更新档案，后端会绑定当前用户的 userId，防止越权。
- `CmsArticleController.java`
  - `POST /api/article/publish`：发布文章（仅医生/管理员角色允许）。
  - `GET /api/article/list`：文章列表（支持按 category 过滤）。
- **`AuthController.java`**：
  - `POST /api/auth/login`：验证密码（BCrypt）并返回 JWT（token & tokenType）。
  - `POST /api/auth/register`：注册新用户，密码使用 BCrypt 加密保存。
- **`EhrPatientProfileController.java`**：

- `PatientProfileServiceImpl.java`：通过 username 查询 `sys_user` 获取 userId，并对 `ehr_patient_profile` 表进行绑定操作，`saveOrUpdateProfile` 强制校验 userId，防止越权写入。
  - `POST /api/ehr/profile/my`：接收 JSON，为当前用户创建或更新档案。
- **`CmsArticleController.java`**：
  - `POST /api/article/publish`：发布健康科普文章（医生/管理员角色）。
  - `GET /api/article/list`：获取文章列表，支持按类别过滤。
  - `GET /api/article/{id}`：获取文章详情，并增加浏览量。
- **`DocContractController.java`**：
  - `POST /api/contract/apply`：居民申请与医生签约。
  - `POST /api/contract/audit`：医生审核签约申请（同意/拒绝）。
  - `GET /api/contract/list`：获取当前用户的签约记录（居民查看自己的，医生查看患者的）。

## 3. EHR 业务逻辑层 (`com.kaijie.service.impl`)
- **`PatientProfileServiceImpl.java`**：通过 username 查询 `sys_user` 得到 userId；使用 userId 去操作 `ehr_patient_profile` 表。`saveOrUpdateProfile` 方法会强制校验并绑定当前用户的 userId，防止越权操作。
- **`ArticleServiceImpl.java`**：实现文章发布、列表查询、详情获取。发布时校验用户角色，详情时更新浏览量。
## 4. 数据持久层与生成器
- **`CodeGenerator.java`**：位于 `test` 或 `generator` 包下，使用 MyBatis-Plus FastAutoGenerator 自动生成 12 张表的底层代码。
- **Mapper XML 迁移**：原生成在 `java/com/kaijie/mapper/xml/` 的 `.xml` 文件已全部迁移至 `src/main/resources/mapper/`，确保打包时能正确加载 SQL 映射。

3. 命令行构建并运行（可运行 jar）
   - 打包：

```bash
## 5. DTO 层 (`com.kaijie.dto`)
```

- **`ArticlePublishDTO.java`**：文章发布请求体，包含标题、类别、封面图片、内容。
- **`ContractApplyDTO.java`**：签约申请请求体，包含医生ID、申请理由。
- **`ContractAuditDTO.java`**：签约审核请求体，包含合同ID、审核状态、拒绝理由。

```bash
java -jar target/Online_Family_Doctor_Service_System-1.0-SNAPSHOT.jar
```

1. **环境准备**： 确保本地 MySQL 已运行，数据库 `family_doctor_db` 包含 12 张核心表。确保 `application.yml` 中的账密正确。
```

返回示例：{"token": "eyJhbGciOiJIUzI1NiIs...", "tokenType": "Bearer"}

2. **构建与启动**： 在 IDE 中刷新 Maven 项目，然后直接运行 `Main.java`。或使用终端命令：

```bash
   Bash
```

- 发布文章（需医生/管理员角色）：

   - **发布文章**：
```bash
   ```
   mvn -DskipTests clean compile
   mvn -DskipTests spring-boot:run
   ```
```

     Bash
- 获取文章列表：

     ```
     curl -H "Authorization: Bearer <填入你的token>" -H "Content-Type: application/json" -d '{"title":"健康饮食指南","category":"饮食","coverImage":"http://example.com/cover.jpg","content":"文章内容..."}' http://localhost:8080/api/article/publish
     ```

   - **获取文章列表**：
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/article/list?category=饮食
3. **获取 Token (登录测试)**：
```

     Bash
- 审核签约：

     ```
     curl -H "Authorization: Bearer <填入你的token>" -H "Content-Type: application/json" -d '{"doctorId":2,"applyReason":"需要定期体检"}' http://localhost:8080/api/contract/apply
     ```
```bash
   Bash
```

   - **审核签约**：

     Bash
- 获取签约列表：

     ```
     curl -H "Authorization: Bearer <填入你的token>" -H "Content-Type: application/json" -d '{"contractId":1,"auditStatus":1}' http://localhost:8080/api/contract/audit
     ```
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/contract/list
```

## 六、 打包产物与调试提示

     Bash
   ```
   curl -H "Content-Type: application/json" -d '{"username":"kaijie","password":"123456"}' http://localhost:8080/api/auth/login
   ```

     ```
     curl -H "Authorization: Bearer <填入你的token>" http://localhost:8080/api/contract/list
     ```
   *返回示例：`{"token": "eyJhbGciOiJIUzI1NiIs...", "tokenType": "Bearer"}`*

   - **查询档案**：

## 六、 安全与可维护性建议（待办事项）
     Bash
     ```
     curl -H "Authorization: Bearer <填入你的token>" http://localhost:8080/api/ehr/profile/my
     ```
   - **更新档案**：
     Bash

     ```
   - **获取签约列表**：
5. **调用文章管理接口 (需携带 Token)**：
