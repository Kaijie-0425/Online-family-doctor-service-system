package com.kaijie.controller;

import com.kaijie.dto.AppointmentCreateDTO;
import com.kaijie.dto.ScheduleCreateDTO;
import com.kaijie.entity.Schedule;
import com.kaijie.service.impl.AppointmentServiceImpl;
import com.kaijie.service.impl.ScheduleServiceImpl;
import com.kaijie.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinic")
public class DocClinicController {

    @Autowired
    private ScheduleServiceImpl scheduleService;

    @Autowired
    private AppointmentServiceImpl appointmentService;

    /**
     * 医生发布排班
     */
    @PostMapping("/schedule")
    public ResponseEntity<String> createSchedule(@RequestBody ScheduleCreateDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        try {
            String res = scheduleService.createSchedule(username, dto);
            return ResponseEntity.ok(res);
        } catch (RuntimeException ex) {
            // 将异常消息放到响应体中
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    /**
     * 查询医生排班
     */
    @GetMapping("/schedule/list")
    public ResponseEntity<?> getDoctorSchedules(@RequestParam Long doctorId,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate) {
        try {
            List<Schedule> list = scheduleService.getDoctorSchedules(doctorId, startDate, endDate);
            return ResponseEntity.ok(list);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    /**
     * 居民发起预约
     */
    @PostMapping("/appointment")
    public ResponseEntity<String> makeAppointment(@RequestBody AppointmentCreateDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        try {
            String res = appointmentService.makeAppointment(username, dto);
            return ResponseEntity.ok(res);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}


