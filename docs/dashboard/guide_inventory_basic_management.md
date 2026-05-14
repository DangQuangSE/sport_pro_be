# Hướng dẫn triển khai Quản lý Tồn kho Cơ bản (Basic Inventory Management)

> Tài liệu này tuân thủ `scope_AI.md`. AI không code trực tiếp — developer tự triển khai dựa trên hướng dẫn này.

---

## 1. Mô tả bài toán

### Mục tiêu business
- Đảm bảo tồn kho được trừ chính xác ngay khi khách đặt hàng.
- Hoàn kho tự động khi đơn bị hủy.
- Admin theo dõi được các sản phẩm sắp hết hàng qua API.
- Admin có thể nhập thêm hàng thủ công (restock) qua API.

### Phạm vi

**In-scope:**
- Thêm `lowStockThreshold` + `@Version` vào `ProductVariant`.
- Tách logic điều chỉnh kho (`adjustStock`) ra module `inventory` riêng biệt để dễ maintain.
- `OrderService` gọi `IInventoryService.adjustStock()` thay vì tự xử lý stock trực tiếp.
- API Admin: lấy danh sách variant sắp hết hàng (`/low-stock`).
- API Admin: nhập thêm hàng thủ công (`/restock/{variantId}`).

**Out-scope:**
- Không có `InventoryLog` — không ghi lịch sử biến động kho.
- Không có Supplier Management, Auto Purchase Order.
- Không có Push Notification / Email cảnh báo.

---

## 2. Thiết kế kỹ thuật

### Kiến trúc flow

```
[Khách đặt hàng]
  → OrderService.placeOrder()
      → IInventoryService.deductStock(variantId, quantity)
          → ProductVariantRepository: giảm stockQuantity, lưu

[Admin hủy đơn / khách hủy đơn]
  → OrderService.updateOrderStatus(CANCELLED)
      → IInventoryService.restoreStock(variantId, quantity)
          → ProductVariantRepository: tăng stockQuantity, lưu

[Admin xem hàng sắp hết]
  → GET /api/admin/inventory/low-stock
      → IInventoryService.getLowStockVariants()
          → ProductVariantRepository.findLowStockVariants()

[Admin nhập thêm hàng]
  → PUT /api/admin/inventory/restock/{variantId}
      → IInventoryService.restock(variantId, quantity)
          → ProductVariantRepository: tăng stockQuantity, lưu
```

### Thành phần liên quan

| Thành phần | File | Ghi chú |
|---|---|---|
| **Module mới** | `modules/inventory/` | Chứa toàn bộ logic kho |
| Interface | `interfaces/IInventoryService.java` | Contract của module |
| Service | `service/InventoryService.java` | Implement `IInventoryService` |
| Controller | `controller/InventoryController.java` | Admin-only endpoints |
| DTO Request | `dto/request/RestockRequest.java` | Body cho restock API |
| DTO Response | `dto/response/LowStockResponse.java` | Response cho low-stock API |
| Message Constant | `constant/InventoryMessageConstant.java` | Tất cả message strings |
| **Chỉnh sửa** | `product/domain/ProductVariant.java` | Thêm `lowStockThreshold`, `@Version` |
| **Chỉnh sửa** | `product/repository/ProductVariantRepository.java` | Thêm query `findLowStockVariants` |
| **Chỉnh sửa** | `order/service/OrderService.java` | Inject `IInventoryService`, bỏ logic stock cũ |

---

## 3. Thư viện đề xuất

Không cần dependency mới. Toàn bộ dùng:
- **Spring Data JPA**: `@Lock`, `@Modifying`, `@Query` — đã có sẵn trong `pom.xml`.
- **Jakarta Validation**: `@Min`, `@NotNull` — đã có sẵn.

---

## 4. Cấu hình cần thêm

### Database
Nếu project dùng `spring.jpa.hibernate.ddl-auto=update` hoặc `validate`: Hibernate sẽ tự thêm cột `low_stock_threshold` và `version` vào bảng `product_variants`.

Nếu dùng migration tool (Flyway/Liquibase), tạo thêm script:
```sql
ALTER TABLE product_variants ADD COLUMN low_stock_threshold INT NOT NULL DEFAULT 5;
ALTER TABLE product_variants ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

Không cần thêm key mới vào `application.properties`.

---

## 5. Kế hoạch triển khai code tay (step-by-step)

### Bước 1: Tạo cấu trúc thư mục module `inventory`

Tạo đầy đủ các package sau (tạo package trống trước):
```
src/main/java/com/sport_pro_be/modules/inventory/
  ├── constant/
  ├── controller/
  ├── dto/
  │   ├── request/
  │   └── response/
  ├── interfaces/
  └── service/
```

> *Tiêu chí hoàn thành*: Cấu trúc thư mục đúng theo Modular Monolith, không có `domain/` vì module này không sở hữu entity riêng — nó dùng nhờ `ProductVariant` của module `product`.

---

### Bước 2: Thêm `lowStockThreshold` và `@Version` vào `ProductVariant`

**File:** `modules/product/domain/ProductVariant.java`

Thêm 2 trường vào cuối class (trước dấu `}`):
- `@Version private Long version;` — bật Optimistic Locking, Hibernate tự quản lý.
- `@Column(name = "low_stock_threshold") private Integer lowStockThreshold = 5;` — ngưỡng cảnh báo hết hàng.

> *Tiêu chí hoàn thành*: Compile thành công. Khi restart, DB có 2 cột mới trên bảng `product_variants`.

---

### Bước 3: Thêm query `findLowStockVariants` vào `ProductVariantRepository`

**File:** `modules/product/repository/ProductVariantRepository.java`

Thêm method:
```
List<ProductVariant> findByStockQuantityLessThanEqualAndLowStockThresholdIsNotNull()
```
Hoặc dùng `@Query` JPQL:
```
@Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= v.lowStockThreshold")
List<ProductVariant> findLowStockVariants();
```

> *Tiêu chí hoàn thành*: Method không compile lỗi, trả đúng danh sách variant có `stockQuantity <= lowStockThreshold`.

---

### Bước 4: Tạo `InventoryMessageConstant`

**File:** `modules/inventory/constant/InventoryMessageConstant.java`

Tạo class với constructor private (utility class). Định nghĩa các hằng số:
- `VARIANT_NOT_FOUND` — khi không tìm thấy variant
- `INSUFFICIENT_STOCK` — khi tồn kho không đủ (dùng format String có `%s`)
- `RESTOCK_QUANTITY_POSITIVE` — validation cho restock request
- `STOCK_ADJUSTED` — thông báo thành công khi restock
- `LOW_STOCK_RETRIEVED` — thông báo thành công khi lấy danh sách low-stock

---

### Bước 5: Tạo DTOs

**File 1:** `modules/inventory/dto/request/RestockRequest.java`
- Field: `@Min(1) @NotNull private Integer quantity;` — số lượng nhập thêm.
- Dùng message từ `InventoryMessageConstant.RESTOCK_QUANTITY_POSITIVE`.

**File 2:** `modules/inventory/dto/response/LowStockResponse.java`
- Fields: `Long variantId`, `String sku`, `String productName`, `String size`, `String color`, `Integer stockQuantity`, `Integer lowStockThreshold`.
- Dùng Lombok `@Builder`, `@Getter`.

---

### Bước 6: Tạo `IInventoryService`

**File:** `modules/inventory/interfaces/IInventoryService.java`

Khai báo 3 phương thức:
```java
void deductStock(Long variantId, int quantity);
void restoreStock(Long variantId, int quantity);
LowStockResponse restock(Long variantId, int quantity);
List<LowStockResponse> getLowStockVariants();
```

> *Tiêu chí hoàn thành*: Interface không có implementation, chỉ là contract.

---

### Bước 7: Triển khai `InventoryService`

**File:** `modules/inventory/service/InventoryService.java`

Annotation: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`.

Inject: `ProductVariantRepository` (dùng nhờ từ module `product`).

**`deductStock(Long variantId, int quantity)`:**
- Đánh dấu `@Transactional`.
- Tìm variant, throw `ResourceNotFoundException` nếu không có.
- Kiểm tra `stockQuantity >= quantity`, nếu không throw `BadRequestException` với message `INSUFFICIENT_STOCK`.
- Set `stockQuantity -= quantity`, save.

**`restoreStock(Long variantId, int quantity)`:**
- Đánh dấu `@Transactional`.
- Tìm variant, throw `ResourceNotFoundException` nếu không có.
- Set `stockQuantity += quantity`, save.

**`restock(Long variantId, int quantity)`:**
- Đánh dấu `@Transactional`.
- Tìm variant, throw `ResourceNotFoundException` nếu không có.
- Set `stockQuantity += quantity`, save.
- Return `LowStockResponse` được map từ variant.

**`getLowStockVariants()`:**
- Đánh dấu `@Transactional(readOnly = true)`.
- Gọi `productVariantRepository.findLowStockVariants()`.
- Map từng `ProductVariant` sang `LowStockResponse`.

> *Tiêu chí hoàn thành*: Inject được vào `OrderService` và `InventoryController` mà không lỗi circular dependency.

---

### Bước 8: Tạo `InventoryController`

**File:** `modules/inventory/controller/InventoryController.java`

Annotation: `@RestController`, `@RequestMapping("/api/admin/inventory")`, `@PreAuthorize("hasRole('ADMIN')")`, `@RequiredArgsConstructor`.

**Endpoint 1:** `GET /low-stock`
- Gọi `inventoryService.getLowStockVariants()`.
- Trả `ApiResponse.of(InventoryMessageConstant.LOW_STOCK_RETRIEVED, result)`.

**Endpoint 2:** `PUT /restock/{variantId}`
- Nhận `@PathVariable Long variantId` và `@Valid @RequestBody RestockRequest request`.
- Gọi `inventoryService.restock(variantId, request.getQuantity())`.
- Trả `ApiResponse.of(InventoryMessageConstant.STOCK_ADJUSTED, result)`.

---

### Bước 9: Refactor `OrderService` — loại bỏ stock logic cũ

**File:** `modules/order/service/OrderService.java`

**9a. Inject `IInventoryService`:**
Thêm `private final IInventoryService inventoryService;` vào danh sách field.

**9b. Trong `placeOrder()` — thay thế block trừ stock cũ (dòng 96–98):**
```
// XÓA:
variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
productVariantRepository.save(variant);

// THAY BẰNG:
inventoryService.deductStock(variant.getId(), cartItem.getQuantity());
```
> Lưu ý: kiểm tra tồn kho (dòng 91–94) vẫn giữ nguyên để có message lỗi rõ ràng trước khi gọi deductStock, HOẶC xóa bỏ nó và để `deductStock()` tự throw — chọn 1 cách nhất quán.

**9c. Trong `updateOrderStatus()` — thêm xử lý khi CANCELLED:**
```java
if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
    for (OrderItem item : order.getItems()) {
        inventoryService.restoreStock(item.getProductVariant().getId(), item.getQuantity());
    }
}
```
Đặt block này song song với block xử lý `DELIVERED` (trước `order.setStatus(status)`).

> *Tiêu chí hoàn thành*: `OrderService` không còn import `ProductVariantRepository` trực tiếp cho mục đích stock (nếu repo này không dùng ở chỗ nào khác trong class thì có thể xóa khỏi field list). Logic stock hoàn toàn được delegate sang `InventoryService`.

---

## 6. Pseudo-code / Code skeleton

### `InventoryService.java`
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements IInventoryService {

    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public void deductStock(Long variantId, int quantity) {
        ProductVariant variant = findVariantOrThrow(variantId);
        if (variant.getStockQuantity() < quantity) {
            throw new BadRequestException(
                String.format(InventoryMessageConstant.INSUFFICIENT_STOCK,
                    variant.getSku(), quantity, variant.getStockQuantity())
            );
        }
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);
    }

    @Override
    @Transactional
    public void restoreStock(Long variantId, int quantity) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
    }

    @Override
    @Transactional
    public LowStockResponse restock(Long variantId, int quantity) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
        return mapToLowStockResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockResponse> getLowStockVariants() {
        return productVariantRepository.findLowStockVariants()
            .stream()
            .map(this::mapToLowStockResponse)
            .toList();
    }

    private ProductVariant findVariantOrThrow(Long variantId) {
        return productVariantRepository.findById(variantId)
            .orElseThrow(() -> new ResourceNotFoundException(InventoryMessageConstant.VARIANT_NOT_FOUND));
    }

    private LowStockResponse mapToLowStockResponse(ProductVariant v) {
        return LowStockResponse.builder()
            .variantId(v.getId())
            .sku(v.getSku())
            .productName(v.getProduct().getName())
            .size(v.getSize())
            .color(v.getColor())
            .stockQuantity(v.getStockQuantity())
            .lowStockThreshold(v.getLowStockThreshold())
            .build();
    }
}
```

### Phần thêm vào `ProductVariant.java`
```java
// Thêm vào cuối class, trước dấu }

@Version
private Long version;  // Optimistic Locking — Hibernate tự quản lý

@Column(name = "low_stock_threshold")
@Builder.Default
private Integer lowStockThreshold = 5;
```

### Phần sửa trong `OrderService.updateOrderStatus()`
```java
// Thêm block CANCELLED song song với block DELIVERED
if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
    for (OrderItem item : order.getItems()) {
        inventoryService.restoreStock(
            item.getProductVariant().getId(),
            item.getQuantity()
        );
    }
}
```

---

## 6.5. Xử lý Concurrent Orders (Race Condition)

### Vấn đề

Kịch bản: sản phẩm X còn **1 cái**. User A và User B **cùng lúc** gọi `POST /api/orders`.

```
Thread A: đọc stock = 1  ──────────────────────────► save(stock = 0) ✅
Thread B: đọc stock = 1  ────────────────────────────────► save(stock = -1) ❌ Bug!
```

Nếu không có cơ chế bảo vệ, cả 2 đều thấy stock = 1, đều đặt được, kết quả stock bị âm.

---

### Giải pháp: Optimistic Locking với `@Version`

Khi thêm `@Version Long version` vào `ProductVariant`, Hibernate tự động thêm điều kiện `WHERE version = ?` vào câu `UPDATE`. Nếu `version` đã thay đổi (do thread khác commit trước), Hibernate sẽ throw `ObjectOptimisticLockingFailureException`.

**Flow bảo vệ:**
```
Thread A: đọc (stock=1, version=0) ──────────────────► UPDATE ... WHERE version=0 → OK, version thành 1 ✅
Thread B: đọc (stock=1, version=0) ──────────────────────────────► UPDATE ... WHERE version=0 → 0 rows affected
                                                                         └──► ObjectOptimisticLockingFailureException
                                                                              └──► ApiExceptionHandler bắt → 409
```

---

### Bước triển khai

#### A. Đã xử lý ở Bước 2: Thêm `@Version` vào `ProductVariant`
*(Không cần làm thêm — `@Version` đã được hướng dẫn ở Bước 2)*

#### B. Thêm constant vào `InventoryMessageConstant` (module-level)

**File:** `modules/inventory/constant/InventoryMessageConstant.java`

Thêm hằng số vào class (cùng chỗ với các message khác của module):

```java
public static final String STOCK_CONFLICT = "Stock update conflict, please try again";
```

> **Tại sao KHÔNG đặt vào `ApiExceptionConstant` global?**  
> `ApiExceptionConstant` là tầng global/common. Nếu đặt message nghiệp vụ kho hàng vào đây, nó sẽ chứa logic thuộc về module `inventory` — vi phạm nguyên lý tách biệt (Separation of Concerns). Global constant chỉ nên chứa message dùng chung thực sự như `INVALID_REQUEST_DATA`, `INTERNAL_SERVER_ERROR`.

#### C. Bắt exception trong `InventoryService` — rethrow thành `AppException`

**Nguyên lý:** `ObjectOptimisticLockingFailureException` do Hibernate throw bên trong `InventoryService`. Đây là nơi gần nhất và đúng nhất để xử lý — bắt tại chỗ và rethrow thành một `AppException` có message rõ ràng từ `InventoryMessageConstant`.

`ApiExceptionHandler` **không cần handler mới** — handler `AppException` đã có sẵn sẽ xử lý.

**File:** `modules/inventory/service/InventoryService.java`

Trong method `deductStock()`, bọc lệnh `save()` bằng try-catch:
```java
@Override
@Transactional
public void deductStock(Long variantId, int quantity) {
    ProductVariant variant = findVariantOrThrow(variantId);
    if (variant.getStockQuantity() < quantity) {
        throw new BadRequestException(
            String.format(InventoryMessageConstant.INSUFFICIENT_STOCK,
                variant.getSku(), quantity, variant.getStockQuantity())
        );
    }
    try {
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);
    } catch (ObjectOptimisticLockingFailureException ex) {
        throw new BadRequestException(InventoryMessageConstant.STOCK_CONFLICT);
    }
}
```

Import cần thêm vào `InventoryService`:
```java
import org.springframework.orm.ObjectOptimisticLockingFailureException;
```

> **Tại sao KHÔNG thêm handler mới vào `ApiExceptionHandler`?**  
> Nếu `ApiExceptionHandler` (global) import `InventoryMessageConstant` (module), tầng global sẽ phụ thuộc vào tầng module — vi phạm kiến trúc phân tầng. Thay vào đó, `InventoryService` tự "dịch" exception của Hibernate sang `AppException` — exception đã có message, global handler chỉ cần gọi `ex.getMessage()`.

---

### Flow hoàn chỉnh sau khi implement

```
Thread B: save() → ObjectOptimisticLockingFailureException (Hibernate)
              └─► catch trong InventoryService
                    └─► throw new BadRequestException(InventoryMessageConstant.STOCK_CONFLICT)
                              └─► ApiExceptionHandler.handleAppException()  ← handler có sẵn
                                        └─► 400 Bad Request + message rõ ràng
```

### Kết quả sau khi implement

| User | Kết quả | HTTP Status |
|---|---|---|
| User A (commit trước) | Đặt hàng thành công, stock = 0 | 200 |
| User B (commit sau) | Thất bại, nhận lỗi rõ ràng | **400 Bad Request** |

Body response User B nhận được:
```json
{
  "message": "Stock update conflict, please try again",
  "data": null,
  "timestamp": "2026-05-14T01:00:00Z"
}
```

---

## 7. Exception handling & validation

| Tình huống | Exception | HTTP Status | File xử lý |
|---|---|---|---|
| Variant không tồn tại | `ResourceNotFoundException` | 404 | `ApiExceptionHandler` (handler `AppException`) |
| Tồn kho không đủ | `BadRequestException` | 400 | `ApiExceptionHandler` (handler `AppException`) |
| `quantity <= 0` ở restock | Validation `@Min(1)` | 400 | `ApiExceptionHandler` (handler `MethodArgumentNotValidException`) |
| 2 request đồng thời | `ObjectOptimisticLockingFailureException` | **409** | `ApiExceptionHandler` — **handler mới cần thêm** |

> **Lưu ý message format:** `INSUFFICIENT_STOCK` nên có format `%s` để truyền SKU vào, ví dụ:  
> `"Insufficient stock for [SKU-001]: requested 2, available 1"`

---

## 8. Checklist tự test

### Happy path
- [ ] Khách đặt hàng 2 sản phẩm → `stockQuantity` giảm 2.
- [ ] Admin hủy đơn → `stockQuantity` tăng lại 2.
- [ ] Admin restock thêm 10 → `stockQuantity` tăng 10.
- [ ] `GET /api/admin/inventory/low-stock` → trả đúng các variant có stock ≤ threshold.

### Edge cases
- [ ] Đặt hàng đúng bằng số lượng còn lại (stockQuantity = quantity) → thành công, stock về 0.
- [ ] Restock variant có `lowStockThreshold = null` → vẫn hoạt động bình thường, chỉ không xuất hiện ở `/low-stock` nếu threshold là null.

### Negative cases
- [ ] Đặt hàng vượt tồn kho → 400 với message rõ ràng.
- [ ] Restock với `quantity = 0` → 400 do validation.
- [ ] Restock với `variantId` không tồn tại → 404.
- [ ] User thường gọi `GET /api/admin/inventory/low-stock` → 403.

### Concurrent (Race condition)
- [ ] Dùng Postman Runner hoặc script gọi 2 request đồng thời mua sản phẩm cuối còn 1 cái → chỉ 1 request thành công, 1 request nhận 409.

---

## 9. Checklist review trước khi commit

- [ ] Build thành công: `mvn clean compile`
- [ ] Cấu trúc `modules/inventory/` đủ các sub-package: `constant`, `controller`, `dto`, `interfaces`, `service`.
- [ ] Không có hardcode string nào trong Controller hoặc Service — tất cả dùng `InventoryMessageConstant`.
- [ ] `@Version` đã có trong `ProductVariant`, DB có cột `version`.
- [ ] `OrderService` đã inject `IInventoryService` (không inject trực tiếp `ProductVariantRepository` cho mục đích stock).
- [ ] `updateOrderStatus()` đã xử lý hoàn kho khi `CANCELLED`.
- [ ] `ApiExceptionHandler` đã có handler cho `ObjectOptimisticLockingFailureException`.
- [ ] Tất cả endpoint admin có `@PreAuthorize("hasRole('ADMIN')")`.
- [ ] Các DTO dùng `@Valid` ở Controller và có annotation validation đầy đủ.

---

## 10. Follow-up nâng cấp

- **Email cảnh báo hàng ngày:** Dùng `@Scheduled` + `@Async` để gửi mail danh sách low-stock mỗi sáng — không làm block HTTP thread.
- **Thống kê nhập/xuất theo tháng:** Khi cần báo cáo, có thể thêm `InventoryLog` entity vào module `inventory` này mà không ảnh hưởng các module khác — đây là lý do tách module riêng từ đầu.
- **Thêm `lowStockThreshold` vào Admin Variant API:** Cho phép admin set ngưỡng cảnh báo khác nhau cho từng variant qua endpoint `PUT /api/admin/product-variants/{variantId}`.
