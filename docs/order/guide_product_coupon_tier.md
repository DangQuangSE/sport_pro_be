# Hướng dẫn triển khai Hệ thống Coupon và Phân hạng Thành viên

Tài liệu này hướng dẫn chi tiết cách thiết kế lại hệ thống giá sản phẩm và triển khai tính năng Coupon (mã giảm giá) tích hợp phân hạng thành viên (Membership Tier) cho dự án `sport_pro_be`.

---

## 1. Mô tả bài toán

### Mục tiêu Business
- **Pricing Refactor**: Tách biệt rõ ràng giữa giá niêm yết (Original Price) và giá bán (Sale Price) để tạo hiệu ứng "mỏ neo" giá trên Frontend.
- **Coupon System**: Tạo các chương trình khuyến mãi linh hoạt, có thể áp dụng theo hạng thành viên để tăng lòng trung thành của khách hàng.
- **Membership Tier**: Phân cấp khách hàng dựa trên tổng chi tiêu để áp dụng các đặc quyền (mã giảm giá riêng).

### Phạm vi (Scope)
- **In-scope**:
    - Thay đổi schema bảng `product_variants`.
    - Thêm module `coupon` (Entity, Repository, Service).
    - Thêm logic phân hạng thành viên trong module `auth`.
    - Cập nhật luồng thanh toán trong `OrderService`.
- **Out-scope**:
    - Hệ thống tích điểm (Loyalty Points) - sẽ phát triển sau.

---

## 2. Thiết kế kỹ thuật (High-level)

### Kiến trúc Flow
1. **Sản phẩm**: `original_price` (hiển thị gạch ngang) và `sale_price` (giá thực thu).
2. **Thanh toán**: `CartItem` -> `Checkout` -> `Apply Coupon` -> `Validate Tier` -> `Calculate Discount` -> `Final Total`.
3. **Phân hạng**: `Order DELIVERED` -> `Update User Spending` -> `Re-calculate Tier`.

### Thành phần liên quan
- **Module Product**: `ProductVariant` entity.
- **Module Auth**: `User` entity, `UserTier` enum.
- **Module Coupon (New)**: `Coupon` entity, `CouponService`.
- **Module Order**: `OrderService` (nơi tích hợp logic áp mã).

---

## 3. Thư viện đề xuất
Không cần thêm thư viện mới. Sử dụng các thư viện hiện có:
- **Spring Data JPA**: Quản lý database.
- **Validation API**: Validate dữ liệu Coupon.

---

## 4. Thiết kế Cơ sở dữ liệu cho Cấu hình động

Thay vì hardcode vào `application.properties`, các mốc hạng thành viên sẽ được lưu vào database để Admin có thể quản lý trực tiếp qua UI.

### Entity: TierConfig
- `id`: Primary Key.
- `tier`: Enum (SILVER, GOLD, PLATINUM).
- `threshold`: BigDecimal (Số tiền tối thiểu để đạt hạng).
- `description`: Mô tả về hạng.

### Entity: User (Cập nhật)
- `totalSpending`: BigDecimal (Lưu tổng tiền đã mua hàng thành công).
- `tier`: UserTier (Lưu hạng hiện tại). 
> [!NOTE]
> Việc thêm trường `tier` trực tiếp vào User là CẦN THIẾT để tối ưu hiệu năng. Hệ thống sẽ không phải tính toán lại tổng tiền mỗi khi User áp mã giảm giá, chỉ cần check trường này.

---

## 5. Danh sách API cần triển khai cho Admin

### Quản lý Coupon
- `POST /api/v1/admin/coupons`: Tạo mã mới.
- `GET /api/v1/admin/coupons`: Danh sách mã (phân trang).
- `PUT /api/v1/admin/coupons/{id}`: Cập nhật mã.
- `DELETE /api/v1/admin/coupons/{id}`: Xóa/Vô hiệu hóa mã.

### Quản lý Cấu hình Hạng (Tier Config)
- `GET /api/v1/admin/tier-configs`: Lấy danh sách các mốc hạng hiện tại.
- `PUT /api/v1/admin/tier-configs/{id}`: Cập nhật mốc tiền cho một hạng.

---

## 6. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Refactor Pricing trong Product Module
1. Sửa `ProductVariant.java`: Đổi tên trường `price` thành `originalPrice`. Trường `salePrice` giữ nguyên nhưng đảm bảo nó là giá dùng để thanh toán.
2. Cập nhật `ProductVariantRequest.java` và `ProductVariantResponse.java` tương ứng.
3. Cập nhật `ProductVariantService.java` để map dữ liệu mới.

### Bước 2: Triển khai Membership Tier trong Auth Module
1. Tạo Enum `UserTier` (`BRONZE`, `SILVER`, `GOLD`, `PLATINUM`).
2. Sửa `User.java`: Thêm `BigDecimal totalSpending` và `UserTier tier`.
3. Tạo `TierService` để xử lý logic nâng hạng dựa trên `totalSpending`.

### Bước 3: Triển khai Module Coupon (New Module)
1. Tạo package `com.sport_pro_be.modules.coupon`.
2. Tạo `Coupon.java` Entity với các trường: `code`, `discountType` (PERCENT/FIXED), `discountValue`, `requiredTier`, `minOrderAmount`, `usageLimit`, `expiryDate`.
3. Tạo `CouponRepository` và `CouponService`.

### Bước 4: Tích hợp Coupon và Tier vào Order Module
1. Sửa `Order.java`: Thêm `discountAmount` và liên kết với `Coupon`.
2. Sửa `OrderRequest.java`: Thêm trường `couponCode`.
3. Cập nhật `OrderService.placeOrder`:
    - Kiểm tra nếu có `couponCode`, gọi `CouponService` để validate.
    - Tính toán số tiền giảm và trừ vào `totalAmount`.
4. Triển khai logic cập nhật hạng thành viên:
    - Tạo method `updateUserTier(User user)` trong `TierService`.
    - Logic: 
        1. Lấy danh sách `TierConfig` sắp xếp theo `threshold` giảm dần.
        2. Duyệt danh sách, nếu `user.totalSpending >= config.threshold` thì set `user.tier = config.tier` và break (chỉ lấy hạng cao nhất đạt được).
    - Gọi logic này trong `OrderService` ngay sau khi đơn hàng được xác nhận đã thanh toán thành công:
        ```java
        user.setTotalSpending(user.getTotalSpending().add(orderAmount));
        tierService.updateUserTier(user);
        userRepository.save(user);
        ```

---

## 6. Pseudo-code / Code Skeleton

### ProductVariant Entity
```java
public class ProductVariant {
    // ...
    private BigDecimal originalPrice; // Giá niêm yết (gạch ngang)
    private BigDecimal salePrice;     // Giá bán thực tế
}
```

### Coupon Entity
```java
public class Coupon {
    private String code;
    private DiscountType type; // PERCENTAGE hoặc FIXED
    private BigDecimal value;
    private UserTier requiredTier; // Hạng tối thiểu để dùng mã
    private BigDecimal minOrderAmount;
    private LocalDateTime expiryDate;
}
```

### OrderService.placeOrder Logic
```java
public OrderResponse placeOrder(Long userId, OrderRequest request) {
    // 1. Lấy thông tin giỏ hàng và User
    // 2. Tính subTotal từ salePrice của các sản phẩm
    // 3. Nếu request có couponCode:
    //    - Validate coupon (còn hạn, còn lượt dùng, hạng User đạt yêu cầu)
    //    - Tính discountAmount dựa trên type (PERCENT hoặc FIXED)
    // 4. totalAmount = subTotal - discountAmount
    // 5. Lưu Order và trừ tồn kho
}
```

---

## 7. Exception handling & validation

| Exception | HTTP Status | Message |
| :--- | :--- | :--- |
| `CouponExpiredException` | 400 Bad Request | "Mã giảm giá đã hết hạn" |
| `TierNotReachedException` | 403 Forbidden | "Bạn cần đạt hạng {tier} để sử dụng mã này" |
| `UsageLimitReachedException` | 400 Bad Request | "Mã giảm giá đã hết lượt sử dụng" |
| `MinAmountNotReachedException` | 400 Bad Request | "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã" |

---

## 8. Checklist tự test
- [ ] Áp mã giảm giá % thành công.
- [ ] Áp mã giảm giá số tiền cố định thành công.
- [ ] Thử áp mã đã hết hạn (phải báo lỗi).
- [ ] Thử áp mã của hạng GOLD cho User hạng SILVER (phải báo lỗi).
- [ ] Kiểm tra giá `totalAmount` sau khi áp mã có khớp không.

---

## 9. Checklist review trước khi commit
- [ ] Kiểm tra logic `totalSpending` có được cộng dồn chính xác không.
- [ ] Đảm bảo `originalPrice` luôn lớn hơn hoặc bằng `salePrice`.
- [ ] Kiểm tra lỗi N+1 khi truy vấn đơn hàng kèm thông tin Coupon.

---

## 10. Follow-up nâng cấp
- Tích hợp gửi email thông báo khi User được nâng hạng thành viên.
- Hỗ trợ Coupon cho từng danh mục sản phẩm cụ thể (Category-based coupons).
