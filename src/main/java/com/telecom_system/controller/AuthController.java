package com.telecom_system.controller;

import com.telecom_system.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final LoginService loginService;
    
    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/user/login")
    public ResponseEntity<?> userLogin(@RequestParam String identifier, 
                                      @RequestParam String password) {
        return loginService.userLogin(identifier, password)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "用户登录成功");
                    response.put("data", user);
                    response.put("userType", "USER");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "用户名或密码错误")
                ));
    }
    
    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestParam String identifier, 
                                       @RequestParam String password) {
        return loginService.adminLogin(identifier, password)
                .map(admin -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "管理员登录成功");
                    response.put("data", admin);
                    response.put("userType", "ADMIN");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "管理员账号或密码错误")
                ));
    }
    
   
    
    /**
     * 退出登录（记录登出时间）
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam Integer accountId) {
        // 调用服务记录登出时间
        return ResponseEntity.ok(Map.of("success", true, "message", "退出成功"));
    }
}