package com.kaijie.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 居民每日体征数据记录表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("health_daily_record")
@ApiModel(value = "HealthDailyRecord对象", description = "居民每日体征数据记录表")
public class HealthDailyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("居民ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("打卡日期")
    @TableField("record_date")
    private LocalDate recordDate;

    @ApiModelProperty("收缩压/高压 (mmHg)")
    @TableField("systolic_pressure")
    private Integer systolicPressure;

    @ApiModelProperty("舒张压/低压 (mmHg)")
    @TableField("diastolic_pressure")
    private Integer diastolicPressure;

    @ApiModelProperty("空腹血糖 (mmol/L)")
    @TableField("blood_sugar")
    private BigDecimal bloodSugar;

    @ApiModelProperty("心率 (次/分)")
    @TableField("heart_rate")
    private Integer heartRate;

    @ApiModelProperty("是否异常报警: 0-正常, 1-异常")
    @TableField("is_abnormal")
    private Byte isAbnormal;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
