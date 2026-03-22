package com.kaijie.service;

import com.kaijie.entity.Contract;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 家庭医生签约流程表 服务类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
public interface IContractService extends IService<Contract> {

    // 居民端申请签约
    String applyContract(String username, com.kaijie.dto.ContractApplyDTO dto);

    // 医生端审核签约
    String auditContract(String username, com.kaijie.dto.ContractAuditDTO dto);

    // 获取当前用户相关的签约记录，roleType: 1-医生,2-居民
    List<Contract> getMyContracts(String username, Integer roleType);

}
