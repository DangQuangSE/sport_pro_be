# Tài liệu chức năng Module: Category & Brand

## 1) Mục tiêu

Module **Category** và **Brand** đóng vai trò quản lý dữ liệu nền tảng cho sản phẩm (Product) trong hệ thống Sport Pro. 

- **Category**: Phân loại sản phẩm theo danh mục (hỗ trợ phân cấp cha-con).
- **Brand**: Quản lý thương hiệu sản phẩm.

Kiến trúc này đảm bảo tính nhất quán, hiệu năng cao và tuân thủ các nguyên tắc Senior Backend đã đề ra.

---

## 2) Kiến trúc hệ thống (Architecture)

Tuân thủ mô hình **Controller - Service - Repository** và tách biệt **Entity - DTO**, đồng bộ với module Auth hiện tại.

### Cấu trúc Package
```text
com.sport_pro_be.
 ├── category/ (Module Category)
 │    ├── controller/
 │    │    ├── CategoryController.java (Public API)
 │    │    └── AdminCategoryController.java (Admin API)
 │    ├── service/
 │    │    ├── CategoryService.java (Interface)
 │    │    └── CategoryServiceImpl.java
 │    ├── domain/ (Entity)
 │    │    └── Category.java
 │    ├── repository/
 │    │    └── CategoryRepository.java
 │    └── dto/
 │         ├── CategoryRequest.java
 │         └── CategoryResponse.java
 └── brand/ (Module Brand)
      ├── controller/
      │    ├── BrandController.java
      │    └── AdminBrandController.java
      ├── service/
      │    ├── BrandService.java
      │    └── BrandServiceImpl.java
      ├── domain/
      │    └── Brand.java
      ├── repository/
      │    └── BrandRepository.java
      └── dto/
           ├── BrandRequest.java
           └── BrandResponse.java
```

### Các thành phần dùng chung
- **Exception Handling**: Sử dụng `GlobalExceptionHandler` đã có để bắt các ngoại lệ và trả về định dạng lỗi thống nhất.
- **Response Format**: Khuyến nghị triển khai một `ApiResponse<T>` generic trong package `common` để bao bọc mọi phản hồi thành công, giúp Frontend dễ dàng xử lý.
- **Audit**: Tự động quản lý `createdAt`, `updatedAt` thông qua JPA Auditing (sử dụng `@EntityListeners(AuditingEntityListener.class)`).
- **Soft Delete**: Sử dụng `@SQLRestriction("deleted_at IS NULL")` để tự động lọc bỏ các bản ghi đã xóa.

---

## 3) Thiết kế Cơ sở dữ liệu (Database Design)

Sử dụng **Soft Delete** (`deletedAt`) để bảo toàn dữ liệu khi có liên kết với Product.

### 3.1 Bảng `categories`
| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | Primary Key | ID tự tăng |
| name | String(100) | Not Null, Unique | Tên danh mục |
| slug | String(150) | Not Null, Unique | Đường dẫn SEO |
| description | Text | | Mô tả danh mục |
| parent_id | Long | FK (categories.id) | Danh mục cha (null nếu là root) |
| image_url | String(255) | | Ảnh đại diện |
| is_active | Boolean | Default: true | Trạng thái hiển thị |
| display_order | Integer | Default: 0 | Thứ tự hiển thị |
| created_at | Timestamp | | Thời gian tạo |
| updated_at | Timestamp | | Thời gian cập nhật |
| deleted_at | Timestamp | | Thời gian xóa (Soft delete) |

**Index:** `index(parent_id)`, `index(is_active)`, `index(deleted_at)`.

### 3.2 Bảng `brands`
| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | Primary Key | ID tự tăng |
| name | String(100) | Not Null, Unique | Tên thương hiệu |
| slug | String(150) | Not Null, Unique | Đường dẫn SEO |
| description | Text | | Mô tả thương hiệu |
| logo_url | String(255) | | Ảnh Logo |
| country | String(100) | | Quốc gia |
| website_url | String(255) | | Website chính thức |
| is_active | Boolean | Default: true | Trạng thái hiển thị |
| created_at | Timestamp | | Thời gian tạo |
| updated_at | Timestamp | | Thời gian cập nhật |
| deleted_at | Timestamp | | Thời gian xóa (Soft delete) |

**Index:** `index(is_active)`, `index(deleted_at)`.

---

## 4) API Contract

### 4.1 Public API (Người dùng khách)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/categories` | Lấy danh sách category active (phân trang + search) |
| GET | `/api/categories/tree` | Lấy cấu trúc cây category |
| GET | `/api/categories/{slug}` | Chi tiết category theo slug |
| GET | `/api/brands` | Lấy danh sách brand active (phân trang + search) |
| GET | `/api/brands/{slug}` | Chi tiết brand theo slug |

### 4.2 Admin API (Quản trị viên)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin/categories` | Tạo category mới |
| PUT | `/api/admin/categories/{id}` | Cập nhật category |
| PATCH | `/api/admin/categories/{id}/status` | Bật/tắt trạng thái hiển thị |
| DELETE | `/api/admin/categories/{id}` | Xóa soft delete |
| POST | `/api/admin/brands` | Tạo brand mới |
| PUT | `/api/admin/brands/{id}` | Cập nhật brand |
| PATCH | `/api/admin/brands/{id}/status` | Bật/tắt trạng thái hiển thị |
| DELETE | `/api/admin/brands/{id}` | Xóa soft delete |

---

## 5) Quy tắc Nghiệp vụ (Business Rules)

### 5.1 Xử lý Slug
- Tự động generate từ `name` khi tạo/cập nhật.
- Đảm bảo tính duy nhất. Nếu trùng, hệ thống sẽ tự động thêm hậu tố (suffix) như `-1`, `-2`.
- Chuẩn hóa: Lowercase, không dấu, thay khoảng trắng bằng gạch ngang.

### 5.2 Xử lý Phân cấp (Category Tree)
- Tránh vòng lặp: `parentId` không được trỏ về chính `id` của nó hoặc các con của nó.
- Khi xóa một category cha: Các con của nó có thể được chuyển lên root hoặc xóa kèm theo (tùy cấu hình, khuyến khích chuyển lên root hoặc giữ nguyên logic link).

### 5.3 Soft Delete
- Không dùng `repository.delete()`.
- Sử dụng `@SQLRestriction` hoặc filter để mặc định không lấy các record đã xóa.
- Khi xóa, kiểm tra xem có Product nào đang link tới không (nếu có, có thể cảnh báo nhưng vẫn cho xóa vì đã dùng soft delete).

### 5.4 Validation
- `name`: 2-100 ký tự, không trống.
- `websiteUrl`: Phải đúng format URL.
- `displayOrder`: Phải là số nguyên dương.

---

## 6) Bảo mật & Phân quyền (RBAC)

- **Public Access**: Tất cả API bắt đầu bằng `/api/` (trừ `/api/admin/`) đều được truy cập công khai.
- **Admin Access**: Các API bắt đầu bằng `/api/admin/` yêu cầu token JWT hợp lệ và User phải có role `ADMIN`.

---

## 7) Kế hoạch triển khai (Roadmap)

1. **Phase 1: Foundation**
   - Thiết lập Entity Category & Brand với JPA Auditing & Soft Delete.
   - Tạo Repository và các cấu trúc DTO cơ bản.
2. **Phase 2: Logic & Service**
   - Triển khai logic generate Slug tự động.
   - Triển khai logic Category Tree.
   - Viết Service xử lý nghiệp vụ & validation.
3. **Phase 3: API & Security**
   - Xây dựng Public Controller & Admin Controller.
   - Cấu hình phân quyền trong `SecurityConfig`.
4. **Phase 4: Optimization**
   - Thêm indexing cho Database.
   - Tối ưu truy vấn N+1 cho Category Tree.
   - Viết Unit Test cho Service.
