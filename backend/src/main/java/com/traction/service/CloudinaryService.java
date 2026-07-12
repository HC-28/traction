package com.traction.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Delegates image upload to Cloudinary and returns the resulting secure URL.
 * Isolating Cloudinary in its own service keeps VehicleService focused on
 * business rules and makes mocking trivial in unit tests.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a file to Cloudinary under the "traction/vehicles" folder.
     *
     * @param file the multipart image file to upload
     * @return the HTTPS-secure URL of the uploaded image
     */
    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "traction/vehicles")
        );
        return (String) result.get("secure_url");
    }
}
