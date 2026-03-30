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

如果你希望我把本节内容作为 `docs/architecture_addendum.md` 单独写入仓库或追加到 `Step1.md` 中，请告知。我可以立刻把此 README.md 内容同步到指定位置。欢迎指定后续操作。

