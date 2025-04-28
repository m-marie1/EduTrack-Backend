package com.college.attendance.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {

    /**
     * Uploads a file to Cloudinary.
     *
     * @param file The file to upload.
     * @return The secure URL of the uploaded file.
     * @throws IOException If an error occurs during upload.
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * Uploads a file to Cloudinary with a specific public ID (filename).
     *
     * @param file     The file to upload.
     * @param publicId The desired public ID (filename) for the file in Cloudinary.
     * @return The secure URL of the uploaded file.
     * @throws IOException If an error occurs during upload.
     */
    String uploadFile(MultipartFile file, String publicId) throws IOException;
}