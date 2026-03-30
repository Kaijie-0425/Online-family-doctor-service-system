package com.kaijie.controller;

import com.kaijie.entity.PatientProfile;
import com.kaijie.security.SecurityUtils;
import com.kaijie.service.IPatientProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EhrPatientProfileController {

    @Autowired
    private IPatientProfileService patientProfileService;

    @GetMapping("/api/ehr/profile/my")
    public ResponseEntity<?> getMyProfile() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthenticated");
        }

        PatientProfile profile = patientProfileService.getProfileByUsername(username);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/api/ehr/profile/my")
    public ResponseEntity<?> saveOrUpdateMyProfile(@RequestBody PatientProfile profile) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthenticated");
        }

        boolean ok = patientProfileService.saveOrUpdateProfile(username, profile);
        if (ok) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to save profile");
        }
    }
}

