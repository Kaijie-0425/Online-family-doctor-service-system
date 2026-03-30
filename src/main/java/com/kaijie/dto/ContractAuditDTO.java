package com.kaijie.dto;

import lombok.Data;

@Data
public class ContractAuditDTO {
    private Long contractId;
    private Integer auditStatus; // 1-同意, 2-拒绝
    private String rejectReason;
}

