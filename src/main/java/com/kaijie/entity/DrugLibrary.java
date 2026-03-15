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
 * 标准药品基础数据库
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("med_drug_library")
@ApiModel(value = "DrugLibrary对象", description = "标准药品基础数据库")
public class DrugLibrary implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("药品ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("药品通用名")
    @TableField("drug_name")
    private String drugName;

    @ApiModelProperty("规格 (如: 0.25g*24粒/盒)")
    @TableField("specification")
    private String specification;

    @ApiModelProperty("适应症")
    @TableField("indications")
    private String indications;

    @ApiModelProperty("禁忌症 (用于配伍禁忌校验)")
    @TableField("contraindications")
    private String contraindications;

    @ApiModelProperty("用法用量")
    @TableField("usage_dosage")
    private String usageDosage;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
