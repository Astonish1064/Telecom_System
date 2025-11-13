package com.telecom_system.controller;

import com.telecom_system.entity.Package;
import com.telecom_system.service.PackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*")
public class PackageController {
    
    private final PackageService packageService;
    
    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }
    
    /**
     * 获取所有套餐
     */
    @GetMapping
    public ResponseEntity<List<Package>> getAllPackages() {
        return ResponseEntity.ok(packageService.findAllPackages());
    }
    
    /**
     * 根据ID获取套餐详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPackageById(@PathVariable Integer id) {
        Optional<Package> packageInfo = packageService.findPackageById(id);
        return packageInfo.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建新套餐
     */
    @PostMapping
    public ResponseEntity<Package> createPackage(@RequestBody Package packageInfo) {
        return ResponseEntity.ok(packageService.createPackage(packageInfo));
    }
    
    /**
     * 更新套餐信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Package> updatePackage(@PathVariable Integer id, 
                                                    @RequestBody Package packageInfo) {
        return ResponseEntity.ok(packageService.updatePackage(id, packageInfo));
    }
    
    /**
     * 删除套餐
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Integer id) {
        packageService.deletePackage(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "套餐删除成功"));
    }
    
    /**
     * 获取热门套餐（使用人数最多的）
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularPackages() {
        return ResponseEntity.ok(packageService.getPopularPackages());
    }
    
    /**
     * 根据价格范围查询套餐
     */
    @GetMapping("/search/by-price")
    public ResponseEntity<List<Package>> getPackagesByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(packageService.findPackagesByPriceRange(minPrice, maxPrice));
    }
}