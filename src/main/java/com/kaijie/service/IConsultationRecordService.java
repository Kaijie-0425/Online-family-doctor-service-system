package com.kaijie.service;

import com.kaijie.entity.ConsultationRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 医患问诊会话主表 服务类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
public interface IConsultationRecordService extends IService<ConsultationRecord> {

    // 启动问诊，会返回生成的问诊记录 ID
    Long startConsultation(String username, Long doctorId);

}
