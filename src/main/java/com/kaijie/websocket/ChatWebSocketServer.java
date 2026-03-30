package com.kaijie.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kaijie.config.WebSocketBeanUtil;
import com.kaijie.dto.ChatMessageDTO;
import com.kaijie.entity.ChatMessage;
import com.kaijie.entity.ConsultationRecord;
import com.kaijie.entity.User;
import com.kaijie.mapper.ConsultationRecordMapper;
import com.kaijie.mapper.UserMapper;
import com.kaijie.security.JwtUtils;
import com.kaijie.service.IChatMessageService;
import com.kaijie.service.IConsultationRecordService;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 核心端点
 */
@Component
@ServerEndpoint("/api/ws/chat/{consultationId}/{token}")
public class ChatWebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // Following static fields requested by user for bean injection pain point
    private static IChatMessageService chatMessageServiceStatic;
    private static com.kaijie.security.JwtUtils jwtUtilsStatic;
    private static IConsultationRecordService consultationRecordServiceStatic;

    @OnOpen
    public void onOpen(Session session, @PathParam("consultationId") String consultationId, @PathParam("token") String token) {
        System.out.println("========== [WS] OnOpen 触发！房间号: " + consultationId + ", Token: " + token + " ==========");

        // parse token to get username
        String username = JwtUtils.getUsernameFromToken(token);
        if (username == null) {
            throw new IllegalStateException("无法从 token 中解析 username");
        }

        UserMapper userMapper = WebSocketBeanUtil.getBean(UserMapper.class);
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        if (user == null) throw new IllegalStateException("无法找到 token 对应的用户: " + username);

        // 在解析出 userId 之后，立刻去数据库查一下这个房间，做核心越权校验
        IConsultationRecordService consultationSvc = consultationRecordServiceStatic != null ? consultationRecordServiceStatic : WebSocketBeanUtil.getBean(IConsultationRecordService.class);
        ConsultationRecord record = consultationSvc.getById(Long.valueOf(consultationId));
        if (record == null) throw new IllegalStateException("无法找到问诊记录: " + consultationId);
        Long userId = user.getId();
        // 【核心越权校验】：如果你既不是这个房间的患者，也不是这个房间的医生，直接踹出去！
        if (!userId.equals(record.getPatientId()) && !userId.equals(record.getDoctorId())) {
            throw new RuntimeException("非法入侵：您无权进入该问诊室！");
        }

        String key = consultationId + "_" + user.getId();
        SESSION_MAP.put(key, session);
        System.out.println(">>> 用户 " + user.getId() + " 成功加入房间！当前在线人数: " + SESSION_MAP.size());
    }

    // Non-static setters to allow Spring to inject beans; they assign static fields
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setChatMessageService(IChatMessageService svc) {
        ChatWebSocketServer.chatMessageServiceStatic = svc;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setJwtUtils(com.kaijie.security.JwtUtils ju) {
        ChatWebSocketServer.jwtUtilsStatic = ju;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setConsultationRecordService(IConsultationRecordService svc) {
        ChatWebSocketServer.consultationRecordServiceStatic = svc;
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("consultationId") String consultationId, @PathParam("token") String token) throws IOException {
        System.out.println("========== [WS] 收到新消息: " + message + " ==========");

        // parse username
        String username = JwtUtils.getUsernameFromToken(token);
        if (username == null) throw new IllegalStateException("无法从 token 中解析 username");

        UserMapper userMapper = WebSocketBeanUtil.getBean(UserMapper.class);
        User sender = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        if (sender == null) throw new IllegalStateException("无法找到发送者用户: " + username);

        // parse incoming JSON to DTO
        ChatMessageDTO dto = OBJECT_MAPPER.readValue(message, ChatMessageDTO.class);

        // build ChatMessage entity
        ChatMessage chat = new ChatMessage();
        chat.setConsultationId(Long.valueOf(consultationId));
        chat.setSenderId(sender.getId());

        // determine receiver by looking up consultation record
        ConsultationRecordMapper crm = WebSocketBeanUtil.getBean(ConsultationRecordMapper.class);
        ConsultationRecord record = crm.selectById(Long.valueOf(consultationId));
        if (record == null) throw new IllegalStateException("无法找到问诊记录: " + consultationId);
        Long receiverId;
        if (sender.getId().equals(record.getPatientId())) receiverId = record.getDoctorId();
        else if (sender.getId().equals(record.getDoctorId())) receiverId = record.getPatientId();
        else throw new SecurityException("用户不属于该问诊会话");
        chat.setReceiverId(receiverId);

        // map msgType string to byte
        byte mt = 1; // default text
        if (dto.getMsgType() != null) {
            switch (dto.getMsgType()) {
                case "text": mt = 1; break;
                case "image": mt = 2; break;
                case "voice": mt = 3; break;
                default: mt = 1; break;
            }
        }
        chat.setMsgType(mt);
        chat.setContent(dto.getContent());
        chat.setCreateTime(LocalDateTime.now());

        // persist
        IChatMessageService chatService = chatMessageServiceStatic != null ? chatMessageServiceStatic : WebSocketBeanUtil.getBean(IChatMessageService.class);
        boolean saved = chatService.save(chat);
        if (!saved) throw new IllegalStateException("消息保存失败");

        // push to receiver if online
        String recvKey = consultationId + "_" + receiverId;
        Session recvSession = SESSION_MAP.get(recvKey);
        String outJson = OBJECT_MAPPER.writeValueAsString(chat);
        if (recvSession != null && recvSession.isOpen()) {
            recvSession.getBasicRemote().sendText(outJson);
            System.out.println(">>> 已将消息推送给用户 " + receiverId + "\n消息内容: " + outJson);
        } else {
            System.out.println(">>> 用户 " + receiverId + " 不在线或 session 已关闭，消息已保存至数据库");
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("consultationId") String consultationId, @PathParam("token") String token) {
        System.out.println("========== [WS] OnClose 触发，房间号: " + consultationId + " ==========");
        String username = JwtUtils.getUsernameFromToken(token);
        if (username == null) {
            System.out.println(">>> 无法从 token 中解析 username，跳过移除操作");
            return;
        }
        UserMapper userMapper = WebSocketBeanUtil.getBean(UserMapper.class);
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        if (user != null) {
            String key = consultationId + "_" + user.getId();
            SESSION_MAP.remove(key);
            System.out.println(">>> 用户 " + user.getId() + " 离开房间！当前在线人数: " + SESSION_MAP.size());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("consultationId") String consultationId, @PathParam("token") String token) {
        System.out.println("========== [WS] OnError 触发，房间号: " + consultationId + " 错误: " + throwable.getMessage());
        // 移除可能存在的 session
        try {
            String username = JwtUtils.getUsernameFromToken(token);
            if (username != null) {
                UserMapper userMapper = WebSocketBeanUtil.getBean(UserMapper.class);
                User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
                if (user != null) {
                    String key = consultationId + "_" + user.getId();
                    SESSION_MAP.remove(key);
                    System.out.println(">>> 已从在线列表移除用户 " + user.getId());
                }
            }
        } catch (Exception ex) {
            System.out.println(">>> OnError 移除 session 时发生异常: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}

