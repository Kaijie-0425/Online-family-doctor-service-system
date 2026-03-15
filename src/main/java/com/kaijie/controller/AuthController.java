package com.kaijie.controller;

import com.kaijie.entity.User;
import com.kaijie.security.JwtUtils;
import com.kaijie.service.IUserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username and password required");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("username", req.getUsername()));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        boolean matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        if (!matches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        Integer roleType = null;
        if (user.getRoleType() != null) roleType = (int) user.getRoleType();

        String token = JwtUtils.generateToken(user.getUsername(), roleType);

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("tokenType", "Bearer");

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null || req.roleType == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username, password or roleType is required");
        }

        // 检查用户名是否已存在
        User existing = userService.getOne(new QueryWrapper<User>().eq("username", req.getUsername()));
        if (existing != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("username already exists");
        }

        User newUser = new User();
        newUser.setUsername(req.getUsername());
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));
        newUser.setRealName(req.getRealName());
        if (req.getRoleType() != null) newUser.setRoleType(req.getRoleType().byteValue());
        // 可根据业务设置默认字段，如状态、头像等
        newUser.setStatus((byte)1);

        boolean saved = userService.save(newUser);
        if (!saved) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to create user");
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("username", newUser.getUsername());
        resp.put("id", newUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private Integer roleType; // 0-admin,1-doctor,2-user
    }
}
