package com.kaijie.mapper;

import com.kaijie.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * WebSocket聊天消息流水表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
