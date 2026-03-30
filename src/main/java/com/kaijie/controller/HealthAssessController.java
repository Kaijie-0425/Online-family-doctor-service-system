package com.kaijie.controller;

import com.kaijie.dto.SymptomAssessDTO;
import com.kaijie.dto.VitalSignRecordDTO;
import com.kaijie.security.SecurityUtils;
import com.kaijie.service.IHealthVitalSignsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
public class HealthAssessController {

    @Autowired
    private IHealthVitalSignsService healthService;

    @PostMapping("/assess")
    public ResponseEntity<?> assess(@RequestBody SymptomAssessDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        String res = healthService.assessSymptoms(username, dto);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/vitals")
    public ResponseEntity<?> record(@RequestBody VitalSignRecordDTO dto) {
        String username = SecurityUtils.getCurrentUsername();
        String res = healthService.recordVitalSigns(username, dto);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/vitals/trend")
    public ResponseEntity<?> trend(@RequestParam(defaultValue = "7") Integer days) {
        String username = SecurityUtils.getCurrentUsername();
        java.util.List<com.kaijie.entity.HealthVitalSigns> list = healthService.getMyVitalSignsTrend(username, days);
        return ResponseEntity.ok(list);
    }
}



