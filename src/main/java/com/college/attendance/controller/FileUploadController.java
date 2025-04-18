package com.college.attendance.controller;

import com.college.attendance.model.FileInfo;
import com.college.attendance.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileInfo>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || 
                !(contentType.startsWith("image/") || 
                contentType.equals("application/pdf") || 
                contentType.equals("application/msword") || 
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
                
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only images, PDFs, and Office documents are allowed"));
            }
            
            // Store the file
            FileInfo fileInfo = fileStorageService.storeFile(file);
            
            return ResponseEntity.ok(
                ApiResponse.success("File uploaded successfully", fileInfo)
            );
            
        } catch (IOException e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
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
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || 
                !(contentType.startsWith("image/") || 
                contentType.equals("application/pdf") || 
                contentType.equals("application/msword") || 
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
                
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only images, PDFs, and Office documents are allowed"));
            }
            
            // Store the file based on type
            FileInfo fileInfo;
            if ("professor-id".equals(fileType)) {
                fileInfo = fileStorageService.storeProfessorIdImage(file);
            } else {
                fileInfo = fileStorageService.storeFile(file);
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("File uploaded successfully", fileInfo)
            );
            
        } catch (IOException e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
} 