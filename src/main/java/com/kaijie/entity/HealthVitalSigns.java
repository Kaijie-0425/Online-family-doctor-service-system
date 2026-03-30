package com.kaijie.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 居民体征数据表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-30
 */
@Getter
@Setter
@TableName("health_vital_signs")
@ApiModel(value = "HealthVitalSigns对象", description = "居民体征数据表")
public class HealthVitalSigns implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("居民ID (关联 sys_user)")
    @TableField("patient_id")
    private Long patientId;

    @ApiModelProperty("记录日期")
    @TableField("record_date")
    private LocalDate recordDate;

    @ApiModelProperty("收缩压(高压 mmHg)")
    @TableField("systolic_bp")
    private Integer systolicBp;

    @ApiModelProperty("舒张压(低压 mmHg)")
    @TableField("diastolic_bp")
    private Integer diastolicBp;

    @ApiModelProperty("空腹血糖(mmol/L)")
    @TableField("blood_sugar")
    private BigDecimal bloodSugar;

    @ApiModelProperty("心率(次/分)")
    @TableField("heart_rate")
    private Integer heartRate;

    @ApiModelProperty("异常预警信息")
    @TableField("warning_msg")
    private String warningMsg;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
