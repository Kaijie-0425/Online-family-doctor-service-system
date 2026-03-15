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
 * 系统核心用户表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "User对象", description = "系统核心用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID (雪花算法)")
    @TableId("id")
    private Long id;

    @ApiModelProperty("登录账号 (手机号或自定义账号)")
    @TableField("username")
    private String username;

    @ApiModelProperty("加密后的密码 (BCrypt)")
    @TableField("password")
    private String password;

    @ApiModelProperty("真实姓名")
    @TableField("real_name")
    private String realName;

    @ApiModelProperty("角色类型: 0-超级管理员, 1-家庭医生, 2-居民用户")
    @TableField("role_type")
    private Byte roleType;

    @ApiModelProperty("联系电话")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("头像URL (存MinIO地址)")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty("性别: 0-未知, 1-男, 2-女")
    @TableField("gender")
    private Byte gender;

    @ApiModelProperty("账号状态: 0-禁用, 1-正常")
    @TableField("status")
    private Byte status;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除标识: 0-未删除, 1-已删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
