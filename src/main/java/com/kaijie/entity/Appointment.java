package com.kaijie.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 居民预约挂号表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@Getter
@Setter
@TableName("doc_appointment")
@ApiModel(value = "Appointment对象", description = "居民预约挂号表")
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID (雪花算法)")
    @TableId("id")
    private Long id;

    @ApiModelProperty("居民ID (关联 sys_user 表)")
    @TableField("patient_id")
    private Long patientId;

    @ApiModelProperty("排班ID (关联 doc_schedule 表)")
    @TableField("schedule_id")
    private Long scheduleId;

    @ApiModelProperty("状态：0-已预约，1-已完成，2-已取消")
    @TableField("status")
    private Byte status;

    @ApiModelProperty("预约创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
