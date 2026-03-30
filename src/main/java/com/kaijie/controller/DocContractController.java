package com.kaijie.controller;

import com.kaijie.dto.ContractApplyDTO;
import com.kaijie.dto.ContractAuditDTO;
import com.kaijie.entity.Contract;
import com.kaijie.entity.User;
import com.kaijie.security.SecurityUtils;
import com.kaijie.service.IContractService;
import com.kaijie.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contract")
public class DocContractController {

    @Autowired
    private IContractService contractService;

    @Autowired
    private IUserService userService;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody ContractApplyDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthenticated");
        }
        try {
            String msg = contractService.applyContract(username, dto);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/audit")
    public ResponseEntity<?> audit(@RequestBody ContractAuditDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthenticated");
        }
        try {
            String msg = contractService.auditContract(username, dto);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthenticated");
        }

        // 获取 roleType
        User user = userService.getOne(new QueryWrapper<User>().eq("username", username));
        Integer roleType = null;
        if (user != null && user.getRoleType() != null) {
            roleType = (int) user.getRoleType();
        }

        List<Contract> list = contractService.getMyContracts(username, roleType);
        return ResponseEntity.ok(list);
    }
}

