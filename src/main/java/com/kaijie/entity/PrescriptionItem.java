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
 * 电子处方药品明细关联表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("med_prescription_item")
@ApiModel(value = "PrescriptionItem对象", description = "电子处方药品明细关联表")
public class PrescriptionItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("明细ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("关联处方主表ID")
    @TableField("prescription_id")
    private Long prescriptionId;

    @ApiModelProperty("关联药品库ID")
    @TableField("drug_id")
    private Long drugId;

    @ApiModelProperty("开药数量")
    @TableField("quantity")
    private Integer quantity;

    @ApiModelProperty("医生特别医嘱 (如: 饭后半小时服用)")
    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
