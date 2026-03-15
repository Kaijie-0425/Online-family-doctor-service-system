package com.kaijie.service.impl;

import com.kaijie.entity.PatientProfile;
import com.kaijie.mapper.PatientProfileMapper;
import com.kaijie.service.IPatientProfileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
