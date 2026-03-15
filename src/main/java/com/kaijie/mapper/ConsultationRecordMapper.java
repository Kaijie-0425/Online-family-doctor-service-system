package com.kaijie.mapper;

import com.kaijie.entity.ConsultationRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 医患问诊会话主表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Mapper
public interface ConsultationRecordMapper extends BaseMapper<ConsultationRecord> {

}
