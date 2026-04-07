package com.kaijie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaijie.entity.Schedule;
import com.kaijie.entity.User;
import com.kaijie.security.SecurityUtils;
import com.kaijie.service.IScheduleService;
import com.kaijie.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 医生排班表 前端控制器
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

	@Autowired
	private IScheduleService scheduleService;

	@Autowired
	private IUserService userService;

	/**
	 * 获取当前登录医生的未来排班（含今天）
	 */
	@GetMapping("/my")
	public ResponseEntity<List<Schedule>> mySchedules() {
		String username = SecurityUtils.getCurrentUsername();
		if (username == null) return ResponseEntity.status(401).build();

		User user = userService.lambdaQuery().eq(User::getUsername, username).one();
		if (user == null) return ResponseEntity.badRequest().build();

		LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<>();
		qw.eq(Schedule::getDoctorId, user.getId())
				.ge(Schedule::getScheduleDate, LocalDate.now())
				.orderByAsc(Schedule::getScheduleDate)
				.orderByAsc(Schedule::getShiftType);

		List<Schedule> list = scheduleService.list(qw);
		return ResponseEntity.ok(list);
	}

	/**
	 * 发布或修改排班
	 */
	@PostMapping("/save")
	public ResponseEntity<String> save(@RequestBody Schedule schedule) {
		try {
			String username = SecurityUtils.getCurrentUsername();
			if (username == null) return ResponseEntity.status(401).body("未登录");

			User user = userService.lambdaQuery().eq(User::getUsername, username).one();
			if (user == null) return ResponseEntity.badRequest().body("用户不存在");

			if (schedule.getScheduleDate() == null || schedule.getShiftType() == null || schedule.getMaxCapacity() == null) {
				return ResponseEntity.badRequest().body("缺少必要参数: scheduleDate, shiftType, maxCapacity");
			}

			// 查找是否存在相同医生、日期、班次的记录
			LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<>();
			qw.eq(Schedule::getDoctorId, user.getId())
					.eq(Schedule::getScheduleDate, schedule.getScheduleDate())
					.eq(Schedule::getShiftType, schedule.getShiftType());

			Schedule exists = scheduleService.getOne(qw);
			if (exists == null) {
				// 新增：availableCapacity 设置为传入的 maxCapacity
				schedule.setDoctorId(user.getId());
				schedule.setAvailableCapacity(schedule.getMaxCapacity());
				scheduleService.save(schedule);
				return ResponseEntity.ok("排班发布成功");
			} else {
				// 存在：检查是否有人挂号
				Integer avail = exists.getAvailableCapacity();
				Integer max = exists.getMaxCapacity();
				if (avail != null && max != null && avail.compareTo(max) < 0) {
					throw new RuntimeException("已有患者预约，无法修改该排班！");
				}
				// 未有挂号，允许修改 max 和 available
				exists.setMaxCapacity(schedule.getMaxCapacity());
				exists.setAvailableCapacity(schedule.getMaxCapacity());
				scheduleService.updateById(exists);
				return ResponseEntity.ok("排班修改成功");
			}
		} catch (RuntimeException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

}
