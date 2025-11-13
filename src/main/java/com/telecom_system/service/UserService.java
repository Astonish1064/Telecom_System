package com.telecom_system.service;

import com.telecom_system.entity.User;
import com.telecom_system.repository.UserRepository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public UserService(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 查找所有用户
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 分页查询用户
     */
    public Page<User> findUsersByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findUserById(Integer account) {
        return userRepository.findById(account);
    }
    
    /**
     * 创建新用户
     */
    public User createUser(User User) {
        // 业务逻辑验证
        if (userRepository.existsById(User.getAccount())) {
            throw new RuntimeException("用户ID已存在: " + User.getAccount());
        }
        
        if (userRepository.findByName(User.getName()).isPresent()) {
            throw new RuntimeException("用户名已存在: " + User.getName());
        }
        
        // 设置默认值
        if (User.getBalance() == null) {
            User.setBalance(BigDecimal.ZERO);
        }
        
        return userRepository.save(User);
    }
    
    /**
     * 更新用户信息
     */
    public User updateUser(Integer account, User User) {
        return userRepository.findById(account)
                .map(existingUser -> {
                    // 只更新允许修改的字段
                    if (User.getName() != null) {
                        existingUser.setName(User.getName());
                    }
                    if (User.getPhone() != null) {
                        existingUser.setPhone(User.getPhone());
                    }
                    if (User.getRole() != null) {
                        existingUser.setRole(User.getRole());
                    }
                    if (User.getPackgeId() != null) {
                        existingUser.setPackgeId(User.getPackgeId());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Integer account) {
        if (!userRepository.existsById(account)) {
            throw new RuntimeException("用户不存在: " + account);
        }
        userRepository.deleteById(account);
    }
    
    /**
     * 用户充值
     */
    public User recharge(Integer account, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        
        return userRepository.findById(account)
                .map(user -> {
                    BigDecimal newBalance = user.getBalance().add(BigDecimal.valueOf(amount));
                    user.setBalance(newBalance);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
    /**
     * 用户消费/扣费
     */
    public User deductBalance(Integer account, Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("扣费金额必须大于0");
        }
        
        return userRepository.findById(account)
                .map(user -> {
                    BigDecimal newBalance = user.getBalance().subtract(BigDecimal.valueOf(amount));
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("余额不足，当前余额: " + user.getBalance());
                    }
                    user.setBalance(newBalance);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
    /**
     * 更改用户套餐
     */
    public User changePackage(Integer account, String packageId) {
        return userRepository.findById(account)
                .map(user -> {
                    user.setPackgeId(packageId);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
    }
    
     /**
     * 获取用户剩余时长信息 - 直接从视图查询
     */
    public Map<String, Object> getRemainingTime(Integer account) {
        // 首先验证用户是否存在
        User user = userRepository.findById(account)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + account));
        
        // 从视图查询剩余时长信息
        String sql = "SELECT * FROM user_remaining_time WHERE account = ?";
        try {
            Map<String, Object> viewData = jdbcTemplate.queryForMap(sql, account);
            
            // 合并用户基本信息和视图数据
            Map<String, Object> result = new HashMap<>();
            result.put("account", user.getAccount());
            result.put("name", user.getName());
            result.put("phone", user.getPhone());
            result.put("packageId", user.getPackgeId());
            result.put("balance", user.getBalance());
            
            // 从视图获取的数据
            result.put("totalDuration", viewData.get("total_duration"));
            result.put("usedDuration", viewData.get("used_duration"));
            result.put("remainingDuration", viewData.get("remaining_duration"));
            result.put("remainingHours", viewData.get("remaining_hours"));
            result.put("status", viewData.get("status"));
            result.put("packageCost", viewData.get("cost"));
            
            return result;
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("用户剩余时长信息不存在: " + account);
        }
    }

    
    /**
     * 条件搜索用户
     */
    public List<User> searchUsers(String name, String phone) {
        if (name != null && !name.trim().isEmpty()) {
            return userRepository.findByNameContaining(name);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            return userRepository.findByPhone(phone)
                    .map(List::of)
                    .orElse(List.of());
        }
        
        return userRepository.findAll();
    }
    
    
    /**
     * 统计用户数量
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        return stats;
    }
}