package com.telecom_system.service;

import com.telecom_system.entity.Package;
import com.telecom_system.repository.PackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageService {
    
    private final PackageRepository packageRepository;
    
    public PackageService(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }
    
    public List<Package> findAllPackages() {
        return packageRepository.findAll();
    }
    
    public Optional<Package> findPackageById(Integer id) {
        return packageRepository.findById(id);
    }
    
    public Package createPackage(Package Package) {
        if (packageRepository.existsById(Package.getId())) {
            throw new RuntimeException("套餐ID已存在: " + Package.getId());
        }
        return packageRepository.save(Package);
    }
    
    public Package updatePackage(Integer id, Package Package) {
        return packageRepository.findById(id)
                .map(existingPackage -> {
                    if (Package.getDuration() != null) {
                        existingPackage.setDuration(Package.getDuration());
                    }
                    if (Package.getCost() != null) {
                        existingPackage.setCost(Package.getCost());
                    }
                    return packageRepository.save(existingPackage);
                })
                .orElseThrow(() -> new RuntimeException("套餐不存在: " + id));
    }
    
    public void deletePackage(Integer id) {
        if (!packageRepository.existsById(id)) {
            throw new RuntimeException("套餐不存在: " + id);
        }
        packageRepository.deleteById(id);
    }
    
    public List<Package> findPackagesByPriceRange(Double minPrice, Double maxPrice) {
        return packageRepository.findByCostBetween(minPrice, maxPrice);
    }
    
    public List<Map<String, Object>> getPopularPackages() {
        List<Object[]> results = packageRepository.findPopularPackages();
        return results.stream()
                .map(result -> {
                    Package Package = (Package) result[0];
                    Long userCount = (Long) result[1];
                    
                    return Map.of(
                        "package", Package,
                        "userCount", userCount
                    );
                })
                .collect(Collectors.toList());
    }
}