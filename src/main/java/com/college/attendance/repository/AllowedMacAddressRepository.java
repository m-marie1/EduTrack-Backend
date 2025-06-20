package com.college.attendance.repository;

import com.college.attendance.model.AllowedMacAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedMacAddressRepository extends JpaRepository<AllowedMacAddress, Long> {

    Optional<AllowedMacAddress> findByMacAddressIgnoreCase(String macAddress);

    void deleteByMacAddressIgnoreCase(String macAddress);

    boolean existsByMacAddressIgnoreCase(String macAddress);
} 