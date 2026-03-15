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
 * 电子建议处方主表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("med_prescription")
@ApiModel(value = "Prescription对象", description = "电子建议处方主表")
public class Prescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("处方ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("关联的问诊会话ID (开处方必须基于某次问诊)")
    @TableField("consultation_id")
    private Long consultationId;

    @ApiModelProperty("开方医生ID")
    @TableField("doctor_id")
    private Long doctorId;

    @ApiModelProperty("患者ID")
    @TableField("patient_id")
    private Long patientId;

    @ApiModelProperty("临床诊断")
    @TableField("clinical_diagnosis")
    private String clinicalDiagnosis;

    @ApiModelProperty("状态: 0-待药师审核, 1-已生效, 2-已驳回, 3-已失效")
    @TableField("status")
    private Byte status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
