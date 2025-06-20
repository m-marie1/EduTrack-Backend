package com.college.attendance.controller;

import com.college.attendance.controller.ApiResponse; // Correct package for ApiResponse
import com.college.attendance.model.FileInfo;
import com.college.attendance.service.CloudinaryService; // Import CloudinaryService
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Add logger
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload") // Keep the base path consistent with API doc
@RequiredArgsConstructor
@Slf4j // Add logger
public class FileUploadController {

    private final CloudinaryService cloudinaryService; // Inject CloudinaryService

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileInfo>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
            }
            
            // Basic validation: ensure we have a detectable content-type.  
            // We no longer restrict to a hard-coded whitelist so that files such as PPT/PPTX, TXT, ZIP, etc. are accepted.  
            // If further security filtering is needed (e.g., to block EXE or DLL), add it here.
            String contentType = file.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unsupported or unknown file type"));
            }
            
            // Upload the file to Cloudinary
            String fileUrl = cloudinaryService.uploadFile(file);

            // Create FileInfo object with Cloudinary URL
            FileInfo fileInfo = new FileInfo(
                    file.getOriginalFilename(),
                    fileUrl,
                    file.getContentType(),
                    file.getSize(),
                    java.time.LocalDateTime.now() // Add uploadedAt timestamp
            );

            log.info("Authenticated file uploaded: {} -> {}", file.getOriginalFilename(), fileUrl);
            return ResponseEntity.ok(
                    ApiResponse.success("File uploaded successfully", fileInfo)
            );

        } catch (IOException e) {
            log.error("Failed to upload authenticated file: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
             log.warn("Illegal argument during authenticated file upload: {}", e.getMessage());
             return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/public")
    public ResponseEntity<ApiResponse<FileInfo>> uploadPublicFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
            }
            
            // Basic validation (see comments above). Allow any non-null content-type.
            String contentType = file.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unsupported or unknown file type"));
            }
            
            // Upload the file to Cloudinary
            // For public uploads like professor IDs, we might want a specific folder or naming convention,
            // but for now, let's use the default behavior which includes a UUID.
            // If a specific public ID is needed based on fileType, we could use the other uploadFile method.
            String fileUrl = cloudinaryService.uploadFile(file);

            // Create FileInfo object with Cloudinary URL
             FileInfo fileInfo = new FileInfo(
                    file.getOriginalFilename(),
                    fileUrl,
                    file.getContentType(),
                    file.getSize(),
                    java.time.LocalDateTime.now() // Add uploadedAt timestamp
            );

            log.info("Public file uploaded (type: {}): {} -> {}", fileType, file.getOriginalFilename(), fileUrl);
            return ResponseEntity.ok(
                    ApiResponse.success("File uploaded successfully", fileInfo)
            );

        } catch (IOException e) {
             log.error("Failed to upload public file (type: {}): {}", fileType, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
             log.warn("Illegal argument during public file upload (type: {}): {}", fileType, e.getMessage());
             return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}