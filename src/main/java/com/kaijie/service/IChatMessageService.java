package com.kaijie.service;

import com.kaijie.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * WebSocket聊天消息流水表 服务类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
public interface IChatMessageService extends IService<ChatMessage> {

    // 获取历史消息，按发送时间升序
    List<ChatMessage> getHistoryMessages(Long consultationId);

}
