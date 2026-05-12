package com.sport_pro_be.modules.upload.constant;

public class UploadMessageConstant {
    private UploadMessageConstant() {}

    public static final String INVALID_FILE_TYPE = "Invalid file type. Only JPG, PNG and WEBP are allowed";
    public static final String FILE_TOO_LARGE = "File size too large. Maximum allowed size is 5MB";
    public static final String UPLOAD_FAILED = "Failed to upload file to Cloudinary";
    public static final String UPLOAD_SUCCESS = "File uploaded successfully";
    public static final String DELETE_FAILED = "Failed to delete file from Cloudinary";
}
