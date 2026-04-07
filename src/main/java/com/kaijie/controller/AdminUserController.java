package com.kaijie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaijie.entity.User;
import com.kaijie.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端用户管理控制器（供前端 Admin 使用）
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminUserController(IUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 根据 ID 获取用户详情（返回前脱敏 password 字段）
     * GET /api/admin/user/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 脱敏密码，确保不返回密码哈希
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * 分页查询用户
     * GET /api/admin/user/page
     */
    @GetMapping("/page")
    public Page<User> page(@RequestParam(value = "current", defaultValue = "1") long current,
                           @RequestParam(value = "size", defaultValue = "10") long size,
                           @RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "roleType", required = false) Byte roleType) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username.trim());
        }
        if (roleType != null) {
            wrapper.eq(User::getRoleType, roleType);
        }
        return userService.page(new Page<>(current, size), wrapper);
    }

    /**
     * 新增用户（密码需加密）
     * POST /api/admin/user
     */
    @PostMapping
    public boolean create(@RequestBody User user) {
        if (user == null) {
            return false;
        }
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userService.save(user);
    }

    /**
     * 修改用户（若未提供密码或密码为空则保留原密码）
     * PUT /api/admin/user
     */
    @PutMapping
    public boolean update(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        if (!StringUtils.hasText(user.getPassword())) {
            // 保留原密码
            User exist = userService.getById(user.getId());
            if (exist != null) {
                user.setPassword(exist.getPassword());
            }
        } else {
            // 对新密码进行加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userService.updateById(user);
    }

    /**
     * 删除用户
     * DELETE /api/admin/user/{id}
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        if (id == null) {
            return false;
        }
        return userService.removeById(id);
    }
}
