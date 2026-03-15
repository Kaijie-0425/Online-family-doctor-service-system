package com.kaijie.service.impl;

import com.kaijie.entity.HealthDailyRecord;
import com.kaijie.mapper.HealthDailyRecordMapper;
import com.kaijie.service.IHealthDailyRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 居民每日体征数据记录表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
public class HealthDailyRecordServiceImpl extends ServiceImpl<HealthDailyRecordMapper, HealthDailyRecord> implements IHealthDailyRecordService {

}
