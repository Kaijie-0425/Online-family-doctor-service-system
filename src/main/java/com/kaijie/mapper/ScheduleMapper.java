package com.kaijie.mapper;

import com.kaijie.entity.Schedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 医生排班表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

}
