package com.college.attendance.service;

import com.college.attendance.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    /**
     * Standard method to store any file
     */
    public FileInfo storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "");
    }
    
    /**
     * Store professor ID images in a specific subfolder
     */
    public FileInfo storeProfessorIdImage(MultipartFile file) throws IOException {
        return storeFile(file, "professor-id/");
    }
    
    /**
     * Core file storage method with subfolder support
     */
    private FileInfo storeFile(MultipartFile file, String subFolder) throws IOException {
        // Create the full upload path with subfolder
        Path uploadPath = Paths.get(uploadDir, subFolder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate a unique file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Copy the file to the target location
        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Create and return file info with the API endpoint path instead of direct file path
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(originalFileName);
        
        // Set different URL pattern based on file type
        if (subFolder.equals("professor-id/")) {
            // For professor ID images - use API endpoint
            fileInfo.setFileUrl("/api/files/professor-id/" + uniqueFileName);
        } else {
            // For regular files - use API endpoint
            fileInfo.setFileUrl("/api/files/" + uniqueFileName);
        }
        
        fileInfo.setContentType(file.getContentType());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setUploadedAt(LocalDateTime.now());
        
        return fileInfo;
    }
} 