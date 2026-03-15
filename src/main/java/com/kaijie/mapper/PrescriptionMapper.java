package com.kaijie.mapper;

import com.kaijie.entity.Prescription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 电子建议处方主表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {

}
