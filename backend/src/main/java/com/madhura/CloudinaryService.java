
package com.madhura;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret,
                        "secure", true
                )
        );
    }

    public String upload(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "Please select an image.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ApiException(400, "Image must be smaller than 5 MB.");
        }

        Set<String> allowedTypes = Set.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        );

        if (!allowedTypes.contains(file.getContentType())) {
            throw new ApiException(
                    400,
                    "Only JPG, PNG and WEBP images are allowed."
            );
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "madhura-sarees/products",
                        "resource_type", "image"
                )
        );

        return (String) result.get("secure_url");
    }
}
