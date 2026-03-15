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
 * 家庭医生签约流程表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("doc_contract")
@ApiModel(value = "Contract对象", description = "家庭医生签约流程表")
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("签约记录ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("居民ID")
    @TableField("patient_id")
    private Long patientId;

    @ApiModelProperty("家庭医生ID")
    @TableField("doctor_id")
    private Long doctorId;

    @ApiModelProperty("签约状态: 0-待审核, 1-已签约, 2-已拒绝, 3-已解约, 4-已过期")
    @TableField("contract_status")
    private Byte contractStatus;

    @ApiModelProperty("居民申请签约时的留言")
    @TableField("apply_reason")
    private String applyReason;

    @ApiModelProperty("医生拒绝签约的原因")
    @TableField("reject_reason")
    private String rejectReason;

    @ApiModelProperty("服务生效日期")
    @TableField("start_date")
    private LocalDate startDate;

    @ApiModelProperty("服务到期日期 (通常签1年)")
    @TableField("end_date")
    private LocalDate endDate;

    @ApiModelProperty("电子协议书存储路径 (MinIO)")
    @TableField("contract_file_url")
    private String contractFileUrl;

    @ApiModelProperty("发起申请时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("状态变更时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
