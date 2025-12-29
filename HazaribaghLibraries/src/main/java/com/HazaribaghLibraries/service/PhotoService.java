package com.HazaribaghLibraries.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class PhotoService {

    private final Cloudinary cloudinary;

    // We inject the values from application.properties
    public PhotoService(@Value("${cloudinary.cloud-name}") String cloudName,
                        @Value("${cloudinary.api-key}") String apiKey,
                        @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String uploadImage(MultipartFile file) throws IOException {
        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        // Return the secure online URL (https://...)
        return (String) uploadResult.get("secure_url");
    }
}