package com.college.attendance.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.college.attendance.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        // Generate a unique public ID within an 'uploads' folder structure
        // This helps organize files in Cloudinary and prevents overwriting files with the same original name.
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        // Sanitize filename slightly (replace spaces, etc.) - more robust sanitization might be needed
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String publicId = "uploads/" + UUID.randomUUID().toString() + "_" + sanitizedFilename;
        return uploadFileInternal(file, publicId);
    }

    @Override
    public String uploadFile(MultipartFile file, String publicId) throws IOException {
        // Ensure the publicId starts with the uploads/ prefix for consistency
        String effectivePublicId = publicId.startsWith("uploads/") ? publicId : "uploads/" + publicId;
        return uploadFileInternal(file, effectivePublicId);
    }

    private String uploadFileInternal(MultipartFile file, String publicId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File to upload cannot be null or empty");
        }

        log.info("Uploading file '{}' to Cloudinary with public ID '{}'", file.getOriginalFilename(), publicId);

        try {
            // Upload the file bytes
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true, // Allow overwriting if the same public_id is used (UUID makes this unlikely for the first method)
                    "resource_type", "auto" // Automatically detect resource type (image, video, raw file like PDF/ZIP)
            ));

            // Get the secure URL (HTTPS) of the uploaded file
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                log.error("Cloudinary upload result did not contain a secure_url. Result: {}", uploadResult);
                throw new IOException("Failed to get secure URL from Cloudinary upload result.");
            }

            log.info("File uploaded successfully. Secure URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload file '{}' to Cloudinary: {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new IOException("Failed to upload file to Cloudinary. Reason: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch potential runtime exceptions from Cloudinary SDK
            log.error("Unexpected error during Cloudinary upload for file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new IOException("Unexpected error uploading file to Cloudinary. Reason: " + e.getMessage(), e);
        }
    }
}