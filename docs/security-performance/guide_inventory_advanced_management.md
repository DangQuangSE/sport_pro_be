# Hướng dẫn triển khai Quản lý Tồn kho Nâng cao (Advanced Inventory Management)

## 1. Mô tả bài toán
### Mục tiêu business
Đảm bảo hàng hóa luôn sẵn sàng để phục vụ khách hàng, theo dõi mọi biến động kho (Nhập/Xuất) và cảnh báo kịp thời khi hàng sắp hết để Admin chủ động nhập hàng.

### Phạm vi
- **In-scope**: 
  - Thêm trường `lowStockThreshold` vào biến thể sản phẩm.
  - API lấy danh sách các biến thể sắp hết hàng (Stock < Threshold).
  - Tự động ghi Log khi có biến động kho (Nhập hàng thủ công, Khách đặt hàng, Khách hủy đơn).
  - API xem lịch sử biến động kho (Stock In/Out logs).
- **Out-scope**: 
  - Quản lý nhà cung cấp (Supplier Management).
  - Tự động tạo đơn nhập hàng (Auto Purchase Order).

---

## 2. Thiết kế kỹ thuật
### Kiến trúc flow
- **Action** (Order Place/Cancel/Admin Update) -> **InventoryService** -> **Update ProductVariant stock** & **Create InventoryLog**.
- **Admin** -> **InventoryController** -> **IInventoryService** -> **InventoryLogRepository**.

### Thành phần liên quan
- **Module mới**: `modules/inventory`. Theo thiết kế Modular, nên tạo module `inventory` riêng để quản lý Log.
- **Entity**: `InventoryLog`
- **Controller**: `InventoryController`
- **Service**: `InventoryService`
- **Repository**: `InventoryLogRepository`
- **DTOs**: `InventoryLogResponse`, `StockAdjustmentRequest`, `LowStockResponse`.

---

## 3. Thư viện đề xuất
- Không cần thư viện ngoài mới.

---

## 4. Cấu hình cần thêm
### CSDL (Database)
- Cần thêm bảng `inventory_logs`.
- Cần thêm cột `low_stock_threshold` vào bảng `product_variants`.

---

## 5. Kế hoạch triển khai code tay (step-by-step)

### Bước 1: Cập nhật ProductVariant Entity
- Mở `com.sport_pro_be.modules.product.domain.ProductVariant.java`.
- Thêm trường `private Integer lowStockThreshold = 5;`.
- *Tiêu chí hoàn thành*: Entity có trường mới, DB được cập nhật (nếu dùng ddl-auto).

### Bước 2: Tạo Entity InventoryLog
- Tạo class `InventoryLog` kế thừa `AbstractAuditingEntity`.
- Các trường: `id`, `productVariantId`, `type` (Enum: IN, OUT, ADJUST), `quantity`, `balance` (tồn kho sau khi đổi), `reason`, `referenceId` (ví dụ Order ID).
- *Tiêu chí hoàn thành*: Entity đúng cấu trúc, quan hệ với ProductVariant rõ ràng.

### Bước 3: Triển khai InventoryService
- Tạo interface `IInventoryService` và implementation `InventoryService`.
- Phương thức quan trọng: `adjustStock(Long variantId, Integer amount, InventoryLogType type, String reason, String referenceId)`.
- **Quan trọng**: Phương thức này phải có `@Transactional`.
- *Tiêu chí hoàn thành*: Mỗi khi stock thay đổi, 1 bản ghi log được tạo ra.

### Bước 4: Tích hợp vào luồng Order
- Tìm đến `OrderService` (hoặc nơi xử lý thanh toán/đặt hàng).
- Gọi `inventoryService.adjustStock(...)` khi đơn hàng được xác nhận (Xuất kho).
- Gọi `inventoryService.adjustStock(...)` khi đơn hàng bị hủy (Nhập kho lại).
- *Tiêu chí hoàn thành*: Stock tự động trừ/cộng khi có đơn hàng.

### Bước 5: Tạo InventoryController cho Admin
- Endpoint `@GetMapping("/low-stock")`: Lấy các variant có `stockQuantity <= lowStockThreshold`.
- Endpoint `@GetMapping("/logs")`: Xem lịch sử biến động (có phân trang).
- *Tiêu chí hoàn thành*: Admin theo dõi được kho hàng minh bạch.

---

## 6. Pseudo-code / code skeleton

### InventoryLog Entity
```java
public class InventoryLog extends AbstractAuditingEntity {
    private Long productVariantId;
    @Enumerated(EnumType.STRING)
    private InventoryLogType type; // IN, OUT, ADJUST
    private Integer quantity;
    private Integer balance; // Tồn kho tại thời điểm đó
    private String reason;
}
```

### InventoryService Logic
```java
@Transactional
public void adjustStock(Long variantId, Integer delta, ...) {
    ProductVariant variant = variantRepo.findById(variantId)...;
    int newStock = variant.getStockQuantity() + delta;
    if (newStock < 0) throw new InsufficientStockException(...);
    
    variant.setStockQuantity(newStock);
    variantRepo.save(variant);
    
    InventoryLog log = InventoryLog.builder()
        .productVariantId(variantId)
        .quantity(delta)
        .balance(newStock)
        .type(delta > 0 ? IN : OUT)
        .build();
    logRepo.save(log);
}
```

---

## 7. Exception handling & validation
- `InsufficientStockException`: Khi khách đặt hàng vượt quá tồn kho.
- `404 Not Found`: Khi variantId không tồn tại.
- Message hằng số đặt tại `InventoryMessageConstant.java`.

---

## 8. Checklist tự test
- [ ] Stock In: Admin nhập thêm 10 sản phẩm, stock tăng 10, log ghi "IN".
- [ ] Stock Out: Khách mua 2 sản phẩm, stock giảm 2, log ghi "OUT".
- [ ] Low Stock: Giảm stock xuống dưới ngưỡng, kiểm tra API `/low-stock` có trả về không.
- [ ] Concurrent Update: Hai khách cùng mua sản phẩm cuối cùng (Cần xử lý Optimistic Locking trên `ProductVariant` bằng `@Version`).

---

## 9. Checklist review trước khi commit
- [ ] Đã thêm `@Version` vào `ProductVariant` để chống race condition chưa?
- [ ] Logic `@Transactional` đã bao quát cả việc update stock và ghi log chưa?
- [ ] Tên biến, package tuân thủ Modular Monolith chưa?

---

## 10. Follow-up nâng cấp
- Gửi Email cảnh báo cho Admin hàng ngày về các sản phẩm sắp hết kho.
- Thống kê biểu đồ nhập xuất kho theo tháng.
