package com.kaijie.service.impl;

import com.kaijie.entity.ConsultationRecord;
import com.kaijie.mapper.ConsultationRecordMapper;
import com.kaijie.service.IConsultationRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * <p>
 * 医患问诊会话主表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15

	@Autowired
	private UserMapper userMapper;

	/**
	 * Start a consultation initiated by username targeting doctorId.
	 * Must log exactly as requested and not swallow exceptions.
	 */
	public Long startConsultation(String username, Long doctorId) {
		System.out.println("========== [HTTP] 进入 startConsultation, 用户: " + username + ", 目标医生: " + doctorId + " ==========");

		if (username == null) throw new IllegalArgumentException("username 不能为空");

		// 查询用户信息以获取居民ID与roleType
		User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
		if (user == null) throw new IllegalStateException("无法找到用户: " + username);

		Integer roleType = user.getRoleType() == null ? null : (int) user.getRoleType();
		if (roleType == null || roleType != 2) {
			throw new SecurityException("只有居民用户可以发起问诊");
		}

		ConsultationRecord record = new ConsultationRecord();
		record.setPatientId(user.getId());
		record.setDoctorId(doctorId);
		record.setStatus((byte)0); // 0-进行中 (按要求)
		record.setStartTime(LocalDateTime.now());
		record.setCreateTime(LocalDateTime.now());

		System.out.println(">>> 准备执行 MyBatis-Plus 插入操作...");
		int rows = baseMapper.insert(record);
		if (rows <= 0) throw new IllegalStateException("插入问诊会话失败");
		System.out.println(">>> 插入成功！生成的问诊房间 ID: " + record.getId());

		return record.getId();
	}

}
