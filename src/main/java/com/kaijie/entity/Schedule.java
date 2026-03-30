package com.kaijie.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 医生排班表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@Getter
@Setter
@TableName("doc_schedule")
@ApiModel(value = "Schedule对象", description = "医生排班表")
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID (雪花算法)")
    @TableId("id")
    private Long id;

    @ApiModelProperty("医生ID (关联 sys_user 表)")
    @TableField("doctor_id")
    private Long doctorId;

    @ApiModelProperty("排班日期 (如 2026-03-25)")
    @TableField("schedule_date")
    private LocalDate scheduleDate;

    @ApiModelProperty("班次：1-上午，2-下午")
    @TableField("shift_type")
    private Byte shiftType;

    @ApiModelProperty("最大放号量")
    @TableField("max_capacity")
    private Integer maxCapacity;

    @ApiModelProperty("剩余号源 (防超卖核心字段)")
    @TableField("available_capacity")
    private Integer availableCapacity;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除：0-未删除，1-已删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
