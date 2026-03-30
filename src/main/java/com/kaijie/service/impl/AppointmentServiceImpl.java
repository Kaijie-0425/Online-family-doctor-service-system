package com.kaijie.service.impl;

import com.kaijie.dto.AppointmentCreateDTO;
import com.kaijie.entity.Appointment;
import com.kaijie.entity.Schedule;
import com.kaijie.entity.User;
import com.kaijie.mapper.AppointmentMapper;
import com.kaijie.mapper.ScheduleMapper;
import com.kaijie.service.IAppointmentService;
import com.kaijie.service.IUserService;
import com.kaijie.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 居民预约挂号表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements IAppointmentService {

	@Autowired
	private IUserService userService;

	@Autowired
	private ScheduleMapper scheduleMapper;

	/**
	 * 居民发起预约
	 */
	public String makeAppointment(String username, AppointmentCreateDTO dto) {
		if (username == null) username = SecurityUtils.getCurrentUsername();
		if (username == null) throw new RuntimeException("未登录");

		User user = userService.lambdaQuery().eq(User::getUsername, username).one();
		if (user == null) throw new RuntimeException("用户不存在");
		if (user.getRoleType() == null || user.getRoleType() != 2) throw new RuntimeException("只有居民用户可以预约挂号");

		Long scheduleId = dto.getScheduleId();
		if (scheduleId == null) throw new RuntimeException("缺少排班ID");

		Schedule schedule = scheduleMapper.selectById(scheduleId);
		if (schedule == null) throw new RuntimeException("排班不存在");

		// 排班日期已过
		LocalDate today = LocalDate.now();
		if (schedule.getScheduleDate().isBefore(today)) {
			throw new RuntimeException("排班已过，无法预约");
		}

		// available_capacity > 0 check and atomic decrement
		UpdateWrapper<Schedule> uw = new UpdateWrapper<>();
		uw.eq("id", scheduleId).gt("available_capacity", 0)
				.setSql("available_capacity = available_capacity - 1");
		int updated = scheduleMapper.update(null, uw);
		if (updated <= 0) {
			throw new RuntimeException("该排班号源已满");
		}

		// 防止重复预约：同一患者同一排班不能重复预约
		QueryWrapper<Appointment> qw = new QueryWrapper<>();
		qw.eq("patient_id", user.getId()).eq("schedule_id", scheduleId);
		Long exists = this.baseMapper.selectCount(qw);
		if (exists != null && exists > 0) {
			// 恢复库存（在高并发下，这里只是补偿性操作；严格情况建议使用分布式锁或最终一致性处理）
			UpdateWrapper<Schedule> uwRecover = new UpdateWrapper<>();
			uwRecover.eq("id", scheduleId).setSql("available_capacity = available_capacity + 1");
			scheduleMapper.update(null, uwRecover);
			throw new RuntimeException("您已预约过该排班，不能重复预约");
		}

		Appointment appointment = new Appointment();
		appointment.setPatientId(user.getId());
		appointment.setScheduleId(scheduleId);
		appointment.setStatus((byte)0);
		appointment.setCreateTime(LocalDateTime.now());

		this.baseMapper.insert(appointment);

		return "预约成功";
	}

}
