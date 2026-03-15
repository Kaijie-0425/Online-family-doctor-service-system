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
 * 算法生成的每日膳食与运动推荐表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("diet_exercise_plan")
@ApiModel(value = "DietExercisePlan对象", description = "算法生成的每日膳食与运动推荐表")
public class DietExercisePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("计划ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("居民ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("计划执行日期")
    @TableField("plan_date")
    private LocalDate planDate;

    @ApiModelProperty("算法计算的推荐摄入总热量(kcal)")
    @TableField("target_calories")
    private Integer targetCalories;

    @ApiModelProperty("一日三餐推荐食谱 (使用JSON数组存储，避免过度建表)")
    @TableField("meal_plan_json")
    private String mealPlanJson;

    @ApiModelProperty("推荐运动消耗计划 (如: 慢跑30分钟，消耗300kcal)")
    @TableField("exercise_plan")
    private String exercisePlan;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
