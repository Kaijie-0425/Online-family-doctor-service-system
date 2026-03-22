package com.kaijie.service;

import com.kaijie.entity.PatientProfile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 居民电子健康档案主表 服务类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
public interface IPatientProfileService extends IService<PatientProfile> {

    // 根据 username 获取居民档案
    PatientProfile getProfileByUsername(String username);

    // 根据 username 保存或更新居民档案
    boolean saveOrUpdateProfile(String username, PatientProfile profile);

}
