package com.telecom_system.service;

import com.telecom_system.entity.LoginInfo;
import com.telecom_system.entity.User;
import com.telecom_system.repository.LoginInfoRepository;
import com.telecom_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class LoginInfoService {
    
    private final LoginInfoRepository loginInfoRepository;
    private final UserRepository userRepository;
    
    public LoginInfoService(LoginInfoRepository loginInfoRepository, UserRepository userRepository) {
        this.loginInfoRepository = loginInfoRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 记录用户登录
     */
    public LoginInfo recordLogin(Integer accountId) {
        
        // 创建登录记录
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId(new LoginInfo.LoginInfoPK(accountId, LocalDateTime.now()));
        // logout_time 为 null，表示用户在线
        
        return loginInfoRepository.save(loginInfo);
    }
    
    /**
     * 记录用户登出
     */
    public LoginInfo recordLogout(Integer accountId) {
        // 查找该用户未下线的会话
        List<LoginInfo> activeSessions = loginInfoRepository.findByIdAccountIdAndLogoutTimeIsNull(accountId);
        
        if (activeSessions.isEmpty()) {
            throw new RuntimeException("用户没有活跃的登录会话: " + accountId);
        }
        
        // 更新最后一个活跃会话的登出时间
        LoginInfo lastSession = activeSessions.get(activeSessions.size() - 1);
        lastSession.setLogoutTime(LocalDateTime.now());
        
        return loginInfoRepository.save(lastSession);
    }
    
    /**
     * 强制用户下线
     */
    public void forceLogout(Integer accountId) {
        List<LoginInfo> activeSessions = loginInfoRepository.findByIdAccountIdAndLogoutTimeIsNull(accountId);
        
        for (LoginInfo session : activeSessions) {
            session.setLogoutTime(LocalDateTime.now());
            loginInfoRepository.save(session);
        }
    }
    
    /**
     * 根据用户ID查找所有登录记录
     */
    @Transactional(readOnly = true)
    public List<LoginInfo> findByAccountId(Integer accountId) {
        return loginInfoRepository.findByIdAccountId(accountId);
    }
    
    /**
     * 查找时间范围内的登录记录
     */
    @Transactional(readOnly = true)
    public List<LoginInfo> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return loginInfoRepository.findByIdLoginTimeBetween(start, end);
    }
    
    /**
     * 查找当前在线用户
     */
    @Transactional(readOnly = true)
    public List<LoginInfo> findOnlineSessions() {
        return loginInfoRepository.findByLogoutTimeIsNull();
    }
    
    /**
     * 查找最近24小时的活跃会话
     */
    @Transactional(readOnly = true)
    public List<LoginInfo> findRecentActiveSessions() {
        LocalDateTime sinceTime = LocalDateTime.now().minusHours(24);
        return loginInfoRepository.findRecentLogins(sinceTime);
    }
    
    /**
     * 获取用户登录统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserLoginStatistics(Integer accountId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户信息
        User user = userRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + accountId));
        
        // 获取所有登录记录
        List<LoginInfo> loginRecords = loginInfoRepository.findByIdAccountId(accountId);
        
        // 计算统计信息
        long totalSessions = loginRecords.size();
        long completedSessions = loginRecords.stream()
                .filter(login -> login.getLogoutTime() != null)
                .count();
        
        long activeSessions = loginRecords.stream()
                .filter(login -> login.getLogoutTime() == null)
                .count();
        
        // 计算总在线时长（小时）
        double totalOnlineHours = loginRecords.stream()
                .filter(login -> login.getLogoutTime() != null)
                .mapToDouble(login -> {
                    Duration duration = Duration.between(login.getLoginTime(), login.getLogoutTime());
                    return duration.toMinutes() / 60.0; // 转换为小时
                })
                .sum();
        
        // 计算平均会话时长
        double averageSessionHours = completedSessions > 0 ? totalOnlineHours / completedSessions : 0;
        
        // 查找最后登录时间
        Optional<LocalDateTime> lastLoginTime = loginRecords.stream()
                .map(LoginInfo::getLoginTime)
                .max(LocalDateTime::compareTo);
        
        // 查找最长会话
        Optional<LoginInfo> longestSession = loginRecords.stream()
                .filter(login -> login.getLogoutTime() != null)
                .max((l1, l2) -> {
                    Duration d1 = Duration.between(l1.getLoginTime(), l1.getLogoutTime());
                    Duration d2 = Duration.between(l2.getLoginTime(), l2.getLogoutTime());
                    return Long.compare(d1.toMinutes(), d2.toMinutes());
                });
        
        stats.put("accountId", accountId);
        stats.put("userName", user.getName());
        stats.put("totalSessions", totalSessions);
        stats.put("completedSessions", completedSessions);
        stats.put("activeSessions", activeSessions);
        stats.put("totalOnlineHours", Math.round(totalOnlineHours * 100.0) / 100.0);
        stats.put("averageSessionHours", Math.round(averageSessionHours * 100.0) / 100.0);
        stats.put("lastLoginTime", lastLoginTime.orElse(null));
        
        if (longestSession.isPresent()) {
            LoginInfo session = longestSession.get();
            Duration duration = Duration.between(session.getLoginTime(), session.getLogoutTime());
            double hours = duration.toMinutes() / 60.0;
            stats.put("longestSessionHours", Math.round(hours * 100.0) / 100.0);
            stats.put("longestSessionDate", session.getLoginTime().toLocalDate());
        }
        
        return stats;
    }
    
    /**
     * 获取系统登录统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemLoginStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取时间范围内的登录记录
        List<LoginInfo> loginRecords = loginInfoRepository.findByIdLoginTimeBetween(startDate, endDate);
        
        // 计算统计信息
        long totalLogins = loginRecords.size();
        long uniqueUsers = loginRecords.stream()
                .map(login -> login.getAccountId())
                .distinct()
                .count();
        
        // 计算平均每日登录次数
        long daysBetween = Duration.between(startDate, endDate).toDays();
        double averageDailyLogins = daysBetween > 0 ? (double) totalLogins / daysBetween : totalLogins;
        
        // 查找最活跃的用户
        Map<Integer, Long> userLoginCounts = new HashMap<>();
        for (LoginInfo login : loginRecords) {
            userLoginCounts.merge(login.getAccountId(), 1L, Long::sum);
        }
        
        Optional<Map.Entry<Integer, Long>> mostActiveUser = userLoginCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("totalLogins", totalLogins);
        stats.put("uniqueUsers", uniqueUsers);
        stats.put("averageDailyLogins", Math.round(averageDailyLogins * 100.0) / 100.0);
        
        if (mostActiveUser.isPresent()) {
            Map.Entry<Integer, Long> entry = mostActiveUser.get();
            Optional<User> user = userRepository.findById(entry.getKey());
            stats.put("mostActiveUserId", entry.getKey());
            stats.put("mostActiveUserName", user.map(User::getName).orElse("未知用户"));
            stats.put("mostActiveUserLoginCount", entry.getValue());
        }
        
        return stats;
    }
    
    /**
     * 检查用户是否在线
     */
    @Transactional(readOnly = true)
    public boolean isUserOnline(Integer accountId) {
        List<LoginInfo> activeSessions = loginInfoRepository.findByIdAccountIdAndLogoutTimeIsNull(accountId);
        return !activeSessions.isEmpty();
    }
}