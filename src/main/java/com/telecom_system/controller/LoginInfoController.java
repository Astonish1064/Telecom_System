package com.telecom_system.controller;

import com.telecom_system.entity.LoginInfo;
import com.telecom_system.service.LoginInfoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/login-records")
@CrossOrigin(origins = "*")
public class LoginInfoController {
    
    private final LoginInfoService loginInfoService;
    
    public LoginInfoController(LoginInfoService loginInfoService) {
        this.loginInfoService = loginInfoService;
    }
    
    /**
     * 获取用户的所有登录记录
     */
    @GetMapping("/user/{accountId}")
    public ResponseEntity<List<LoginInfo>> getUserLoginRecords(@PathVariable Integer accountId) {
        return ResponseEntity.ok(loginInfoService.findByAccountId(accountId));
    }
    
    /**
     * 获取时间范围内的登录记录
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<LoginInfo>> getLoginRecordsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(loginInfoService.findByTimeRange(start, end));
    }
    
    /**
     * 获取当前在线用户
     */
    @GetMapping("/online")
    public ResponseEntity<List<LoginInfo>> getOnlineUsers() {
        return ResponseEntity.ok(loginInfoService.findOnlineSessions());
    }
    
    /**
     * 强制用户下线
     */
    @PostMapping("/{accountId}/force-logout")
    public ResponseEntity<?> forceLogout(@PathVariable Integer accountId) {
        loginInfoService.forceLogout(accountId);
        return ResponseEntity.ok(Map.of("success", true, "message", "用户已强制下线"));
    }
    
    /**
     * 获取用户统计信息（总在线时长等）
     */
    @GetMapping("/{accountId}/statistics")
    public ResponseEntity<Map<String, Object>> getUserLoginStatistics(@PathVariable Integer accountId) {
        return ResponseEntity.ok(loginInfoService.getUserLoginStatistics(accountId));
    }
}