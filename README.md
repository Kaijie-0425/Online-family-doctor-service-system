# Online-family-doctor-service-system

版本：V1.2
更新时间：2026-03-30

本仓库为「家庭医生在线服务系统」后端（Spring Boot 2.7 + MyBatis-Plus + JWT）。

本文档为本次迭代的技术补充包摘要，聚焦新增/强化的三大业务模块：家庭医生签约、健康资讯（CMS）、医生排班与预约挂号。文档以模块化结构呈现当前实现状态、核心数据模型、关键设计与答辩亮点（架构亮点），并列出核心 REST API 字典。

---

## 第一部分：新增核心数据模型

下面按表格列出新增/重点表的核心字段与业务作用，便于快速理解数据模型。

### doc_contract（家庭医生签约表）
| 字段 (SQL) | Java 字段 | 说明 |
|---|---:|---|
| id (BIGINT) | id | 签约记录主键（雪花等） |
| patient_id (BIGINT) | patientId | 居民（申请人）ID，关联 `sys_user` |
| doctor_id (BIGINT) | doctorId | 医生（被申请方）ID，关联 `sys_user` |
| contract_status (TINYINT) | contractStatus | 签约状态：0-待审核，1-已签约，2-已拒绝，3-已解约，4-已过期 |
| apply_reason (VARCHAR) | applyReason | 居民申请留言 |
| reject_reason (VARCHAR) | rejectReason | 医生拒绝原因 |
| start_date / end_date (DATE) | startDate / endDate | 服务生效 / 到期日期 |
| contract_file_url (VARCHAR) | contractFileUrl | 电子协议书存储路径（MinIO） |
| create_time / update_time | createTime / updateTime | 时间戳 |
| is_deleted (TINYINT) | isDeleted | 逻辑删除标识 |

业务作用：记录居民与医生之间的签约申请与审批流程、状态流转与合同生效期。

### cms_article（健康资讯文章表）
| 字段 (SQL) | Java 字段 | 说明 |
|---|---:|---|
| id (BIGINT) | id | 文章主键 |
| author_id (BIGINT) | authorId | 发布者 ID（医生/管理员） |
| title (VARCHAR) | title | 标题 |
| category (VARCHAR) | category | 分类 |
| cover_image (VARCHAR) | coverImage | 封面图 URL |
| content (TEXT) | content | 富文本内容（HTML） |
| view_count (INT) | viewCount | 浏览量 |
| like_count (INT) | likeCount | 点赞数 |
| publish_status (TINYINT) | publishStatus | 发布状态：0 草稿，1 已发布 |
| create_time / update_time | createTime / updateTime | 时间戳 |
| is_deleted (TINYINT) | isDeleted | 逻辑删除标识 |

业务作用：支持基于角色的文章发布、公开阅读与基本统计（浏览量/点赞）。

### doc_schedule（医生排班表）
| 字段 (SQL) | Java 字段 | 说明 |
|---|---:|---|
| id (BIGINT) | id | 排班主键 |
| doctor_id (BIGINT) | doctorId | 医生 ID |
| schedule_date (DATE) | scheduleDate | 排班日期 |
| shift_type (TINYINT) | shiftType | 班次：1-上午，2-下午 |
| max_capacity (INT) | maxCapacity | 最大放号量 |
| available_capacity (INT) | availableCapacity | 剩余号源（并发控制核心字段） |
| create_time / update_time | createTime/updateTime | 时间戳 |
| is_deleted (TINYINT) | isDeleted | 逻辑删除标识 |

UNIQUE KEY（排班去重）设计思路：
```
ALTER TABLE doc_schedule
ADD CONSTRAINT uq_schedule_doctor_date_shift UNIQUE (doctor_id, schedule_date, shift_type);
```
数据库层的联合唯一索引保证同一医生同日同班次仅能有一条排班记录；Service 层做预校验以便返回友好错误。

### doc_appointment（居民预约挂号表）
| 字段 (SQL) | Java 字段 | 说明 |
|---|---:|---|
| id (BIGINT) | id | 预约主键 |
| patient_id (BIGINT) | patientId | 居民（患者）ID |
| schedule_id (BIGINT) | scheduleId | 关联的排班 ID |
| status (TINYINT) | status | 预约状态：0-已预约，1-已完成，2-已取消 |
| create_time / update_time | createTime/updateTime | 时间戳 |
| is_deleted (TINYINT) | isDeleted | 逻辑删除标识 |

UNIQUE KEY（防重复挂号）设计思路：
```
ALTER TABLE doc_appointment
ADD CONSTRAINT uq_appointment_patient_schedule UNIQUE (patient_id, schedule_id);
```
该约束作为数据库层的最终防线，防止同一患者对同一排班重复挂号；业务层也做存在性判断以便返回友好信息。

---

## 第二部分：核心业务链路解析（答辩亮点）

以下分三小节说明我们在实现中遇到的三项关键难题及解决办法，突出设计决策与架构亮点。

### 1) 状态机流转与双端校验（签约模块）

- 问题：签约为双向流程（居民申请 -> 医生审核），需要防止重复申请、越权审批及非法状态流转，同时保留可审计信息。
- 实现要点：
  - 明确定义 `contract_status` 的状态域（0/1/2/3/4），服务层统一使用该枚举语义。
  - 申请流程：`applyContract(username, dto)` 会使用 `username` 查 `sys_user` 得到 `patientId`，先查询是否存在未完成/重复申请；若无则插入 `contract_status=0` 的记录并保存 `apply_reason`。
  - 审核流程：`auditContract(username, dto)` 在 Service 层根据 `username` 得到操作者 `doctorId`，**校验操作者为目标记录的 doctor_id**（防止越权审批），并校验记录处于可审核状态（通常从 0 -> 1 或 0 -> 2）。
  - 审核成功时写入 `start_date`/`end_date`/`contract_file_url`，拒绝时写入 `reject_reason`。全部在事务内完成以保证原子性。
  - 审计：`create_time` 与 `update_time` 保留时间线，业务异常返回明确消息以便前端提示。

答辩亮点：双端（申请端/审核端）在 Service 层做冗余校验，结合 DB 字段记录形成可追溯、可验证的状态机实现。

### 2) RBAC 接口越权防护（资讯发布模块）

- 问题：仅允许医生/管理员发布文章，普通居民只能阅读；需要防止接口被越权调用或内部调用绕过权限。
- 实现要点：
  - 使用 JWT 无状态认证，把 `username` 作为 principal 存入 `SecurityContext`（`JwtAuthenticationFilter`）。
  - Controller 层检查登录并将 `username` 传入 Service；Service 层再根据 `username` 查询 `sys_user` 并校验 `roleType`（0 管理员、1 医生为允许发布者）。
  - RBAC 校验放在 Service 层（不仅仅依赖 Controller 注解），保证所有调用路径都受相同权限约束。
  - 阅读与统计（view_count）在 Service 或 DAO 层做 DB 更新；当前使用直接 DB 更新浏览数（后续可扩展为 Redis 防刷）。

答辩亮点：将权限判定下沉到 Service 层，确保发布权限在任何调用路径下都被强制执行，提升可追溯性与测试性。

### 3) 高并发下的库存扣减与防超卖机制（挂号模块）

- 问题：预约为高并发场景，需避免超卖并保证不重复挂号。
- 实现要点（核心）：
  - 数据层：`doc_schedule.available_capacity` 作为库存；`doc_appointment` 上的唯一索引 `(patient_id, schedule_id)` 作为最终重复插入约束。
  - 原子扣减策略：在 Service 层使用单条条件 UPDATE（MyBatis-Plus 的 UpdateWrapper）：
	```sql
	UPDATE doc_schedule
	SET available_capacity = available_capacity - 1
	WHERE id = :scheduleId
	  AND available_capacity > 0;
	```
	如果受影响行数为 0 则表示库存已耗尽，返回 `该排班号源已满`。
  - 该单条 UPDATE 在 InnoDB 下对被修改行加行级锁，从而在 DB 层完成“检查+扣减”的原子性，避免读-改-写竞态。
  - 插入预约：在扣减库存成功后插入 `doc_appointment`；若插入因唯一索引冲突失败（极端并发），上层会捕获异常并执行补偿（恢复库存或回滚事务）。

答辩亮点：把并发控制下沉到数据库层并与唯一索引协同，使用最小代价（单条 UPDATE）实现高并发下的正确性与较高吞吐。

---

## 第三部分：新增 API 接口字典（核心接口一览）

说明：接口返回为原生对象/字符串；业务异常由 Service 抛出 RuntimeException，Controller 捕获并以 HTTP 400 返回异常消息文本；未鉴权返回 401。

### 签约模块（`/api/contract`）
| 路径 | 方法 | 参数 / Body | 角色 | 功能 |
|---|---:|---|---|---|
| /api/contract/apply | POST | Body: `ContractApplyDTO` | 居民（需登录） | 居民发起签约申请（contract_status=0） |
| /api/contract/audit | POST | Body: `ContractAuditDTO` | 医生（需登录） | 医生同意/拒绝申请，更新 `contract_status` |
| /api/contract/list | GET | - | 登录用户 | 列出当前用户相关签约记录 |

### 资讯模块（`/api/article`）
| 路径 | 方法 | 参数 / Body | 角色 | 功能 |
|---|---:|---|---|---|
| /api/article/publish | POST | Body: `ArticlePublishDTO` | 医生/管理员（需登录） | 发布/保存文章（Service 校验 roleType） |
| /api/article/list | GET | Param: category (可选) | 公开 | 列表查询 |
| /api/article/{id} | GET | Path: id | 公开 | 文章详情（同时更新 view_count） |

### 挂号/排班模块（`/api/clinic`）
| 路径 | 方法 | 参数 / Body | 角色 | 功能 |
|---|---:|---|---|---|
| /api/clinic/schedule | POST | Body: `ScheduleCreateDTO` | 医生（需登录） | 医生发布排班（同一医生同日同班次不可重复） |
| /api/clinic/schedule/list | GET | Params: doctorId (必), startDate, endDate | 公开或需鉴权（当前配置需鉴权） | 查询医生排班列表 |
| /api/clinic/appointment | POST | Body: `AppointmentCreateDTO` | 居民（需登录） | 居民预约：原子扣减库存并插入预约记录 |

---

## 架构亮点（摘要）

- 数据层与应用层双重保障：对重复/越权类问题同时使用 Service 校验与数据库联合唯一索引，既保证友好提示又保最终一致性。
- 并发保护下沉到 DB：库存扣减使用条件 UPDATE（`available_capacity > 0`）的单语句实现“检查+扣减”的原子性，借助 InnoDB 行级锁保障正确性与并发吞吐。
- RBAC 在 Service 层生效：角色判定放在 Service 层可保证所有调用路径（包括调度/内部调用）都受保护。
- 可观测性：新增全局请求日志过滤器与鉴权入口点，使未鉴权/鉴权失败在日志与 HTTP 响应中可见，便于联调与线上排查。

---

## 第四部分：智能健康评估与自查（新增）

本次迭代新增“智能健康评估与自查”模块，包含两大能力：
- 基于大模型的症状初步评估（AI 医生建议）
- 居民体征数据录入、预警与趋势查询

实现要点
- AI 调用：后端通过 `RestTemplate` 调用外部模型服务（示例采用 硅基流动 平台 API: `https://api.siliconflow.cn/v1/chat/completions`），请求/响应按 OpenAI-like chat completion 格式交互，最终从返回 JSON 中提取 `choices[0].message.content` 并原样返回给前端。
- 体征录入：居民角色（`roleType == 2`）可录入体征，系统按规则生成预警信息并入库（表：`health_vital_signs`）。
- 鉴权与权限：所有接口需带 JWT；服务层进一步校验 `sys_user.role_type` 防止越权。

接口字典（新增）
- POST `/api/health/assess`
  - 描述：基于用户输入症状，调用大模型返回初步健康评估建议（字符串）。
  - 请求 Body: `{ "symptoms": "..." }`（JSON）
  - 权限：需登录（任意角色）
  - 返回：HTTP 200 + 原始字符串（或在 AI 离线时返回固定提示："AI 医生暂时离线，请直接预约在线问诊。"）

- POST `/api/health/vitals`
  - 描述：居民录入体征数据并保存，若触发预警返回预警信息。
  - 请求 Body: `{ "systolicBp":150, "diastolicBp":95, "bloodSugar":7.5, "heartRate":102 }`
  - 权限：仅居民（roleType == 2）可调用
  - 返回：HTTP 200 + 字符串，示例："体征指标正常" 或 "预警：血压偏高; 心率异常"

- GET `/api/health/vitals/trend?days=7`
  - 描述：获取当前登录居民最近 `days` 天的体征记录（按日期升序）
  - 权限：需登录（居民）
  - 返回：HTTP 200 + JSON 数组，元素为 `HealthVitalSigns` 实体字段

鉴权 / 授权错误行为
- 未登录或 Token 无效：后端会抛出 `UnauthenticatedException`，由全局异常处理器映射为 HTTP 401，响应体为纯文本错误消息，例如："未登录，请先登录" 或 "用户不存在或未登录"。
- 权限不足（例如 医生 调用体征录入接口）：后端会抛出 `PermissionDeniedException`，映射为 HTTP 403，响应体为纯文本消息，例如："只有居民用户可以录入体征"。
- 其他服务异常：HTTP 500，响应体为异常消息或通用提示（"服务器错误"）。

关于 API Key（重要）
- 为了调用外部大模型服务，后端代码中有一个 API Key 常量占位：
  - `HealthVitalSignsServiceImpl` 文件中的 `SILICONFLOW_API_KEY` 已设置为占位字符串 `REPLACE_WITH_YOUR_API_KEY`（请务必替换）。
- 安全建议：不要把真实 API Key 硬编码到源码。推荐做法：
  1. 把密钥放在 `application.yml` 中的自定义属性（并在部署时通过配置管理替换），或
  2. 使用环境变量（例如 `SILICONFLOW_API_KEY`），并在启动脚本/容器定义中注入。示例（macOS zsh）：

```bash
export SILICONFLOW_API_KEY="sk-xxxxxxxxxxxxxxxxxxxxxxxx"
mvn spring-boot:run
```

或者在生产容器里通过 Secrets/环境变量注入。

示例请求（curl）
- 症状评估：
```bash
curl -X POST http://localhost:8080/api/health/assess \
  -H "Authorization: Bearer <你的JWT>" \
  -H "Content-Type: application/json" \
  -d '{"symptoms":"我最近头晕、心悸"}'
```

- 体征录入（居民）：
```bash
curl -X POST http://localhost:8080/api/health/vitals \
  -H "Authorization: Bearer <你的JWT>" \
  -H "Content-Type: application/json" \
  -d '{"systolicBp":150,"diastolicBp":95,"bloodSugar":7.5,"heartRate":102}'
```

- 趋势查询：
```bash
curl -X GET 'http://localhost:8080/api/health/vitals/trend?days=7' \
  -H "Authorization: Bearer <你的JWT>"
```

返回示例与格式化建议
- AI 文本通常为多行说明，后端会原样返回该字符串。若需前端展示更友好，可选择在前端或后端对该字符串做格式化：
  - 极简版（单行）/ 多行（段落 + 列表）/ 结构化 JSON（便于渲染）三种形式均可；建议后端默认返回原始字符串，由前端按场景展示或调用后端提供的格式化工具。

日志与审计
- 建议在生产环境开启调用审计（例如把 AI 原始返回与请求日志写入审计表或日志系统），以便事后回溯和质量评估（注意脱敏）。

以上文档已合入本 README。如需我将该节单独生成为 `docs/health_assessment.md` 或追加到 `Step1.md`，我可以继续执行并提交修改。

如果你希望我把本节内容作为 `docs/architecture_addendum.md` 单独写入仓库或追加到 `Step1.md` 中，请告知。我可以立刻把此 README.md 内容同步到指定位置。欢迎指定后续操作。

---

# Online_Family_Doctor_Service_System

一个基于 Spring Boot 2.7 + MyBatis-Plus 的示例医疗问诊系统（基础包名：`com.kaijie`）。

本次仓库已实现/集成的关键点（摘要）

- Spring Boot 2.7
- MyBatis-Plus ORM
- Spring Security + JWT（已实现基础认证/鉴权）
- 基于 Java WebSocket (@ServerEndpoint) 的医患实时问诊聊天室（WebSocket 服务端位于 `com.kaijie.websocket`）

---

WebSocket 聊天室（实现说明）

1. 端点

- WebSocket 连接地址（模板）:
  `ws://<host>:<port>/api/ws/chat/{consultationId}/{token}`
  - `consultationId`：问诊房间 ID（对应数据库表 `im_consultation_record.id`）
  - `token`：JWT（后端会解析 token 得到 username / userId，用于鉴权与定位用户）

2. 重要行为与安全校验

- 在 `@OnOpen`（连接建立）阶段会：
  1. 解析 `token` 得到 `username`，再读取用户信息（User）以获取 `userId`。
  2. 立即查询问诊房间记录（调用 `IConsultationRecordService.getById(consultationId)`）。
  3. 核心越权校验：只有当 `userId` 等于该房间的 `patient_id` 或 `doctor_id` 时才允许进入；否则直接抛出异常并中断连接（按设计：非法连接应被立刻断开）。

- 在 `@OnMessage` 阶段：
  - 接收客户端发送的 JSON（形如 {"msgType":"text","content":"..."}），将其解析为 `ChatMessageDTO`，封装为 `ImChatMessage`（实体），并持久化到 `im_chat_message` 表。
  - 将消息推送到同房间的对端（如果其在线且 Session.open），否则仅持久化保存供离线处理。

- 日志策略（便于排查静默失败）：
  - 关键点（进入方法、收到消息、插入数据库前后、推送结果、异常）均使用 `System.out.println("...")` 打印明确日志，不吞掉异常。

3. 时间序列化

- 为了支持 Java 8 的 `LocalDateTime` 序列化，WebSocket 服务端的 `ObjectMapper` 已注册 `JavaTimeModule` 并禁用了 `WRITE_DATES_AS_TIMESTAMPS`，以便使用 ISO-8601 文本格式传输时间字段。
- 如未在 `pom.xml` 中声明，请确保添加依赖：

```xml
<dependency>
  <groupId>com.fasterxml.jackson.datatype</groupId>
  <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

4. 相关类/文件

- WebSocketServer: `com.kaijie.websocket.ChatWebSocketServer`
- WebSocket 工具注入: `com.kaijie.config.WebSocketBeanUtil`
- DTO: `com.kaijie.dto.ChatMessageDTO`（包含 `msgType` 和 `content`）
- 问诊记录实体: `com.kaijie.entity.ConsultationRecord`（对应表 `im_consultation_record`）
- 消息实体: `com.kaijie.entity.ChatMessage`（对应表 `im_chat_message`）

---

HTTP 接口（与聊天室相关）

- 启动问诊（示例）
  - POST `/api/consultation/start`，参数：`doctorId`（Long）
  - 业务逻辑（`ImConsultationRecordServiceImpl.startConsultation`）会：
    1. 打印进入日志（包含 username、目标医生 id）
    2. 校验请求用户角色（仅居民 roleType==2 可发起）
    3. 构造问诊记录并设置 `status=1`（进行中），调用 `mapper.insert(record)` 前后打印日志，并返回生成的 `id`

- 拉取历史消息
  - GET `/api/consultation/history`，参数：`consultationId`（Long）
  - `ImChatMessageServiceImpl.getHistoryMessages(consultationId)` 返回该房间按 `send_time` 升序的所有消息

---

常见问题与故障排查

1. MySQL 插入失败：

错误样例：
```
java.sql.SQLException: Field 'symptom_desc' doesn't have a default value
```
原因与处理：
- 这个错误通常由 MySQL 的严格模式（strict mode）导致：当表中某列定义为 NOT NULL 且没有默认值时，插入语句未为该列提供值就会报错。
- 解决方法：
  1. 在插入前为实体的 `symptomDesc` 字段设置值（推荐）；
  2. 或在数据库层为 `symptom_desc` 列设置默认值或允许 NULL；
  3. 或修改 MySQL 配置（不推荐）以关闭严格模式。

2. WebSocket 连接被立即断开
- 如果你在 `OnOpen` 抛出异常（例如越权校验失败），WebSocket 将被容器中断，客户端会看到连接失败或瞬断（1006/1011）。这属于设计行为以阻止非法访问。

3. Jackson 时间序列化问题
- 如果你发现时间以时间戳显示或解析失败，请确认项目已加入 `jackson-datatype-jsr310` 并且 `ObjectMapper` 注册了 `JavaTimeModule`。

---

本地编译与测试（快速指南）

1. 构建：

```bash
mvn clean package -DskipTests
```

2. 启动：

```bash
java -jar target/Online_Family_Doctor_Service_System-1.0-SNAPSHOT.jar
```

3. 测试 WebSocket（示例，使用 wscat）：

```bash
npm install -g wscat
wscat -c "ws://localhost:8080/api/ws/chat/1/你的_JWT_token"
# 连接后发送如下消息示例
{"msgType":"text","content":"你好，医生"}
```

日志检查点（在控制台）

- 连接建立：========== [WS] OnOpen 触发！房间号: x, Token: y ==========
- 用户加入：>>> 用户 <id> 成功加入房间！当前在线人数: N
- 收到消息：========== [WS] 收到新消息: {...} ==========
- 插入 DB 前：>>> 准备执行 MyBatis-Plus 插入操作...
- 插入成功：>>> 插入成功！生成的问诊房间 ID: <id>
- 推送信息：>>> 已将消息推送给用户 <id> / 用户 <id> 不在线或 session 已关闭

---

后续工作（建议）

- 若需要，我可以继续：
  1. 完整实现 `ChatMessageDTO`、`WebSocketConfig.java`（返回 `ServerEndpointExporter` 的 @Bean），
  2. 完整实现 `ImConsultationRecordServiceImpl.startConsultation` 与 `ImChatMessageServiceImpl.getHistoryMessages` 与 Controller（按你之前给出的详细日志要求）。

如需我把 README 里某段内容改成更详细的 API 文档或加入示例请求/响应，请告诉我你想补充的部分，我会继续更新。
