# Hướng dẫn chi tiết: Tích hợp Cloudinary & Kiến trúc Upload linh hoạt

## 1. Mô tả bài toán
- **Mục tiêu**: Thay thế việc lưu URL ảnh thủ công bằng hệ thống tự động tải lên Cloudinary. Đảm bảo tính trừu tượng (Abstraction) để dễ dàng chuyển đổi sang AWS S3 hoặc lưu local mà không làm thay đổi logic nghiệp vụ của các module khác.
- **Phạm vi**: 
    - Module `upload` dùng chung.
    - Cloudinary Integration.
    - Refactor Product Image module.

## 2. Kiến trúc & Thiết kế (Senior Perspective)
Hệ thống sử dụng **Strategy Pattern** và **Dependency Inversion**.
- **Module Upload**: Đóng vai trò là một "Service Provider". Nó cung cấp một Interface `IUploadService` mà các module khác (như Product, User, v.v.) sẽ phụ thuộc vào.
- **Tính năng xóa ảnh**: Một vấn đề phổ biến là "rác" trên Cloud khi bản ghi DB bị xóa. Hệ thống này tích hợp cơ chế tự động xóa ảnh trên Cloud khi Service xóa bản ghi tương ứng.

## 3. Thư viện & Cấu hình
### Dependency (pom.xml)
```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>2.0.0</version>
</dependency>
```
*Lưu ý: Dùng `cloudinary-http5` thay vì bản cũ để tận dụng Apache HTTP Client 5 hiệu năng cao.*

### Biến môi trường (.env)
```env
CLOUDINARY_CLOUD_NAME=xxx
CLOUDINARY_API_KEY=xxx
CLOUDINARY_API_SECRET=xxx
```

## 4. Chi tiết triển khai code tay

### Bước 1: Interface IUploadService
Đây là "bản hợp đồng" giữa module Upload và các module khác.
```java
public interface IUploadService {
    // Upload file lên một folder chỉ định, trả về secure URL
    String uploadFile(MultipartFile file, String folder);
    
    // Xóa file trên Cloud dựa vào URL
    void deleteFile(String imageUrl);
    
    // Validate file (size, extension) tập trung
    void validateFile(MultipartFile file);
}
```

### Bước 2: Cloudinary Configuration
Khởi tạo Bean `Cloudinary` để Spring Container quản lý.
```java
@Bean
public Cloudinary cloudinary() {
    return new Cloudinary(ObjectUtils.asMap(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret,
        "secure", true // Bắt buộc dùng HTTPS
    ));
}
```

### Bước 3: Logic xử lý trong CloudinaryUploadService
- **Upload**: Sử dụng `file.getBytes()` và truyền vào `uploader().upload()`.
- **Xử lý Public ID (Quan trọng)**: Để xóa ảnh trên Cloudinary, bạn cần `public_id`. Tuy nhiên, DB chỉ lưu URL.
    - **Giải pháp**: Viết hàm `extractPublicId(String imageUrl)`.
    - **Logic**: Cắt chuỗi từ sau `/upload/`, loại bỏ phần `version` (v123456/) và phần mở rộng (`.jpg`).
    - *Senior Tip*: Nên dùng Regex hoặc logic String ổn định để xử lý cả trường hợp ảnh nằm trong folder.

### Bước 4: Tích hợp vào ProductImageService
- Thay vì nhận URL từ Request, Service giờ nhận `MultipartFile`.
- Quy trình: `Validate file` -> `Upload lên Cloudinary` -> `Lấy URL trả về` -> `Lưu DB`.
- Trong hàm `deleteImage`, gọi `uploadService.deleteFile(url)` trước khi xóa bản ghi DB.

## 5. Xử lý Exception & Validation (Chi tiết)
- **Dung lượng**: Giới hạn 5MB. Trả về `400 Bad Request`.
- **Định dạng**: Chỉ cho phép `image/jpeg`, `image/png`, `image/webp`.
- **Lỗi Cloud**: Nếu Cloudinary API trả về lỗi (hết quota, sai key), bắt exception và ném ra `AppException` với status `500`.

## 6. Hướng dẫn Test với Postman
1. Chọn method `POST`.
2. URL: `http://localhost:8080/api/admin/products/{id}/images`.
3. Tab **Body** -> chọn **form-data**.
4. Key: `file` -> chuyển type từ `Text` sang `File` -> Chọn ảnh.
5. Key: `isThumbnail` (Boolean), `sortOrder` (Integer) -> Truyền dưới dạng text nếu cần.

## 7. Checklist Senior Review
- [ ] **Resource Management**: Đã đảm bảo không bị memory leak khi xử lý byte array của file lớn?
- [ ] **Security**: Key Cloudinary đã được để trong `.env` và không bị commit lên Git?
- [ ] **User Experience**: Nếu upload lỗi, message trả về có rõ ràng để User biết file quá lớn hay sai định dạng không?
- [ ] **Maintainability**: Interface `IUploadService` có đủ tổng quát để sau này thay bằng S3 không?

## 8. Nâng cấp tương lai (Follow-up)
- **Image Transformation**: Tự động resize ảnh về kích thước chuẩn (ví dụ 800x800) ngay khi upload để tiết kiệm băng thông.
- **Async Processing**: Sử dụng `@Async` cho việc xóa ảnh trên Cloud để tăng tốc độ phản hồi API cho người dùng (vì việc xóa không nhất thiết phải hoàn thành ngay lập tức).
