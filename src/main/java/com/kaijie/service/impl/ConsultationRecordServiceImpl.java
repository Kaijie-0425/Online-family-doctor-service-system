package com.kaijie.service.impl;

import com.kaijie.entity.ConsultationRecord;
import com.kaijie.mapper.ConsultationRecordMapper;
import com.kaijie.service.IConsultationRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医患问诊会话主表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
public class ConsultationRecordServiceImpl extends ServiceImpl<ConsultationRecordMapper, ConsultationRecord> implements IConsultationRecordService {

}
