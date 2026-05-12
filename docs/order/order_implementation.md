# Hướng dẫn triển khai Module Order (Đơn hàng)

Tài liệu này mô tả chi tiết cách triển khai module Order trong hệ thống Sport Pro Backend, tuân thủ các quy tắc lập trình Senior (`rule_be.md`).

---

## 1. Quy trình nghiệp vụ (Business Flow)

Quy trình đặt hàng được thực hiện theo các bước sau:

1.  **Lấy thông tin người dùng:** Xác thực người dùng thông qua `Authentication`.
2.  **Kiểm tra giỏ hàng:** Truy xuất giỏ hàng hiện tại của người dùng. Nếu giỏ hàng trống, trả về lỗi `400 Bad Request`.
3.  **Khởi tạo đơn hàng:** Tạo đối tượng `Order` với trạng thái mặc định là `PENDING`.
4.  **Xử lý từng mục trong giỏ hàng:**
    *   Kiểm tra tồn kho (`stock_quantity`) của từng biến thể sản phẩm.
    *   Trừ số lượng tồn kho.
    *   Tính toán giá bán (ưu tiên `sale_price` nếu có).
    *   Tạo các đối tượng `OrderItem`.
5.  **Cập nhật tổng tiền:** Tính toán `totalAmount` dựa trên danh sách `OrderItem`.
6.  **Xóa giỏ hàng:** Sau khi đặt hàng thành công, làm trống giỏ hàng của người dùng.
7.  **Trả về kết quả:** Map dữ liệu sang `OrderResponse` và trả về cho client.

---

## 2. Cấu trúc dữ liệu (Database Schema)

### Entity: Order
*   `id`: Khóa chính (Auto Increment).
*   `user_id`: Khóa ngoại liên kết với bảng `users`.
*   `shipping_address`: Địa chỉ giao hàng (Text).
*   `phone_number`: Số điện thoại nhận hàng.
*   `total_amount`: Tổng giá trị đơn hàng (Decimal).
*   `status`: Trạng thái đơn hàng (`PENDING`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`).
*   `payment_method`: Phương thức thanh toán (`CASH`, `BANK_TRANSFER`, `VNPAY`).

### Entity: OrderItem
*   `id`: Khóa chính.
*   `order_id`: Khóa ngoại liên kết với bảng `orders`.
*   `product_variant_id`: Khóa ngoại liên kết với bảng `product_variants`.
*   `quantity`: Số lượng mua.
*   `price`: Giá tại thời điểm mua.

---

## 3. Tối ưu hiệu năng (Performance Optimization)

Để giải quyết vấn đề **N+1 Query** khi lấy danh sách đơn hàng hoặc chi tiết đơn hàng, chúng tôi sử dụng `@EntityGraph` trong `OrderRepository`.

```java
@EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product"})
Page<Order> findByUserId(Long userId, Pageable pageable);
```

Cấu hình này đảm bảo rằng Hibernate sẽ thực hiện `LEFT JOIN` để lấy toàn bộ thông tin về Item, Biến thể và Sản phẩm chỉ trong **01 câu lệnh SQL duy nhất**.

---

## 4. Quản lý lỗi (Error Handling)

Tất cả các thông báo lỗi được quản lý tập trung tại `ApiExceptionConstant` để đảm bảo tính nhất quán và dễ bảo trì:

*   `USER_NOT_FOUND`: Người dùng không tồn tại.
*   `CART_EMPTY`: Giỏ hàng trống.
*   `INSUFFICIENT_STOCK`: Không đủ hàng trong kho.
*   `ORDER_NOT_FOUND`: Đơn hàng không tồn tại hoặc không thuộc quyền sở hữu của người dùng.

---

## 5. Bảo mật (Security)

*   **Xác thực:** Toàn bộ API của Order yêu cầu quyền `ROLE_USER`.
*   **Phân quyền dữ liệu:** Khi lấy chi tiết đơn hàng, hệ thống luôn kiểm tra `userId` để đảm bảo người dùng chỉ có thể xem đơn hàng của chính mình.
*   **Validation:** Sử dụng `@Valid` và các annotation như `@NotBlank`, `@Pattern` (cho số điện thoại) để validate dữ liệu đầu vào tại Controller.

---

## 6. Hướng dẫn mở rộng

Trong tương lai, nếu cần tích hợp thanh toán Online (VNPAY, Momo), logic sẽ được thêm vào `OrderService` trước khi kết thúc transaction hoặc thông qua một luồng xử lý bất đồng bộ (`@Async`).
