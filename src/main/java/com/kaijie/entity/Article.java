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
 * 健康科普资讯文章表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("cms_article")
@ApiModel(value = "Article对象", description = "健康科普资讯文章表")
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文章ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("发布者ID (关联医生或管理员)")
    @TableField("author_id")
    private Long authorId;

    @ApiModelProperty("文章标题")
    @TableField("title")
    private String title;

    @ApiModelProperty("分类 (如: 老年病、儿科、秋季养生)")
    @TableField("category")
    private String category;

    @ApiModelProperty("封面图URL")
    @TableField("cover_image")
    private String coverImage;

    @ApiModelProperty("富文本内容 (HTML格式)")
    @TableField("content")
    private String content;

    @ApiModelProperty("浏览量 (后期结合Redis做防刷)")
    @TableField("view_count")
    private Integer viewCount;

    @ApiModelProperty("点赞数")
    @TableField("like_count")
    private Integer likeCount;

    @ApiModelProperty("状态: 0-草稿, 1-已发布")
    @TableField("publish_status")
    private Byte publishStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
