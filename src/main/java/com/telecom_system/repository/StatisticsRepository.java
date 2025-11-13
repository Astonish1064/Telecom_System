package com.telecom_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface StatisticsRepository extends JpaRepository<Object, Long> {
    
    // 系统统计信息
    @Query("SELECT " +
           "COUNT(u) as totalUsers, " +
           "SUM(u.balance) as totalBalance, " +
           "AVG(u.balance) as averageBalance, " +
           "COUNT(DISTINCT u.packgeId) as activePackages " +
           "FROM UserInfo u")
    Map<String, Object> getSystemStatistics();
    
    // 套餐使用统计
    @Query("SELECT p.id, p.duration, p.cost, COUNT(u) as userCount " +
           "FROM PackageInfo p LEFT JOIN UserInfo u ON p.id = u.packgeId " +
           "GROUP BY p.id, p.duration, p.cost " +
           "ORDER BY userCount DESC")
    List<Object[]> getPackageUsageStatistics();
    
    // 用户活跃度统计
    @Query("SELECT u.account, u.name, COUNT(li) as loginCount, " +
           "SUM(EXTRACT(EPOCH FROM (li.logoutTime - li.id.loginTime)) / 3600) as totalHours " +
           "FROM UserInfo u LEFT JOIN LoginInfo li ON u.account = li.id.accountId " +
           "WHERE li.logoutTime IS NOT NULL " +
           "GROUP BY u.account, u.name " +
           "ORDER BY totalHours DESC")
    List<Object[]> getUserActivityStatistics();
}