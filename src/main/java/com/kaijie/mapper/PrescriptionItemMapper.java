package com.kaijie.mapper;

import com.kaijie.entity.PrescriptionItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 电子处方药品明细关联表 Mapper 接口
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {

}
