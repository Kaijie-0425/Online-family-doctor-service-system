package com.kaijie.dto;

import lombok.Data;

/**
 * 体征录入 DTO
 */
@Data
public class VitalSignRecordDTO {
    private Integer systolicBp;
    private Integer diastolicBp;
    private Double bloodSugar;
    private Integer heartRate;
}

