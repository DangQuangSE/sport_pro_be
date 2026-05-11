# Hướng dẫn triển khai Module Cart (Giỏ hàng)

## 1. Mô tả bài toán
- **Mục tiêu business:** Cho phép người dùng đã xác thực (Authenticated User) quản lý giỏ hàng cá nhân: thêm sản phẩm, cập nhật số lượng, xóa sản phẩm và xem tổng quan giỏ hàng trước khi thanh toán.
- **Phạm vi in-scope:** CRUD thao tác giỏ hàng lưu trong Database. Tính toán tổng tiền.
- **Phạm vi out-scope:** 
  - Khách vãng lai (Guest): Frontend tự quản lý bằng LocalStorage, sau khi Login sẽ gọi API đồng bộ lên DB (API đồng bộ có thể làm ở phase sau).
  - Thanh toán & Tạo đơn: Sẽ được tách riêng ở Module Order.

## 2. Thiết kế kỹ thuật (high-level)
- **Kiến trúc flow:** Client -> CartController -> CartService -> CartRepository/CartItemRepository -> Database.
- **Thành phần liên quan:** 
  - `modules/cart/domain/Cart.java`
  - `modules/cart/domain/CartItem.java`
  - `modules/product/domain/ProductVariant.java` (Để lấy thông tin tồn kho và giá)
  - `modules/auth/domain/User.java` (Owner của giỏ hàng)

**Thiết kế DB Schema (ERD):**
- **Table `carts`**: `id` (PK), `user_id` (Unique, FK), `created_at`, `updated_at`.
- **Table `cart_items`**: `id` (PK), `cart_id` (FK), `product_variant_id` (FK), `quantity` (Integer > 0).
  - *Constraint*: `cart_id` + `product_variant_id` phải là Unique (Tránh việc 1 variant xuất hiện 2 dòng trong cùng 1 giỏ hàng).

## 3. Thư viện đề xuất
- **Không cần thêm thư viện mới.** Tận dụng `spring-boot-starter-data-jpa` cho DB và `spring-boot-starter-validation` cho kiểm tra dữ liệu đầu vào.

## 4. Cấu hình cần thêm
- **Không cần thêm cấu hình** vào `application.properties` hay `pom.xml`. Toàn bộ sẽ dùng cấu hình DB hiện tại.

## 5. Kế hoạch triển khai code tay (step-by-step)
*   **Bước 1:** Tạo Entity `Cart` và `CartItem` trong package `modules/cart/domain`. Cấu hình quan hệ OneToOne với User, OneToMany với CartItem, ManyToOne với ProductVariant.
*   **Bước 2:** Tạo `CartRepository` và `CartItemRepository`. Viết method `findByUserId` trong `CartRepository`.
*   **Bước 3:** Tạo package `dto/request` và `dto/response`. Định nghĩa `CartItemRequest` (variantId, quantity) và `CartResponse` (tổng tiền, danh sách item trả về).
*   **Bước 4:** Tạo `ICartService` và `CartService`. Triển khai các hàm xử lý: Lấy giỏ hàng, Thêm/Sửa/Xóa Item.
*   **Bước 5:** Tạo `CartController`. Đánh dấu `@PreAuthorize("hasRole('USER')")` và lấy `userId` từ `SecurityContextHolder`.

## 6. Pseudo-code / code skeleton (Không copy-paste chạy ngay)

**Entity `Cart`**
```java
package com.sport_pro_be.modules.cart.domain;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Quan hệ 1-N với CartItem. Nhớ dùng CascadeType.ALL và orphanRemoval = true
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
}
```

**Entity `CartItem`**
```java
package com.sport_pro_be.modules.cart.domain;

import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cart_id", "product_variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity;
}
```

**Service `CartService` Logic**
```java
@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    // Inject các Repositories: Cart, CartItem, ProductVariant, UserRepository

    @Transactional
    public CartResponse getMyCart(Long userId) {
        // 1. Tìm Cart theo userId. Nếu chưa có -> Tạo mới Cart trống và save.
        // 2. Tính toán tổng tiền: duyệt qua danh sách items, lấy (variant.price * item.quantity).
        // 3. Map sang CartResponse.
    }

    @Transactional
    public CartResponse addOrUpdateItem(Long userId, CartItemRequest request) {
        // 1. Lấy Cart của user (nếu ko có thì tạo mới).
        // 2. Tìm ProductVariant theo request.getVariantId(). Kiểm tra tồn tại và status ACTIVE.
        // 3. Tìm xem variant này đã có trong Cart chưa (bằng cách duyệt danh sách cart.getItems()).
        // 4. Xử lý logic quantity:
        //    - Có rồi: quantity = quantity cũ + request.getQuantity().
        //    - Chưa có: quantity = request.getQuantity().
        // 5. Kiểm tra tồn kho: Nếu quantity > variant.getStockQuantity() -> ném BadRequestException.
        // 6. Nếu quantity <= 0 -> Xóa CartItem khỏi Cart (cart.getItems().remove(item)).
        // 7. Ngược lại -> Cập nhật quantity cho item hiện tại, hoặc thêm item mới vào cart.getItems().
        // 8. Save Cart (JPA Cascade sẽ lo việc lưu/xóa CartItem dưới DB).
        // 9. Trả về CartResponse mới.
    }

    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        // 1. Lấy Cart của user.
        // 2. Tìm item cần xóa trong danh sách cart.getItems() thông qua cartItemId.
        // 3. Xóa item ra khỏi list (cart.getItems().remove(item)). Hibernate sẽ tự xóa dưới DB nhờ orphanRemoval = true.
    }
}
```

## 7. Exception handling & validation
- **`ResourceNotFoundException`**: Khi `product_variant_id` không tồn tại hoặc variant bị vô hiệu hóa (STATUS != ACTIVE).
- **`BadRequestException`**: 
  - "Số lượng yêu cầu vượt quá tồn kho hiện tại" (Out of stock).
- **Validation DTO**:
  - `@NotNull(message = "Variant ID không được để trống")` cho trường `variantId`.
  - Có thể nới lỏng `@Min(0)` cho `quantity` nếu bạn cho phép frontend truyền `quantity = 0` để xóa item, hoặc tách API xóa riêng rẽ.

## 8. Checklist tự test
- [ ] Lấy giỏ hàng lần đầu khi user vừa đăng ký (Kỳ vọng: Trả về giỏ hàng trống, không bị lỗi NullPointer).
- [ ] Thêm 1 sản phẩm vào giỏ (Kỳ vọng: Tổng tiền tính đúng, số lượng item là 1).
- [ ] Thêm tiếp **cùng** 1 sản phẩm đó vào giỏ (Kỳ vọng: Không tạo dòng mới dưới DB, chỉ tăng `quantity` lên 2, Constraint Unique không bị vi phạm).
- [ ] Thêm sản phẩm với `quantity` > `stockQuantity` (Kỳ vọng: Lỗi HTTP 400).
- [ ] Update `quantity` về 0 (Kỳ vọng: Item tự động bị xóa khỏi giỏ hàng nhờ orphan removal).

## 9. Checklist review trước khi commit
- [ ] **Bảo mật:** Đảm bảo `userId` được trích xuất từ JWT token ở Controller qua `SecurityContextHolder`. Tuyệt đối không cho phép client truyền `userId` vào request body. (Tránh lỗi User A sửa giỏ hàng User B).
- [ ] **Hiệu năng:** Kiểm tra log SQL console xem có bị lỗi N+1 Query khi `getMyCart` kèm danh sách `CartItem` và `ProductVariant` không. Nếu có quá nhiều query SELECT, hãy cân nhắc thêm `@EntityGraph(attributePaths = {"items", "items.productVariant"})` trên hàm `findByUserId` của `CartRepository`.

## 10. Follow-up nâng cấp
- Khi làm module `Order`, giỏ hàng sẽ bị xóa (`clear`) hoặc đánh dấu là đã chuyển thành đơn hàng sau khi thanh toán thành công.
- Tích hợp Redis nếu số lượng truy cập quá lớn để giảm tải cho DB. Tuy nhiên, lưu ý việc đồng bộ Redis - DB khá phức tạp, nên chỉ làm khi thực sự cần.
