package com.kaijie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaijie.entity.User;
import com.kaijie.mapper.UserMapper;
import com.kaijie.service.IDoctorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<UserMapper, User> implements IDoctorService {

    private final UserMapper userMapper;

    public DoctorServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<Map<String, Object>> listDoctors() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        // role_type: 1 表示家庭医生
        qw.eq("role_type", 1);
        qw.select("id", "username", "real_name", "avatar");
        List<User> users = userMapper.selectList(qw);
        return users.stream().map(u -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getRealName() != null ? u.getRealName() : u.getUsername());
            m.put("department", null);
            m.put("title", null);
            m.put("avatar", u.getAvatar());
            return m;
        }).collect(Collectors.toList());
    }
}


