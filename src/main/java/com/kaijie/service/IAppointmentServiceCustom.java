package com.kaijie.service;

import java.util.List;
import java.util.Map;

public interface IAppointmentServiceCustom {
    /**
     * 列出基于排班的可挂号记录（每条为一个 schedule 的剩余号源）
     */
    List<Map<String, Object>> listAvailableAppointments();

    /**
     * 使用排班 ID 为当前患者挂号（原子性扣减 available_capacity，并插入挂号/会话记录）
     * @param patientId 患者ID
     * @param scheduleId 排班ID
     */
    void bookAppointmentByPatient(Long patientId, Long scheduleId);
}

