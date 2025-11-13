package com.telecom_system.service;

import com.telecom_system.entity.Admin;
import com.telecom_system.entity.User;
import com.telecom_system.repository.AdminRepository;
import com.telecom_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    
    public LoginService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }
    
    /**
     * 用户登录验证
     */
    public Optional<User> userLogin(String identifier, String password) {
        return userRepository.validateLogin(identifier, password);
    }
    
    /**
     * 管理员登录验证
     */
    public Optional<Admin> adminLogin(String identifier, String password) {
        return adminRepository.validateLogin(identifier, password);
    }
    
    /**
     * 检查用户是否存在
     */
    public boolean userExists(String identifier) {
        return userRepository.findUserForLogin(identifier).isPresent();
    }
    
    /**
     * 检查管理员是否存在
     */
    public boolean adminExists(String identifier) {
        try {
            Integer account = Integer.valueOf(identifier);
            return adminRepository.findById(account).isPresent();
        } catch (NumberFormatException e) {
            return adminRepository.findByName(identifier).isPresent();
        }
    }
}