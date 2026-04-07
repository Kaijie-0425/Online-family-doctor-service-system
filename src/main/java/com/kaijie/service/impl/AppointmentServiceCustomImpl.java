package com.kaijie.service.impl;

import com.kaijie.entity.Appointment;
import com.kaijie.entity.ConsultationRecord;
import com.kaijie.entity.Schedule;
import com.kaijie.entity.User;
import com.kaijie.mapper.AppointmentMapper;
import com.kaijie.mapper.ConsultationRecordMapper;
import com.kaijie.mapper.ScheduleMapper;
import com.kaijie.mapper.UserMapper;
import com.kaijie.service.IAppointmentServiceCustom;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentServiceCustomImpl implements IAppointmentServiceCustom {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * 查询可挂号医生及剩余号源
     */
    @Override
    public List<Map<String, Object>> listAvailableAppointments() {
        // 查询排班表中剩余号源 > 0 且 schedule_date >= today 的记录
        QueryWrapper<Schedule> sq = new QueryWrapper<>();
        sq.gt("available_capacity", 0).ge("schedule_date", java.time.LocalDate.now());
        sq.select("id", "doctor_id", "schedule_date", "shift_type", "available_capacity");
        sq.orderByAsc("schedule_date", "shift_type");
        List<Schedule> schedules = scheduleMapper.selectList(sq);

        List<Map<String, Object>> res = new ArrayList<>();
        for (Schedule s : schedules) {
            Map<String, Object> item = new HashMap<>();
            item.put("scheduleId", s.getId());
            // 获取医生名称
            User d = userMapper.selectById(s.getDoctorId());
            String name = (d != null && d.getRealName() != null) ? d.getRealName() : (d != null ? d.getUsername() : null);
            item.put("doctorName", name);
            item.put("scheduleDate", s.getScheduleDate());
            item.put("shiftType", s.getShiftType());
            item.put("availableCapacity", s.getAvailableCapacity());
            res.add(item);
        }
        return res;
    }

    /**
     * 防超卖挂号：在事务中使用条件更新减少 available_capacity > 0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookAppointmentByPatient(Long patientId, Long scheduleId) {
        if (patientId == null || scheduleId == null) throw new RuntimeException("参数错误");

        // 先查询排班，确保存在且获取 doctorId
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) throw new RuntimeException("排班不存在");
        if (schedule.getAvailableCapacity() == null || schedule.getAvailableCapacity() <= 0) {
            throw new RuntimeException("号源不足，挂号失败");
        }

        // 原子条件更新：根据 scheduleId 扣减 available_capacity
        UpdateWrapper<Schedule> uw = new UpdateWrapper<>();
        uw.eq("id", scheduleId).gt("available_capacity", 0).setSql("available_capacity = available_capacity - 1");
        int updated = scheduleMapper.update(null, uw);
        if (updated <= 0) {
            throw new RuntimeException("号源不足，挂号失败");
        }

        // 扣减成功后，插入 ConsultationRecord（保存患者ID、医生ID、挂号时间）
        ConsultationRecord cr = new ConsultationRecord();
        cr.setDoctorId(schedule.getDoctorId());
        cr.setPatientId(patientId);
        cr.setCreateTime(LocalDateTime.now());
        cr.setStatus((byte)0);
        consultationRecordMapper.insert(cr);

        // 插入 Appointment 表（保存 scheduleId）
        Appointment ap = new Appointment();
        ap.setPatientId(patientId);
        ap.setScheduleId(scheduleId);
        ap.setStatus((byte)0);
        ap.setCreateTime(LocalDateTime.now());
        appointmentMapper.insert(ap);
    }
}


