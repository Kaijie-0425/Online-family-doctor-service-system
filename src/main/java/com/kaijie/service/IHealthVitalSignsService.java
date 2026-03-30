package com.kaijie.service;

import com.kaijie.entity.HealthVitalSigns;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 居民体征数据表 服务类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-30
 */
public interface IHealthVitalSignsService extends IService<HealthVitalSigns> {

	/**
	 * 使用症状描述调用大模型给出评估建议
	 */
	String assessSymptoms(String username, com.kaijie.dto.SymptomAssessDTO dto);

	/**
	 * 居民录入体征并可能触发预警
	 */
	String recordVitalSigns(String username, com.kaijie.dto.VitalSignRecordDTO dto);

	/**
	 * 获取最近 days 天的体征走势
	 */
	java.util.List<HealthVitalSigns> getMyVitalSignsTrend(String username, Integer days);

}
