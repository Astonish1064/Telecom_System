package com.telecom_system.service;

import com.telecom_system.entity.Admin;
import com.telecom_system.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminService {
    
    private final AdminRepository adminRepository;
    
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
    // 查找所有管理员
    public List<Admin> findAllAdmins() {
        return adminRepository.findAll();
    }
    
    // 根据Account创建新管理员
    public Admin createAdmin(Admin admin) {
        if (adminRepository.existsById(admin.getAccount())) {
            throw new RuntimeException("管理员ID已存在: " + admin.getAccount());
        }
        
        if (adminRepository.existsByName(admin.getName())) {
            throw new RuntimeException("管理员名已存在: " + admin.getName());
        }
        
        return adminRepository.save(admin);
    }
    
    // 根据Account更新管理员信息
    public Admin updateAdmin(Integer account, Admin admin) {
        return adminRepository.findById(account)
                .map(existingAdmin -> {
                    if (admin.getName() != null) {
                        existingAdmin.setName(admin.getName());
                    }
                    if (admin.getPassword() != null) {
                        existingAdmin.setPassword(admin.getPassword());
                    }
                    return adminRepository.save(existingAdmin);
                })
                .orElseThrow(() -> new RuntimeException("管理员不存在: " + account));
    }
    
    // 根据Account删除管理员
    public void deleteAdmin(Integer account) {
        if (!adminRepository.existsById(account)) {
            throw new RuntimeException("管理员不存在: " + account);
        }
        adminRepository.deleteById(account);
    }
    
    // 重置管理员密码
    public void resetPassword(Integer account) {
        adminRepository.findById(account)
                .ifPresent(admin -> {
                    admin.setPassword("default123"); // 重置为默认密码
                    adminRepository.save(admin);
                });
    }
}