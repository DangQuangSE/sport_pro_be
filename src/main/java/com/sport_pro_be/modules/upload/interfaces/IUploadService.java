package com.sport_pro_be.modules.upload.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {
    /**
     * Uploads a file to a specific folder.
     * @param file the file to upload
     * @param folder the folder name (e.g., "products", "users")
     * @return the secure URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Deletes a file from the storage.
     * @param imageUrl the full URL of the image to delete
     */
    void deleteFile(String imageUrl);

    /**
     * Validates the file (type and size).
     * @param file the file to validate
     */
    void validateFile(MultipartFile file);
}
