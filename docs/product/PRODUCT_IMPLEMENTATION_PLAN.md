# Kế hoạch Triển khai Product Module (Sport Pro)

Tài liệu này liệt kê chi tiết các công việc cần làm và hướng dẫn implement module Product tiệm cận chuẩn production.

---

## 1. Thiết kế Entity (Database)

### 1.1 Product (Sản phẩm chung)
Lưu thông tin tổng quan của một sản phẩm.
- **Fields**: `id`, `name`, `slug`, `description`, `category_id`, `brand_id`, `gender` (Enum: `MALE`, `FEMALE`, `UNISEX`), `status` (Enum: `ACTIVE`, `INACTIVE`, `DELETED`), `created_at`, `updated_at`.
- **Cách implement**: 
  - Đánh dấu `@Entity` map với table `products`.
  - Áp dụng Soft Delete bằng Hibernate: `@SQLDelete(sql = "UPDATE products SET status = 'DELETED' WHERE id=?")` và `@Where(clause = "status <> 'DELETED'")` (hoặc `@Filter`).
  - Dùng hàm/library tiện ích để tự động sinh `slug` ở Backend từ `name` trước khi lưu (`@PrePersist`, `@PreUpdate`), xử lý cộng thêm hậu tố số (vd: `-1`) nếu slug đã tồn tại trong DB.

### 1.2 ProductVariant (Phiên bản cụ thể)
Lưu thông tin có tính biến động như màu sắc, size, giá cả, và tồn kho.
- **Fields**: `id`, `product_id`, `sku`, `size`, `color`, `price`, `sale_price`, `stock_quantity`, `status`, `created_at`, `updated_at`.
- **Cách implement**:
  - `@ManyToOne` với `Product`.
  - Cột `sku` phải đánh `UNIQUE`.
  - Không lưu giá hay stock ở `Product`, bắt buộc truy xuất từ `ProductVariant`.

### 1.3 ProductImage (Hình ảnh sản phẩm)
- **Fields**: `id`, `product_id`, `variant_id` (nullable), `image_url`, `is_thumbnail`, `sort_order`.
- **Cách implement**:
  - Nếu ảnh là chung cho toàn bộ sản phẩm -> `variant_id = null`.
  - Nếu ảnh đặc thù cho một màu/size cụ thể -> có giá trị `variant_id`.

---

## 2. Xây dựng Data Transfer Objects (DTO)

Tách biệt DTO cho từng use case để tối ưu hóa payload.

### 2.1 Request DTOs (Kèm Validation)
- **ProductCreateRequest / ProductUpdateRequest**:
  - `@NotBlank(message = "Name is required")` cho `name`.
  - `@NotNull` cho `categoryId`, `brandId`, `gender`.
- **ProductVariantRequest**:
  - `@NotBlank` cho `sku`, `size`, `color`.
  - `@Positive(message = "Price must be > 0")` cho `price`.
  - Chú ý logic custom validation: `salePrice` phải `<= price`.
  - `@Min(value = 0)` cho `stockQuantity`.

### 2.2 Response DTOs
- **ProductListResponse**: Dùng cho danh sách. Trả về `id`, `name`, `slug`, `thumbnailUrl`, `brandName`, `categoryName`, `minPrice`, `maxPrice`, `availableSizes`, `availableColors`.
- **ProductDetailResponse**: Chi tiết sản phẩm. Bao gồm các trường của List nhưng có thêm `description`, danh sách `images`, và danh sách `variants`.
- **ProductVariantResponse**: `id`, `sku`, `size`, `color`, `price`, `salePrice`, `stockQuantity`, `status`.

---

## 3. Thiết kế & Triển khai API

Sử dụng RESTful chuẩn và chia role rõ ràng.

### 3.1 Admin API (`/api/admin/products`) - Yêu cầu Role ADMIN
- `GET /` : Lấy danh sách sản phẩm (có phân trang & filter cơ bản).
- `GET /{id}` : Chi tiết sản phẩm để edit.
- `POST /` : Tạo sản phẩm mới (chưa có variant).
- `PUT /{id}` : Cập nhật thông tin chung sản phẩm.
- `DELETE /{id}` : Xóa mềm (`status = DELETED`).
- `POST /{productId}/variants` : Thêm mới variant.
- `PUT /product-variants/{variantId}` : Cập nhật variant.
- `DELETE /product-variants/{variantId}` : Xóa variant.
- `POST /{productId}/images` : Upload/thêm link ảnh.
- `DELETE /product-images/{imageId}` : Xóa ảnh.

### 3.2 Public API (`/api/products`) - Cho Khách hàng
- `GET /` : Danh sách sản phẩm (Bắt buộc phân trang: `page`, `size`, trả về format: `items`, `page`, `size`, `totalElements`, `totalPages`).
  - Hỗ trợ Filter qua query params: `categoryId`, `brandId`, `gender`, `size`, `color`, `minPrice`, `maxPrice`, `sort`.
- `GET /{slug}` : Xem chi tiết sản phẩm theo slug (để SEO tốt hơn).

---

## 4. Xử lý logic nghiệp vụ quan trọng & Tìm kiếm (Filter)

- **Search & Filter (Khuyên dùng Specification / Criteria API)**:
  - Tạo `ProductSpecification` để build dynamic query.
  - Tránh viết các method dài kiểu `findByCategoryAndBrandAnd...` trong JPA Repository vì rất khó bảo trì.
  - Xử lý join sang bảng `product_variants` nếu có filter theo `size`, `color`, `price`.
- **Quản lý Giỏ hàng (Cart) & Đơn hàng (Order)**:
  - Nhắc nhở: CartItem và OrderItem sau này sẽ mapping tới **`variant_id`**, không phải `product_id`.

---

## 5. Trình tự thực hiện (Roadmap)

Thực hiện code theo luồng từ dưới lên trên (Bottom-up):
- [ ] **Bước 1**: Tạo các Entity `Product`, `ProductVariant`, `ProductImage` cùng với các file Migration/SQL (bao gồm cả file Index).
- [ ] **Bước 2**: Xây dựng Repository layer (bao gồm Custom Repository để dùng JPA Specification/Criteria cho Filter).
- [ ] **Bước 3**: Tạo các DTOs (Request/Response) kèm theo rule Validation (Jakarta Validation).
- [ ] **Bước 4**: Implement Product Service và Admin API để thêm, sửa, xóa (soft delete) Product.
- [ ] **Bước 5**: Implement Variant Service và Admin API thao tác Variant.
- [ ] **Bước 6**: Xử lý logic Upload và lưu trữ URL Image.
- [ ] **Bước 7**: Xây dựng Public API List Products (Hỗ trợ Pagination & Filter động qua Criteria API).
- [ ] **Bước 8**: Cung cấp Public API Get Product Detail By Slug.
- [ ] **Bước 9**: Tối ưu hóa truy vấn (N+1 query problem, fetch join) và kiểm tra hoạt động của các Index DB.
