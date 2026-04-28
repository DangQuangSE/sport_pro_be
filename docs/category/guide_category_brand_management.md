# Hướng dẫn triển khai Module Category & Brand

Tài liệu này hướng dẫn chi tiết cách triển khai module Category và Brand cho dự án Sport Pro, tuân thủ các nguyên tắc Senior Backend và quy ước hỗ trợ AI.

---

## 1) Mô tả bài toán

### Mục tiêu business
- Xây dựng hệ thống quản lý danh mục (Category) và thương hiệu (Brand) làm nền tảng cho việc quản lý sản phẩm.
- Hỗ trợ phân loại sản phẩm đa cấp (Category Tree) và gán nhãn thương hiệu.
- Đảm bảo hiệu năng truy vấn và SEO (thông qua Slug).

### Phạm vi (Scope)
- **In-scope**: 
    - CRUD Category & Brand (Admin).
    - API lấy danh sách, cây danh mục, chi tiết theo slug (Public).
    - Tự động generate Slug.
    - Soft Delete & Auditing.
- **Out-of-scope**: 
    - Giao diện người dùng (Frontend).
    - Quản lý kho hàng (Inventory).
    - Quản lý sản phẩm (Product - Sẽ triển khai sau).

---

## 2) Thiết kế kỹ thuật (High-level)

### Kiến trúc Flow
`Client -> Controller -> Service -> Repository -> Database`

### Thành phần liên quan
- **Entities**: `Category`, `Brand`.
- **Repositories**: `CategoryRepository`, `BrandRepository`.
- **Services**: `CategoryService`, `BrandService`.
- **Controllers**: 
    - `AdminCategoryController`, `AdminBrandController` (Role ADMIN).
    - `CategoryController`, `BrandController` (Public).
- **DTOs**: 
    - Request: `CategoryRequest`, `BrandRequest`.
    - Response: `CategoryResponse`, `BrandResponse`.

---

## 3) Thư viện đề xuất

1. **Lombok**: Giảm thiểu boilerplate code (Getter, Setter, Constructor).
2. **Spring Data JPA**: Tương tác với cơ sở dữ liệu.
3. **Hibernate Validator**: Validate dữ liệu đầu vào thông qua annotation (@NotBlank, @Size...).
4. **MapStruct** (Khuyên dùng): Mapping giữa Entity và DTO một cách tường minh và hiệu năng cao. Nếu chưa muốn dùng thư viện, có thể dùng manual Mapper.
5. **Apache Commons Lang3**: Hỗ trợ xử lý String (Normalizer) để tạo Slug.

---

## 4) Cấu hình cần thêm

### pom.xml
Đảm bảo đã có các dependency sau:
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-web`
- `lombok`

### application.properties
Kích hoạt JPA Auditing và SQL logging để debug:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
# JPA Auditing sẽ được kích hoạt qua @Configuration class
```

---

## 5) Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Thiết lập Auditing & Base Entity
- Tạo `@Configuration` class để kích hoạt JPA Auditing (`@EnableJpaAuditing`).
- Tạo một `AbstractAuditingEntity` để các entity khác kế thừa (chứa `createdAt`, `updatedAt`).

### Bước 2: Triển khai Domain Entity
- Tạo `Category.java` và `Brand.java`.
- Sử dụng `@SQLRestriction("deleted_at IS NULL")` để hỗ trợ Soft Delete.
- Định nghĩa các mối quan hệ (ví dụ: Category cha-con).

### Bước 3: Tạo Repository
- Tạo `CategoryRepository` và `BrandRepository` kế thừa `JpaRepository`.
- Thêm các method truy vấn theo `slug`, `isActive`, `parentId`.

### Bước 4: Xây dựng DTO & Mapper
- Tạo các lớp Request/Response DTO.
- Viết logic chuyển đổi (Mapper).

### Bước 5: Viết Service Layer
- **Interface**: Định nghĩa các nghiệp vụ.
- **Implementation**:
    - Xử lý logic tạo Slug tự động (Normalization, xử lý trùng lặp).
    - Xử lý logic Category Tree (Tránh vòng lặp).
    - Xử lý Soft Delete.

### Bước 6: Xây dựng Controller
- Triển khai Public API (không cần login).
- Triển khai Admin API (bảo vệ bởi Spring Security).

---

## 6) Chi tiết triển khai Code (Implementation Details)

### 6.1 Cấu hình JPA Auditing
Tạo file trong package `config`:
```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Kích hoạt tự động điền createdAt, updatedAt
}
```

### 6.2 Base Entity (AbstractAuditingEntity)
Tạo trong package `common` hoặc `domain` gốc:
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class AbstractAuditingEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### 6.3 Category Entity
```java
@Entity
@Table(name = "categories")
@SQLRestriction("deleted_at IS NULL")
@Getter @Setter
public class Category extends AbstractAuditingEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    private String description;
    private Long parentId;
    private String imageUrl;
    private boolean isActive = true;
    private Integer displayOrder = 0;

    private LocalDateTime deletedAt;
}
```

### 6.4 Data Transfer Object (DTO)
```java
// CategoryRequest.java
@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100)
    private String name;
    
    private String description;
    private Long parentId;
    private String imageUrl;
    private Integer displayOrder;
}

// CategoryResponse.java
@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String imageUrl;
    private boolean isActive;
    private Integer displayOrder;
    private List<CategoryResponse> children; // Cho cấu trúc cây
}
```

### 6.5 Logic xử lý Slug (Service Implementation)
```java
private String generateUniqueSlug(String name) {
    // 1. Chuyển tiếng Việt có dấu thành không dấu, lowercase, thay khoảng trắng bằng gạch ngang
    String baseSlug = Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-");
            
    // 2. Kiểm tra trùng lặp và thêm suffix nếu cần
    String slug = baseSlug;
    int counter = 1;
    while (categoryRepository.existsBySlug(slug)) {
        slug = baseSlug + "-" + counter++;
    }
    return slug;
}
```

### 6.6 Logic xử lý Cây Danh mục (Category Tree)
```java
@Override
public List<CategoryResponse> getCategoryTree() {
    List<Category> allCategories = categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
    
    // Bước 1: Map sang DTO
    Map<Long, CategoryResponse> nodes = allCategories.stream()
        .map(this::mapToResponse) // Hàm convert Entity -> DTO
        .collect(Collectors.toMap(CategoryResponse::getId, Function.identity()));

    List<CategoryResponse> roots = new ArrayList<>();

    // Bước 2: Xây dựng quan hệ cha-con
    nodes.values().forEach(node -> {
        if (node.getParentId() == null) {
            roots.add(node);
        } else {
            CategoryResponse parent = nodes.get(node.getParentId());
            if (parent != null) {
                if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                parent.getChildren().add(node);
            }
        }
    });
    return roots;
}
```

### 6.7 Admin Controller
```java
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo danh mục thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }
}
```

---

## 7) Exception Handling & Validation

### Validation
- `name`: `@NotBlank`, `@Size(min = 2, max = 100)`.
- `slug`: Phải Unique (kiểm tra ở Service).
- `parentId`: Kiểm tra tồn tại và tránh vòng lặp (parentId != id).

### Exceptions
- `ResourceNotFoundException` (404): Khi không tìm thấy ID hoặc Slug.
- `DuplicateResourceException` (409): Khi tên hoặc slug đã tồn tại.
- `InvalidRequestException` (400): Khi logic nghiệp vụ bị vi phạm (ví dụ: vòng lặp category).

---

## 8) Checklist tự test

- [ ] **Tạo mới**: Category/Brand có tự động sinh Slug đúng chuẩn không?
- [ ] **Update**: Khi đổi tên, slug có cập nhật không (tùy yêu cầu business)?
- [ ] **Soft Delete**: Sau khi xóa, record có còn trong DB không? Truy vấn list có bị ẩn đi không?
- [ ] **Category Tree**: API `/tree` có trả về đúng cấu trúc lồng nhau không?
- [ ] **Validation**: Gửi dữ liệu trống, tên quá ngắn... có trả về lỗi 400 và thông báo rõ ràng không?

---

## 9) Checklist review trước khi commit

- [ ] **N+1 Query**: Truy vấn danh sách/cây có bị lỗi N+1 không? (Sử dụng `@EntityGraph` hoặc `JOIN FETCH`).
- [ ] **Auditing**: `createdAt` và `updatedAt` có tự động điền không?
- [ ] **Security**: Admin API đã được chặn bởi Role ADMIN chưa? Public API đã mở chưa?
- [ ] **Clean Code**: Tên biến/hàm có theo chuẩn camelCase không? Có magic string nào không?

---

## 10) Follow-up nâng cấp

- **Caching**: Sử dụng Redis để cache Category Tree vì dữ liệu này ít thay đổi nhưng truy cập nhiều.
- **Image Upload**: Tích hợp Cloudinary để upload Logo Brand và ảnh Category thay vì lưu link String thủ công.
- **Search**: Tích hợp Hibernate Search hoặc Elasticsearch nếu danh sách phình to.
