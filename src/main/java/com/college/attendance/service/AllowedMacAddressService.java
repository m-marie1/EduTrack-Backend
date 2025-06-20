package com.college.attendance.service;

import com.college.attendance.model.AllowedMacAddress;
import com.college.attendance.repository.AllowedMacAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllowedMacAddressService {

    private final AllowedMacAddressRepository repository;

    public List<String> getAllMacAddresses() {
        return repository.findAll()
                .stream()
                .map(AllowedMacAddress::getMacAddress)
                .collect(Collectors.toList());
    }

    public String addMacAddress(String macAddress) {
        String normalized = normalize(macAddress);
        if (!repository.existsByMacAddressIgnoreCase(normalized)) {
            AllowedMacAddress mac = AllowedMacAddress.builder()
                    .macAddress(normalized)
                    .build();
            repository.save(mac);
        }
        return normalized;
    }

    public void removeMacAddress(String macAddress) {
        repository.deleteByMacAddressIgnoreCase(normalize(macAddress));
    }

    public void removeAll() {
        repository.deleteAll();
    }

    private String normalize(String mac) {
        // Remove common separators and convert to uppercase for consistency
        return mac.trim().replaceAll("[:-]", "").toUpperCase();
    }
} 