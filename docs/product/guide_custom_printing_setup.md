# Hướng dẫn triển khai Module Quản lý In ấn (Custom Printing Management)

## 1. Mô tả bài toán
- **Mục tiêu**: Thiết lập hệ thống quản lý các loại chất liệu in và cấu hình giá in dựa trên các thành phần (dòng chữ, logo). Đảm bảo tính linh hoạt khi thay đổi giá hoặc thêm chất liệu mới.
- **Phạm vi**: 
    - Quản lý danh mục chất liệu in (Printing Materials).
    - Cấu hình giá cộng thêm cho các thành phần thiết kế (Text, Logo).

## 2. Thiết kế kỹ thuật (High-level)
- **Kiến trúc**: Module `printing` độc lập, cung cấp API cho cả Admin (quản lý) và Client (lấy bảng giá để hiển thị trên trình editor).
- **Thành phần**:
    - `PrintingMaterial`: Entity lưu thông tin chất liệu (In Decal, In nhiệt...).
    - `PrintingPriceConfig`: Entity lưu đơn giá cho mỗi "dòng chữ" hoặc "logo".
    - `PrintingElementType`: Enum phân loại (`TEXT`, `IMAGE`).

## 3. Thư viện đề xuất
- **Spring Data JPA**: Sử dụng các tính năng chuẩn của JPA.
- **Lombok**: Giảm thiểu code boilerplate cho Entity/DTO.

## 4. Cấu hình cần thêm
- Không cần cấu hình đặc biệt, chỉ cần tạo các table tương ứng trong Database thông qua JPA.

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Khởi tạo Package & Enum
- Tạo package `com.sport_pro_be.modules.printing`.
- Tạo enum `PrintingElementType` với hai giá trị: `TEXT`, `IMAGE`.

### Bước 2: Xây dựng Entity
- Tạo `PrintingMaterial.java` với các trường: `id`, `name`, `description`, `basePrice`, `isActive`.
- Tạo `PrintingPriceConfig.java` với các trường: `id`, `type` (Enum), `unitPrice`, `description`.

### Bước 3: Tạo Repository
- Tạo `PrintingMaterialRepository` và `PrintingPriceConfigRepository`.

### Bước 4: Xây dựng Service & Controller (Admin)
- Triển khai các API CRUD cơ bản để Admin có thể thêm/sửa/xóa chất liệu và giá cấu hình.
- Tuân thủ quy tắc: `interfaces` chứa Interface, `service` chứa Implementation.

### Bước 5: Xây dựng API công khai cho Client
- Tạo endpoint `GET /api/public/printing/configs` để trả về toàn bộ danh sách chất liệu và bảng giá, giúp Frontend tính toán giá real-time cho khách hàng.

## 6. Pseudo-code / code skeleton

### PrintingMaterial.java
```java
@Entity
@Table(name = "printing_materials")
public class PrintingMaterial extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private Boolean isActive = true;
}
```

### PrintingPriceConfig.java
```java
@Entity
@Table(name = "printing_price_configs")
public class PrintingPriceConfig extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private PrintingElementType type; // TEXT or IMAGE
    
    private BigDecimal unitPrice;
}
```

## 7. Exception handling & validation
- `MATERIAL_NOT_FOUND`: Khi khách hàng chọn chất liệu không tồn tại.
- `INVALID_ELEMENT_TYPE`: Khi cấu hình giá sai loại thành phần.
- Đảm bảo `basePrice` và `unitPrice` luôn là số dương.

## 8. Checklist tự test
- [ ] Admin có thể tạo mới một chất liệu in (VD: In Decal - 50.000đ).
- [ ] Admin có thể cập nhật giá cho mỗi dòng chữ (VD: 5.000đ/dòng).
- [ ] API public trả về đúng danh sách và giá đang hoạt động (`isActive = true`).

## 9. Checklist review trước khi commit
- [ ] Các trường tiền tệ sử dụng `BigDecimal` để tránh sai số.
- [ ] Tên package tuân thủ `rule_be.md`.
- [ ] Đã kế thừa `AbstractAuditingEntity` để theo dõi ngày tạo/sửa.

## 10. Follow-up
- Sau khi xong module này, bước tiếp theo sẽ là tích hợp nó vào `OrderItem` để lưu kết quả thiết kế và tính tổng tiền đơn hàng.
