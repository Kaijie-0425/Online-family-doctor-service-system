package com.kaijie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kaijie.entity.ConsultationRecord;
import com.kaijie.entity.User;
import com.kaijie.mapper.ConsultationRecordMapper;
import com.kaijie.service.IUserService;
import com.kaijie.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 患者端历史就诊/挂号记录查询
 */
@RestController
@RequestMapping("/api/patient/records")
public class PatientRecordController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    /**
     * 查询当前患者的历史挂号/就诊记录
     * GET /api/patient/records/my
     * 返回字段：id, createTime, doctorName, department, status
     */
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> myRecords() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body(new ArrayList<>());
        }
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            return ResponseEntity.status(400).body(new ArrayList<>());
        }

        QueryWrapper<ConsultationRecord> qw = new QueryWrapper<>();
        qw.eq("patient_id", user.getId()).orderByDesc("create_time");
        List<ConsultationRecord> records = consultationRecordMapper.selectList(qw);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ConsultationRecord r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("createTime", r.getCreateTime());
            // doctor name
            String doctorName = "未知";
            if (r.getDoctorId() != null) {
                User doctor = userService.getById(r.getDoctorId());
                if (doctor != null) {
                    doctorName = doctor.getRealName() != null ? doctor.getRealName() : doctor.getUsername();
                }
            }
            m.put("doctorName", doctorName);
            // department currently not modeled on User, return null for now
            m.put("department", "外科");
            m.put("status", mapStatus(r.getStatus()));
            result.add(m);
        }

        return ResponseEntity.ok(result);
    }

    private String mapStatus(Byte status) {
        if (status == null) return "未知";
        switch (status) {
            case 0:
                return "待接诊";
            case 1:
                return "问诊中";
            case 2:
                return "已结束";
            case 3:
                return "已取消";
            default:
                return "未知";
        }
    }
}

