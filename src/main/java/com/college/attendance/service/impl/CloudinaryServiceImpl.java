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

        // Separate the base name from the extension so we don't include the extension twice.
        // For example, for "cover_3.pdf" we want:
        //   baseName = "cover_3" , extension = "pdf"
        // Cloudinary will automatically append the extension that corresponds to the uploaded resource
        // therefore we must NOT embed the extension inside the publicId, otherwise we end up with
        // URLs such as ".../cover_3.pdf.pdf" which break for certain resource-types (e.g., PDFs).

        String baseName;
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalFilename.substring(0, dotIndex);
        } else {
            baseName = originalFilename; // No extension found
        }

        // Sanitize the baseName (replace disallowed chars, keep dot out because it's removed)
        // Note: In Java string literals, backslash must be escaped (\\) so the dash is treated literally inside the regex
        String sanitizedBaseName = baseName.replaceAll("[^a-zA-Z0-9_\\-]", "_");

        // Build the publicId without the extension; Cloudinary will add it automatically in the secure_url
        String publicId = "uploads/" + UUID.randomUUID().toString() + "_" + sanitizedBaseName;
        return uploadFileInternal(file, publicId);
    }

    @Override
    public String uploadFile(MultipartFile file, String publicId) throws IOException {
        // Ensure the publicId does not include an extension to avoid double extensions in generated URLs
        String effectivePublicId = publicId.startsWith("uploads/") ? publicId : "uploads/" + publicId;
        int extIndex = effectivePublicId.lastIndexOf('.');
        if (extIndex > effectivePublicId.lastIndexOf('/')) {
            effectivePublicId = effectivePublicId.substring(0, extIndex);
        }
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