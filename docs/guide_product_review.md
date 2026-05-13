# Hướng dẫn triển khai: Hệ thống Đánh giá & Phản hồi (Product Reviews & Ratings)

Tài liệu này hướng dẫn bạn tự tay triển khai tính năng đánh giá sản phẩm, đảm bảo tuân thủ kiến trúc Modular Monolith và các tiêu chuẩn của dự án.

---

## 1. Mô tả bài toán
- **Mục tiêu business**: Xây dựng lòng tin cho khách hàng mới thông qua các đánh giá thực tế (rating, comment, hình ảnh) từ những người đã mua hàng.
- **Phạm vi (Scope)**:
    - **In-scope**: Đánh giá sản phẩm đã mua, sửa đánh giá, upload ảnh, tính điểm trung bình, Admin phản hồi/kiểm duyệt.
    - **Out-scope**: Hệ thống vote hữu ích (helpful), lọc đánh giá theo từ khóa phức tạp.

## 2. Thiết kế kỹ thuật (High-level)
- **Kiến trúc**: Tạo module mới `review`.
- **Thành phần**:
    - `ProductReview`: Thực thể chính.
    - `ReviewService`: Xử lý logic nghiệp vụ và tính toán điểm trung bình.
    - `ReviewRepository`: Truy vấn đánh giá theo sản phẩm.
    - `UserReviewController` & `AdminReviewController`: Các endpoint API.

## 3. Thư viện đề xuất
- **Hibernate Envers (Tùy chọn)**: Nếu muốn lưu lịch sử các lần sửa đánh giá của khách hàng.
- **Jakarta Validation**: Đã có sẵn trong dự án, dùng để validate rating (1-5) và comment không trống.

## 4. Cấu hình cần thêm
- **`application.properties`**:
    ```properties
    # Thư mục lưu trữ ảnh review trên Cloudinary
    cloudinary.folder.reviews=reviews
    ```

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Cập nhật các Entity hiện có
- **File**: `Product.java`
    - Thêm trường `averageRating` (Double) và `reviewCount` (Integer).
- **File**: `OrderItem.java`
    - Thêm quan hệ `@OneToOne` với `ProductReview`.

### Bước 2: Khởi tạo Module `review`
- Tạo package `com.sport_pro_be.modules.review` với các sub-package: `domain`, `repository`, `service`, `dto`, `controller`, `constant`.

### Bước 3: Định nghĩa Thực thể `ProductReview`
- Các trường cần thiết: `id`, `user`, `product`, `orderItem`, `rating`, `comment`, `images` (List<String>), `replyComment`, `isActive`.
- Kế thừa `AbstractAuditingEntity`.

### Bước 4: Tạo DTOs và Constants
- `ReviewRequest`: Chứa rating, comment, orderItemId.
- `ReviewResponse`: Chứa thông tin hiển thị (tên user, avatar, nội dung, ảnh).
- `ReviewMessageConstant`: Chứa các thông báo lỗi/thành công.

### Bước 5: Triển khai Repository
- Viết phương thức tìm kiếm đánh giá theo `productId` có phân trang và chỉ lấy các bản ghi `isActive = true`.

### Bước 6: Triển khai Service (`ReviewService`)
- **Hàm `createReview`**: 
    - Kiểm tra đơn hàng có `STATUS = DELIVERED`.
    - Kiểm tra món hàng đã được đánh giá chưa.
    - Upload ảnh qua `IUploadService`.
    - Lưu thực thể và gọi hàm cập nhật `averageRating` của sản phẩm.
- **Hàm `updateReview`**:
    - Kiểm tra quyền sở hữu (chỉ chính chủ mới được sửa).
    - Cập nhật nội dung và tính toán lại điểm trung bình.
- **Hàm `updateProductRatingSummary`**: Tính toán lại điểm trung bình bằng cách lấy trung bình cộng tất cả rating của sản phẩm đó.

### Bước 7: Viết Controller
- Tách biệt `UserReviewController` (cho khách) và `AdminReviewController` (kiểm duyệt).

---

## 6. Code Skeleton (Khung mã nguồn)

### ReviewService.java
```java
public class ReviewService implements IReviewService {
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request, List<MultipartFile> images) {
        // 1. Validate OrderItem & User ownership
        // 2. Check OrderStatus == DELIVERED
        // 3. Check if already reviewed
        // 4. Upload images
        // 5. Save Review
        // 6. updateProductRatingSummary(product)
        return null;
    }

    private void updateProductRatingSummary(Product product) {
        // Get all active reviews of product
        // Calculate average
        // product.setAverageRating(avg);
        // product.setReviewCount(count);
        // productRepository.save(product);
    }
}
```

---

## 7. Exception handling & validation
- `400 Bad Request`: Khi đơn hàng chưa giao hoặc đã đánh giá rồi.
- `404 Not Found`: Khi không tìm thấy sản phẩm hoặc đánh giá.
- `403 Forbidden`: Khi khách hàng sửa đánh giá của người khác.

---

## 8. Checklist tự test
- [ ] Gửi đánh giá cho sản phẩm chưa mua (Mong đợi: Lỗi).
- [ ] Gửi đánh giá cho sản phẩm đang giao (Mong đợi: Lỗi).
- [ ] Gửi đánh giá hợp lệ kèm 2-3 ảnh.
- [ ] Chỉnh sửa đánh giá và kiểm tra điểm trung bình trên trang sản phẩm có thay đổi không.

## 9. Checklist review trước khi commit
- [ ] Check SQL log xem có bị N+1 khi lấy danh sách đánh giá không.
- [ ] Kiểm tra ảnh đã được upload đúng folder `reviews` trên Cloudinary chưa.

## 10. Follow-up nâng cấp
- Thêm tính năng "Đánh giá ẩn danh".
- Tự động tặng điểm thưởng (Membership points) sau khi khách hàng đánh giá thành công.
