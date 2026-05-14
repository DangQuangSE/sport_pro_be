# Hướng dẫn triển khai: Quản lý Hoàn trả (Returns & Refunds)

## 1. Mô tả bài toán
- **Mục tiêu business**: Cung cấp quy trình chuyên nghiệp để xử lý yêu cầu trả hàng của khách hàng, kiểm soát chất lượng hàng trả về và thực hiện hoàn tiền, giúp tăng uy tín cho thương hiệu.
- **Phạm vi**:
    - Khách hàng tạo yêu cầu trả hàng (cho toàn bộ đơn hoặc từng món).
    - Admin duyệt/từ chối yêu cầu.
    - Cập nhật kho khi nhận hàng trả về.
    - Ghi nhận trạng thái hoàn tiền.

## 2. Thiết kế kỹ thuật (High-level)
- **Module**: `order` (Mở rộng)
- **Thành phần**:
    - `ReturnRequest`: Entity lưu thông tin yêu cầu (lý do, hình ảnh minh chứng, trạng thái).
    - `ReturnStatus` (Enum): PENDING, APPROVED, REJECTED, RECEIVED, REFUNDED, COMPLETED.
    - `RefundService`: Xử lý logic hoàn tiền (kết nối với API thanh toán nếu có).
    - `OrderService`: Cập nhật trạng thái đơn hàng sang `RETURNED`.

## 3. Thư viện đề xuất
- **Cloudinary**: Để khách hàng upload ảnh minh chứng sản phẩm lỗi (Đã có sẵn trong dự án).

## 4. Cấu hình cần thêm
- `ReturnMessageConstant.java`: Các thông báo cho quy trình đổi trả.
- Enum `ReturnReason`: WRONG_SIZE, DAMAGED, NOT_AS_DESCRIBED, CHANGE_MIND.

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Tạo Entity `ReturnRequest`
- Folder: `modules/order/domain`
- Các trường: `id`, `orderId`, `userId`, `reason`, `description`, `evidenceImages` (List String), `status`, `refundAmount`, `adminNote`.

### Bước 2: Tạo DTOs & Constant
- Folder: `modules/order/dto/request`, `modules/order/dto/response`, `modules/order/constant`.

### Bước 3: Triển khai Service xử lý logic
- `createReturnRequest`: Kiểm tra đơn hàng có ở trạng thái `DELIVERED` không? Có quá hạn trả hàng (VD: 7 ngày) không?
- `processReturnRequest` (Admin): Duyệt hoặc từ chối.
- `receiveReturnItem`: Khi nhận được hàng, cập nhật lại `stock_quantity` trong `ProductVariant`.

### Bước 4: Tích hợp logic Hoàn tiền (Refund)
- Nếu thanh toán qua PG (Stripe/VNPAY), gọi API hoàn tiền của họ.
- Nếu thanh toán COD/Chuyển khoản, ghi nhận thông tin tài khoản để Admin chuyển tay(hiện tại hệ thống chỉ có tài khoản cá nhân của admin nên phải để admin tự chuyển và đổi trạng thái đơn hàng thủ công).

### Bước 5: Tạo Controller
- `POST /api/v1/returns`: Khách hàng tạo yêu cầu.
- `GET /api/v1/admin/returns`: Admin xem danh sách yêu cầu.
- `PATCH /api/v1/admin/returns/{id}/status`: Admin cập nhật trạng thái(xem lại api cập nhật trạng thái đơn hàng đã có sẵn, hãy xem xét có cần tạo 1 api mới không).

## 6. Pseudo-code / Code skeleton

```java
public class ReturnRequest extends AbstractAuditingEntity {
    private Order order;
    private List<OrderItem> items; // Những món nào được trả
    private String reason;
    private ReturnStatus status;
    private BigDecimal refundAmount;
}

@Transactional
public void receiveItem(Long requestId) {
    // 1. Update status -> RECEIVED
    // 2. Loop list items in request
    // 3. productVariant.setStockQuantity(productVariant.getStockQuantity() + item.getQuantity())
}
```

## 7. Exception handling & validation
- `ORDER_NOT_DELIVERED`: Không thể trả hàng khi chưa nhận được hàng.
- `RETURN_PERIOD_EXPIRED`: Quá hạn trả hàng.
- `INVALID_REFUND_AMOUNT`: Số tiền hoàn trả không được lớn hơn số tiền đã thanh toán.

## 8. Checklist tự test
- [ ] Tạo yêu cầu trả hàng cho 1 món trong đơn hàng có 3 món.
- [ ] Admin duyệt yêu cầu, kiểm tra status của `ReturnRequest`.
- [ ] Xác nhận đã nhận hàng, kiểm tra `stock_quantity` của sản phẩm đó có tăng lên không.
- [ ] Thử tạo yêu cầu trả hàng cho đơn hàng mới ở trạng thái `PENDING` (Phải báo lỗi).

## 9. Checklist review trước khi commit
- [ ] Kiểm tra tính toàn vẹn dữ liệu: Nếu đơn hàng đã hoàn trả toàn bộ, trạng thái Order phải là `RETURNED`.
- [ ] Đảm bảo chỉ User sở hữu đơn hàng mới được tạo yêu cầu trả hàng.

## 10. Follow-up nâng cấp
- Tích hợp bên vận chuyển để lấy hàng tại nhà (Pickup service).
- Hệ thống chat/ticket để trao đổi thêm về lý do trả hàng.
