package com.kaijie.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleCreateDTO {
    /** 格式 yyyy-MM-dd */
    private String scheduleDate;

    /** 1-上午, 2-下午 */
    private Integer shiftType;

    /** 最大放号量 */
    private Integer maxCapacity;
}

