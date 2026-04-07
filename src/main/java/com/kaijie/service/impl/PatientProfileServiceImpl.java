package com.kaijie.service.impl;

import com.kaijie.entity.PatientProfile;
import com.kaijie.entity.User;
import com.kaijie.mapper.PatientProfileMapper;
import com.kaijie.mapper.UserMapper;
import com.kaijie.service.IPatientProfileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 居民电子健康档案主表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
public class PatientProfileServiceImpl extends ServiceImpl<PatientProfileMapper, PatientProfile> implements IPatientProfileService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PatientProfile getProfileByUsername(String username) {
        if (username == null) return null;
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) return null;
        Long userId = user.getId();
        if (userId == null) return null;
        return this.baseMapper.selectOne(new QueryWrapper<PatientProfile>().eq("user_id", userId));
    }

    @Override
    public boolean saveOrUpdateProfile(String username, PatientProfile profile) {
        if (username == null || profile == null) return false;
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || user.getId() == null) return false;
        Long userId = user.getId();
        // 强制设置 userId，防止越权
        profile.setUserId(userId);
        // 同步冗余的 username 到档案表
        profile.setUserName(username);

        // 检查是否已存在该用户的档案
        PatientProfile existing = this.baseMapper.selectOne(new QueryWrapper<PatientProfile>().eq("user_id", userId));
        if (existing != null) {
            // 保留 id 以便 updateById
            profile.setId(existing.getId());
            return this.updateById(profile);
        } else {
            return this.save(profile);
        }
    }
}
