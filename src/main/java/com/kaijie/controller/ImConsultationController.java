package com.kaijie.controller;

import com.kaijie.entity.ChatMessage;
import com.kaijie.service.IChatMessageService;
import com.kaijie.service.IConsultationRecordService;
import com.kaijie.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImConsultationController {

    @Autowired
    private IConsultationRecordService consultationRecordService;

    @Autowired
    private IChatMessageService chatMessageService;

    @PostMapping("/api/consultation/start")
    public Long startConsultation(@RequestParam Long doctorId) {
        String username = SecurityUtils.getCurrentUsername();
        return consultationRecordService.startConsultation(username, doctorId);
    }

    @GetMapping("/api/consultation/history")
    public List<ChatMessage> getHistory(@RequestParam Long consultationId) {
        return chatMessageService.getHistoryMessages(consultationId);
    }
}


