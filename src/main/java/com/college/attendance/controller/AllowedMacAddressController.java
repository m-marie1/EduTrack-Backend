package com.college.attendance.controller;

import com.college.attendance.service.AllowedMacAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AllowedMacAddressController {

    private final AllowedMacAddressService service;

    // Accessible by any authenticated user (students, professors, admins)
    @GetMapping("/allowed-mac-addresses")
    public ResponseEntity<ApiResponse<List<String>>> getAllowedMacAddresses() {
        List<String> macs = service.getAllMacAddresses();
        return ResponseEntity.ok(ApiResponse.success("Allowed MAC addresses retrieved", macs));
    }

    // Admin: add a MAC address
    @PostMapping("/admin/allowed-mac-addresses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> addAllowedMacAddress(@RequestBody Map<String, String> payload) {
        String mac = payload.get("macAddress");
        if (mac == null || mac.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("macAddress is required"));
        }
        String added = service.addMacAddress(mac);
        return ResponseEntity.ok(ApiResponse.success("MAC address added successfully", added));
    }

    // Admin: delete specific MAC address
    @DeleteMapping("/admin/allowed-mac-addresses/{macAddress}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAllowedMacAddress(@PathVariable String macAddress) {
        service.removeMacAddress(macAddress);
        return ResponseEntity.ok(ApiResponse.success("MAC address removed successfully"));
    }

    // Admin: delete all MAC addresses (open attendance from anywhere)
    @DeleteMapping("/admin/allowed-mac-addresses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> clearAllowedMacAddresses() {
        service.removeAll();
        return ResponseEntity.ok(ApiResponse.success("All allowed MAC addresses have been cleared"));
    }
} 