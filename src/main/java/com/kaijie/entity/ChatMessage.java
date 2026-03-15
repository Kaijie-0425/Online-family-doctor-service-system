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
 * WebSocket聊天消息流水表
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("im_chat_message")
@ApiModel(value = "ChatMessage对象", description = "WebSocket聊天消息流水表")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("消息ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("关联的问诊会话ID")
    @TableField("consultation_id")
    private Long consultationId;

    @ApiModelProperty("发送者ID")
    @TableField("sender_id")
    private Long senderId;

    @ApiModelProperty("接收者ID")
    @TableField("receiver_id")
    private Long receiverId;

    @ApiModelProperty("消息类型: 1-文本, 2-图片, 3-语音")
    @TableField("msg_type")
    private Byte msgType;

    @ApiModelProperty("消息内容 (文字内容或图片/语音的MinIO URL)")
    @TableField("content")
    private String content;

    @ApiModelProperty("语音时长(秒)，仅msg_type=3时有值")
    @TableField("duration")
    private Integer duration;

    @ApiModelProperty("是否已读: 0-未读, 1-已读")
    @TableField("is_read")
    private Byte isRead;

    @ApiModelProperty("发送时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
