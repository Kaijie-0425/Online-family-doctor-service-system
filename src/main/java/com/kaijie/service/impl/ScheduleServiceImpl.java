package com.kaijie.service.impl;

import com.kaijie.dto.ScheduleCreateDTO;
import com.kaijie.entity.Schedule;
import com.kaijie.entity.User;
import com.kaijie.mapper.ScheduleMapper;
import com.kaijie.service.IScheduleService;
import com.kaijie.service.IUserService;
import com.kaijie.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 医生排班表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {

	@Autowired
	private IUserService userService;

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * 医生发布排班
	 */
	public String createSchedule(String username, ScheduleCreateDTO dto) {
		if (username == null) username = SecurityUtils.getCurrentUsername();
		if (username == null) throw new RuntimeException("未登录");

		User user = userService.lambdaQuery().eq(User::getUsername, username).one();
		if (user == null) throw new RuntimeException("用户不存在");
		if (user.getRoleType() == null || user.getRoleType() != 1) throw new RuntimeException("只有医生可以发布排班");

		LocalDate scheduleDate;
		try {
			scheduleDate = LocalDate.parse(dto.getScheduleDate(), DATE_FORMAT);
		} catch (Exception ex) {
			throw new RuntimeException("日期格式错误，需为 yyyy-MM-dd");
		}

		// 校验重复排班：同一医生，同一日期，同一班次
		QueryWrapper<Schedule> qw = new QueryWrapper<>();
		qw.eq("doctor_id", user.getId())
				.eq("schedule_date", scheduleDate)
				.eq("shift_type", dto.getShiftType());
		Long exists = this.baseMapper.selectCount(qw);
		if (exists != null && exists > 0) throw new RuntimeException("该时段已排班，请勿重复操作");

		Schedule schedule = new Schedule();
		schedule.setDoctorId(user.getId());
		schedule.setScheduleDate(scheduleDate);
		schedule.setShiftType(dto.getShiftType().byteValue());
		schedule.setMaxCapacity(dto.getMaxCapacity());
		schedule.setAvailableCapacity(dto.getMaxCapacity());

		this.baseMapper.insert(schedule);
		return "排班发布成功";
	}

	/**
	 * 根据医生ID和时间范围查询排班
	 */
	public List<Schedule> getDoctorSchedules(Long doctorId, String startDate, String endDate) {
		QueryWrapper<Schedule> qw = new QueryWrapper<>();
		qw.eq(doctorId != null, "doctor_id", doctorId);
		if (startDate != null && !startDate.isEmpty()) {
			LocalDate s = LocalDate.parse(startDate, DATE_FORMAT);
			qw.ge("schedule_date", s);
		}
		if (endDate != null && !endDate.isEmpty()) {
			LocalDate e = LocalDate.parse(endDate, DATE_FORMAT);
			qw.le("schedule_date", e);
		}
		qw.orderByAsc("schedule_date", "shift_type");
		return this.baseMapper.selectList(qw);
	}

}
