# Implementation Plan: Product Reviews & Ratings

Hệ thống cho phép khách hàng đánh giá sản phẩm sau khi mua hàng, upload ảnh thực tế và chỉnh sửa nội dung đánh giá.

## 1. Quy tắc Nghiệp vụ (Business Rules)
- **Xác thực mua hàng (Verified Purchase)**: Chỉ những khách hàng có đơn hàng trạng thái `DELIVERED` mới được đánh giá.
- **Ràng buộc 1-1**: Mỗi món đồ trong đơn hàng (`OrderItem`) chỉ được đánh giá duy nhất một lần.
- **Quyền chỉnh sửa**: Khách hàng có thể sửa lại nội dung và số sao đánh giá. Hệ thống sẽ tự động cập nhật lại điểm trung bình của sản phẩm.
- **Kiểm duyệt**: Admin có quyền ẩn các đánh giá không phù hợp hoặc phản hồi đánh giá.

## 2. Thiết kế Cơ sở dữ liệu (Database Design)

### Thực thể `ProductReview`
- `id`: PK.
- `user_id`: FK (ManyToOne).
- `product_id`: FK (ManyToOne).
- `order_item_id`: FK (OneToOne) - Đảm bảo mỗi món hàng chỉ đánh giá 1 lần.
- `rating`: Integer (1-5).
- `comment`: Text.
- `images`: JSON list (Lưu URL từ Cloudinary).
- `replyComment`: Text (Admin phản hồi).
- `isActive`: Boolean (Mặc định true).

### Cập nhật thực thể `Product`
- `averageRating`: Double (Cache điểm trung bình).
- `reviewCount`: Integer (Cache tổng số lượt đánh giá).

## 3. Hệ thống API

### Khách hàng (`/api/user/reviews`)
- `POST /`: Gửi đánh giá mới (kèm ảnh).
- `PUT /{id}`: Chỉnh sửa đánh giá.

### Công khai (`/api/public/reviews`)
- `GET /product/{productId}`: Lấy danh sách đánh giá của sản phẩm (Phân trang).

### Admin (`/api/admin/reviews`)
- `POST /{id}/reply`: Phản hồi khách hàng.
- `DELETE /{id}`: Xóa/Ẩn đánh giá.

## 4. Quy trình xử lý Logic
1. Nhận thiết kế từ Frontend (Rating + Comment + Images).
2. Validate điều kiện mua hàng.
3. Upload ảnh lên Cloudinary folder `reviews`.
4. Lưu `ProductReview`.
5. Tính toán lại `averageRating` của `Product` bằng cách lấy trung bình cộng tất cả đánh giá hiện có.
6. Trả về kết quả thành công.
