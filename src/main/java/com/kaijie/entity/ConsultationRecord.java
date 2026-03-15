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
 * 医患问诊会话主表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("im_consultation_record")
@ApiModel(value = "ConsultationRecord对象", description = "医患问诊会话主表")
public class ConsultationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会话ID (问诊单号)")
    @TableId("id")
    private Long id;

    @ApiModelProperty("居民ID")
    @TableField("patient_id")
    private Long patientId;

    @ApiModelProperty("接诊医生ID")
    @TableField("doctor_id")
    private Long doctorId;

    @ApiModelProperty("患者初始病情描述")
    @TableField("symptom_desc")
    private String symptomDesc;

    @ApiModelProperty("状态: 0-待接诊, 1-问诊中, 2-已结束, 3-已取消")
    @TableField("status")
    private Byte status;

    @ApiModelProperty("医生初步诊断建议")
    @TableField("diagnosis_result")
    private String diagnosisResult;

    @ApiModelProperty("接诊开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty("问诊结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
