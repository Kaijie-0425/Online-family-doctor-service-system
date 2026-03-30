package com.kaijie.service.impl;

import com.kaijie.dto.ContractApplyDTO;
import com.kaijie.dto.ContractAuditDTO;
import com.kaijie.entity.Contract;
import com.kaijie.entity.User;
import com.kaijie.mapper.ContractMapper;
import com.kaijie.mapper.UserMapper;
import com.kaijie.service.IContractService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 家庭医生签约流程表 服务实现类
 * </p>
 *
 * @author kaijie
 * @since 2026-03-15
 */
@Service
@Transactional
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements IContractService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String applyContract(String username, ContractApplyDTO dto) {
        if (username == null || dto == null || dto.getDoctorId() == null) {
            throw new RuntimeException("参数错误");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || user.getId() == null) {
            throw new RuntimeException("用户不存在");
        }
        Long patientId = user.getId();

        // 校验是否已有待审核或已签约记录
        QueryWrapper<Contract> q = new QueryWrapper<>();
        q.eq("patient_id", patientId).in("contract_status", 0, 1);
        Contract exist = this.baseMapper.selectOne(q);
        if (exist != null) {
            throw new RuntimeException("您已有生效或处理中的签约，请勿重复申请");
        }

        Contract record = new Contract();
        record.setPatientId(patientId);
        record.setDoctorId(dto.getDoctorId());
        record.setApplyReason(dto.getApplyReason());
        record.setContractStatus((byte)0);
        record.setCreateTime(LocalDateTime.now());

        boolean ok = this.save(record);
        if (!ok) throw new RuntimeException("申请提交失败");
        return "申请已提交，等待医生审核";
    }

    @Override
    public String auditContract(String username, ContractAuditDTO dto) {
        if (username == null || dto == null || dto.getContractId() == null || dto.getAuditStatus() == null) {
            throw new RuntimeException("参数错误");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || user.getId() == null) {
            throw new RuntimeException("用户不存在");
        }
        Long doctorId = user.getId();

        Contract record = this.baseMapper.selectById(dto.getContractId());
        if (record == null) throw new RuntimeException("签约记录不存在");
        if (!doctorId.equals(record.getDoctorId())) throw new RuntimeException("无权审核该签约记录");
        if (record.getContractStatus() == null || record.getContractStatus() != 0) throw new RuntimeException("只有待审核的签约可以被处理");

        int status = dto.getAuditStatus();
        if (status == 1) {
            // 同意
            record.setContractStatus((byte)1);
            record.setStartDate(LocalDate.now());
            record.setEndDate(LocalDate.now().plusYears(1));
            record.setRejectReason(null);
        } else if (status == 2) {
            // 拒绝
            if (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty()) {
                throw new RuntimeException("拒绝时必须填写rejectReason");
            }
            record.setContractStatus((byte)2);
            record.setRejectReason(dto.getRejectReason());
            record.setStartDate(null);
            record.setEndDate(null);
        } else {
            throw new RuntimeException("未知的审核状态");
        }

        record.setUpdateTime(LocalDateTime.now());
        boolean ok = this.updateById(record);
        if (!ok) throw new RuntimeException("审核处理失败");
        return "审核处理完成";
    }

    @Override
    public List<Contract> getMyContracts(String username, Integer roleType) {
        if (username == null || roleType == null) return java.util.Collections.emptyList();

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || user.getId() == null) return java.util.Collections.emptyList();
        Long userId = user.getId();

        QueryWrapper<Contract> q = new QueryWrapper<>();
        if (roleType == 2) {
            q.eq("patient_id", userId);
        } else if (roleType == 1) {
            q.eq("doctor_id", userId);
        } else {
            // 非医生或居民角色返回空
            return java.util.Collections.emptyList();
        }
        q.orderByDesc("create_time");
        return this.baseMapper.selectList(q);
    }
}
