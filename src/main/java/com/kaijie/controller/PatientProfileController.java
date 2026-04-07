package com.kaijie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaijie.entity.PatientProfile;
import com.kaijie.entity.User;
import com.kaijie.service.IPatientProfileService;
import com.kaijie.service.IUserService;

import java.util.List;

/**
 * <p>
 * 居民电子健康档案主表 前端控制器
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/patientProfile")
public class PatientProfileController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IPatientProfileService patientProfileService;

    /**
     * 获取管辖患者列表（演示用：直接从 sys_user 中查询 role_type = 2）
     * 支持按 realName 或 phone 模糊搜索
     */
    @GetMapping("/list")
    public List<User> list(@RequestParam(required = false) String realName,
                           @RequestParam(required = false) String phone) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        // role_type = 2 表示居民/患者
        qw.eq(User::getRoleType, 2);
        if (realName != null && !realName.trim().isEmpty()) {
            qw.like(User::getRealName, realName.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            qw.like(User::getPhone, phone.trim());
        }
        List<User> users = userService.list(qw);
        // 安全脱敏：返回前将 password 置空
        if (users != null) {
            users.forEach(u -> {
                if (u != null) {
                    u.setPassword(null);
                }
            });
        }
        return users;
    }

    /**
     * 查看指定患者的 EHR 档案（若未录入，返回空对象）
     */
    @GetMapping("/detail/{userId}")
    public PatientProfile detail(@PathVariable Long userId) {
        LambdaQueryWrapper<PatientProfile> qw = new LambdaQueryWrapper<>();
        qw.eq(PatientProfile::getUserId, userId);
        List<PatientProfile> list = patientProfileService.list(qw);
        if (list == null || list.isEmpty()) {
            return new PatientProfile();
        }
        return list.get(0);
    }

}
