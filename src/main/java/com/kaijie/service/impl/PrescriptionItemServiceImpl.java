package com.kaijie.service.impl;

import com.kaijie.entity.PrescriptionItem;
import com.kaijie.mapper.PrescriptionItemMapper;
import com.kaijie.service.IPrescriptionItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 电子处方药品明细关联表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
public class PrescriptionItemServiceImpl extends ServiceImpl<PrescriptionItemMapper, PrescriptionItem> implements IPrescriptionItemService {

}
