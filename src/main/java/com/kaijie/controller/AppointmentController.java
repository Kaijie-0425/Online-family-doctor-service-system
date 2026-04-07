package com.kaijie.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kaijie.service.IAppointmentServiceCustom;
import com.kaijie.service.IUserService;
import com.kaijie.entity.User;
import com.kaijie.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 居民预约挂号表 前端控制器
 * </p>
 *
 * @author kaijie
 * @since 2026-03-29
 */
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

	@Autowired
	private IAppointmentServiceCustom appointmentService;

	@Autowired
	private IUserService userService;

	/**
	 * 查询当前可挂号医生及可用号源
	 */
	@GetMapping("/list")
	public ResponseEntity<List<Map<String, Object>>> listAvailable() {
		List<Map<String, Object>> list = appointmentService.listAvailableAppointments();
		return ResponseEntity.ok(list);
	}

	/**
	 * 挂号（强制使用当前登录用户作为 patient）
	 */
	@PostMapping("/book")
	public ResponseEntity<String> book(@RequestParam("scheduleId") Long scheduleId) {
		String username = SecurityUtils.getCurrentUsername();
		if (username == null) return ResponseEntity.status(401).body("未登录");
		User user = userService.lambdaQuery().eq(User::getUsername, username).one();
		if (user == null) return ResponseEntity.status(400).body("用户不存在");
		try {
			appointmentService.bookAppointmentByPatient(user.getId(), scheduleId);
			return ResponseEntity.ok("挂号成功");
		} catch (RuntimeException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

}
