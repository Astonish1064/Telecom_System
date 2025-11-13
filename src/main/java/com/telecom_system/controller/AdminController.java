package com.telecom_system.controller;

import com.telecom_system.entity.Admin;
import com.telecom_system.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    /**
     * 获取所有管理员
     */
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.findAllAdmins());
    }
    
    /**
     * 创建管理员
     */
    @PostMapping
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin adminInfo) {
        return ResponseEntity.ok(adminService.createAdmin(adminInfo));
    }
    
    /**
     * 更新管理员信息
     */
    @PutMapping("/{account}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable Integer account, 
                                                @RequestBody Admin adminInfo) {
        return ResponseEntity.ok(adminService.updateAdmin(account, adminInfo));
    }
    
    /**
     * 删除管理员
     */
    @DeleteMapping("/{account}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Integer account) {
        adminService.deleteAdmin(account);
        return ResponseEntity.ok(Map.of("success", true, "message", "管理员删除成功"));
    }
    
    /**
     * 重置管理员密码
     */
    @PostMapping("/{account}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Integer account) {
        adminService.resetPassword(account);
        return ResponseEntity.ok(Map.of("success", true, "message", "密码重置成功"));
    }
}