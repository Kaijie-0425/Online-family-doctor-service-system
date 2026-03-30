package com.kaijie.mapper;

import com.kaijie.entity.Appointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 居民预约挂号表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

}
