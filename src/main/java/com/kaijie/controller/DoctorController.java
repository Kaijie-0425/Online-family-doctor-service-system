package com.kaijie.controller;

import com.kaijie.service.IDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private IDoctorService doctorService;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listDoctors() {
        return ResponseEntity.ok(doctorService.listDoctors());
    }
}


