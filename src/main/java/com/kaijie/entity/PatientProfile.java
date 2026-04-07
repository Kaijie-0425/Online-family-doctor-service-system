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
 * 居民电子健康档案主表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("ehr_patient_profile")
@ApiModel(value = "PatientProfile对象", description = "居民电子健康档案主表")
public class PatientProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("档案ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("关联sys_user表的居民ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("关联sys_user表的居民登录名（冗余，便于查询与展示）")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty("身份证号 (建议后端脱敏/加密存储)")
    @TableField("id_card")
    private String idCard;

    @ApiModelProperty("出生日期")
    @TableField("birthday")
    private LocalDate birthday;

    @ApiModelProperty("血型 (A/B/AB/O/RH阴性等)")
    @TableField("blood_type")
    private String bloodType;

    @ApiModelProperty("身高 (cm)")
    @TableField("height")
    private BigDecimal height;

    @ApiModelProperty("体重 (kg) - 用于后期计算BMI")
    @TableField("weight")
    private BigDecimal weight;

    @ApiModelProperty("过敏史 (逗号分隔或JSON)")
    @TableField("allergies")
    private String allergies;

    @ApiModelProperty("紧急联系人姓名")
    @TableField("emergency_contact_name")
    private String emergencyContactName;

    @ApiModelProperty("紧急联系人电话")
    @TableField("emergency_contact_phone")
    private String emergencyContactPhone;

    @ApiModelProperty("建档时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("最后更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除: 0-未删, 1-已删")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
