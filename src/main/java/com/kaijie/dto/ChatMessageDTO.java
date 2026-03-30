package com.kaijie.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 简单的传输 DTO，用于 WebSocket 前端/后端交换消息
 */
@Getter
@Setter
public class ChatMessageDTO {
    // 消息类型，例如: text/image
    private String msgType;
    // 消息内容，文本或资源 URL
    private String content;
}

