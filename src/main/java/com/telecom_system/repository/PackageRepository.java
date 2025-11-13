package com.telecom_system.repository;

import com.telecom_system.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PackageRepository extends JpaRepository<Package, Integer> {
    
    // 根据价格范围查找套餐
    List<Package> findByCostBetween(Double minCost, Double maxCost);
    
    // 查找价格低于指定值的套餐
    List<Package> findByCostLessThanEqual(Double maxCost);
    
    // 根据时长查找套餐（模糊匹配）
    List<Package> findByDurationContaining(String durationKeyword);
    
    // 自定义查询：查找最受欢迎的套餐（使用人数最多的）
    @Query("SELECT p, COUNT(u) as userCount " +
           "FROM PackageInfo p LEFT JOIN UserInfo u ON p.id = u.packgeId " +
           "GROUP BY p.id " +
           "ORDER BY userCount DESC")
    List<Object[]> findPopularPackages();
    
}