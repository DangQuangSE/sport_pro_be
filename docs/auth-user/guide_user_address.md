# Hướng dẫn triển khai: Sổ địa chỉ (Address Book)

## 1. Mô tả bài toán
- **Mục tiêu business**: Cho phép người dùng lưu trữ nhiều địa chỉ giao hàng khác nhau (Nhà riêng, Cơ quan, v.v.), đặt một địa chỉ làm mặc định để tối ưu quy trình Checkout.
- **Phạm vi**:
    - Thêm, sửa, xóa địa chỉ.
    - Đặt địa chỉ mặc định.
    - Tích hợp vào quy trình đặt hàng (chọn từ danh sách thay vì nhập tay).

## 2. Thiết kế kỹ thuật (High-level)
- **Module mới**: `user_address`
- **Mô hình**: Modular Monolith.
- **Thành phần**:
    - `UserAddress`: Entity chính, quan hệ `@ManyToOne` với `User`.
    - `AddressController`: Các endpoint CRUD.
    - `AddressService`: Logic kiểm tra số lượng địa chỉ tối đa (VD: 5), logic chuyển đổi địa chỉ mặc định.
    - `AddressRepository`: Truy vấn địa chỉ theo `userId`.

## 3. Thư viện đề xuất
- **Không cần thư viện ngoài mới**: Sử dụng Spring Data JPA và Validation hiện có.

## 4. Cấu hình cần thêm
- `AddressMessageConstant.java`: Định nghĩa các thông báo lỗi/thành công.
- Không cần thay đổi `pom.xml`.

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Tạo Entity `UserAddress`
- Folder: `modules/user_address/domain`
- File: `UserAddress.java` (Kế thừa `AbstractAuditingEntity`)
- Tiêu chí: Có các trường: `receiverName`, `phoneNumber`, `province`, `district`, `ward`, `detailAddress`, `isDefault`, `addressType` (HOME, OFFICE, OTHER).

### Bước 2: Tạo Repository & Constant
- Folder: `modules/user_address/repository` & `modules/user_address/constant`
- File: `AddressRepository.java`, `AddressMessageConstant.java`.

### Bước 3: Tạo DTOs
- Folder: `modules/user_address/dto/request` & `modules/user_address/dto/response`
- File: `AddressRequest.java`, `AddressResponse.java`.

### Bước 4: Triển khai Interface & Service
- Folder: `modules/user_address/interfaces` & `modules/user_address/service`
- Logic quan trọng: Khi đặt một địa chỉ làm `isDefault = true`, tất cả địa chỉ khác của user đó phải chuyển về `false`.

### Bước 5: Tạo Controller
- Folder: `modules/user_address/controller`
- Endpoint: `POST /api/v1/addresses`, `GET /api/v1/addresses`, `PUT /api/v1/addresses/{id}`, `DELETE /api/v1/addresses/{id}`, `PATCH /api/v1/addresses/{id}/default`.

## 6. Pseudo-code / Code skeleton

```java
// UserAddress Entity
public class UserAddress extends AbstractAuditingEntity {
    private Long id;
    private User user;
    private String receiverName;
    private String phoneNumber;
    private String province; // Tỉnh/Thành phố
    private String district; // Quận/Huyện
    private String ward;     // Phường/Xã
    private String detailAddress;
    private Boolean isDefault;
    private AddressType type;
}

// Service Logic set Default
@Transactional
public void setDefault(Long addressId, Long userId) {
    // 1. Tìm địa chỉ theo id và userId
    // 2. Set all isDefault = false cho userId này
    // 3. Set isDefault = true cho addressId
    // 4. Save
}
```

## 7. Exception handling & validation
- `ADDRESS_NOT_FOUND`: 404 Not Found.
- `MAX_ADDRESS_REACHED`: 400 Bad Request (nếu giới hạn 5-10 địa chỉ).
- Validation: `@NotBlank` cho tất cả các trường địa chỉ.

## 8. Checklist tự test
- [ ] Thêm địa chỉ mới và kiểm tra `isDefault`.
- [ ] Thêm địa chỉ thứ 2 làm mặc định, kiểm tra địa chỉ 1 có mất mặc định không.
- [ ] Xóa địa chỉ đang là mặc định (Cần chọn địa chỉ khác làm mặc định hoặc báo lỗi).
- [ ] User A không được sửa/xóa địa chỉ của User B.

## 9. Checklist review trước khi commit
- [ ] Check N+1 query khi lấy danh sách địa chỉ.
- [ ] Kiểm tra các thông báo lỗi đã nằm trong `Constant` chưa.
- [ ] Map đúng từ Entity sang Response DTO.

## 10. Follow-up nâng cấp
- Tích hợp API của GHN/GHTK để lấy danh sách Tỉnh/Huyện/Xã chính xác thay vì nhập text.
