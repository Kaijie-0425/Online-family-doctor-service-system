package com.kaijie.service.impl;

import com.kaijie.entity.ChatMessage;
import com.kaijie.mapper.ChatMessageMapper;
import com.kaijie.service.IChatMessageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * WebSocket聊天消息流水表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Override
    public List<ChatMessage> getHistoryMessages(Long consultationId) {
        if (consultationId == null) return java.util.Collections.emptyList();
        QueryWrapper<ChatMessage> q = new QueryWrapper<>();
        q.eq("consultation_id", consultationId).orderByAsc("create_time");
        return this.list(q);
    }

}
