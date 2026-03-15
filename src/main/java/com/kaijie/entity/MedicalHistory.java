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
 * 居民既往病史与慢性病标签表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("ehr_medical_history")
@ApiModel(value = "MedicalHistory对象", description = "居民既往病史与慢性病标签表")
public class MedicalHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("关联居民ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("疾病名称 (如: 高血压、糖尿病)")
    @TableField("disease_name")
    private String diseaseName;

    @ApiModelProperty("类型: 1-慢性病, 2-手术史, 3-遗传病")
    @TableField("disease_type")
    private Byte diseaseType;

    @ApiModelProperty("确诊日期")
    @TableField("diagnose_date")
    private LocalDate diagnoseDate;

    @ApiModelProperty("确诊医院")
    @TableField("hospital_name")
    private String hospitalName;

    @ApiModelProperty("当前状态: 0-已治愈, 1-治疗/控制中")
    @TableField("current_status")
    private Byte currentStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
