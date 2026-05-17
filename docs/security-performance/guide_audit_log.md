# Hướng dẫn triển khai: Lịch sử hoạt động (Audit Logs)

## 1. Mô tả bài toán
- **Mục tiêu business**: Theo dõi và ghi lại các hành động quan trọng của người dùng (đặc biệt là Admin) như thay đổi giá, xóa sản phẩm, duyệt đơn hàng để phục vụ mục đích bảo mật và tra soát.
- **Phạm vi**:
    - Ghi lại: Ai làm, làm gì, trên đối tượng nào, dữ liệu cũ/mới (nếu cần), IP, thời gian.
    - Chỉ áp dụng cho các API thay đổi dữ liệu (POST, PUT, DELETE).

## 2. Thiết kế kỹ thuật (High-level)
- **Kiến trúc**: Sử dụng **AOP (Aspect Oriented Programming)** để tự động ghi log mà không can thiệp vào logic của Service.
- **Thành phần**:
    - `@Loggable`: Annotation tùy chỉnh để đánh dấu method cần ghi log.
    - `AuditAspect`: Xử lý logic capture dữ liệu trước/sau khi method thực thi.
    - `AuditLog`: Entity lưu trữ thông tin log.
    - `AuditLogService`: Lưu log vào Database bất đồng bộ (`@Async`) để không làm chậm request chính.

## 3. Thư viện đề xuất
- **Spring AOP**: Có sẵn trong `spring-boot-starter-aop`.

## 4. Cấu hình cần thêm
- `pom.xml`: Đảm bảo có `spring-boot-starter-aop`.
- `AsyncConfig.java`: Kích hoạt `@EnableAsync` (Đã có trong dự án, cần kiểm tra pool size).

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Tạo Entity `AuditLog`
- Folder: `modules/audit/domain`
- Các trường: `id`, `userId`, `email`, `action` (VD: UPDATE_PRODUCT), `resource` (VD: PRODUCT), `resourceId`, `payload` (JSON dữ liệu thay đổi), `status` (SUCCESS/FAIL), `ipAddress`, `timestamp`.

### Bước 2: Tạo Annotation `@Loggable`
- Folder: `modules/audit/interfaces`
- Chứa các thuộc tính như `action` và `resource`.

### Bước 3: Triển khai `AuditAspect`
- Folder: `modules/audit/aspect`
- Sử dụng `@Around("@annotation(loggable)")`.
- Lấy thông tin user từ `SecurityContextHolder`.
- Lấy thông tin Request (IP) từ `RequestContextHolder`.

### Bước 4: Tạo Service lưu log bất đồng bộ
- Folder: `modules/audit/service`
- Method `saveLog` phải đánh dấu `@Async` để tránh blocking.

### Bước 5: Gắn Annotation vào các Service cần theo dõi
- Ví dụ: `ProductService.updateProduct`, `OrderService.updateStatus`.

## 6. Pseudo-code / Code skeleton

```java
@Aspect
@Component
public class AuditAspect {
    @Around("@annotation(loggable)")
    public Object trace(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        // 1. Trước khi thực thi: Lấy dữ liệu input, IP, User
        Object result = joinPoint.proceed();
        // 2. Sau khi thực thi: Lưu log SUCCESS
        auditLogService.saveLog(..., "SUCCESS");
        return result;
    }
}

@Service
public class AuditLogService {
    @Async
    public void saveLog(AuditLog log) {
        auditLogRepository.save(log);
    }
}
```

## 7. Exception handling & validation
- Nếu việc lưu log thất bại (ví dụ lỗi DB), KHÔNG được làm fail request chính của người dùng. Cần bao bọc `saveLog` trong try-catch và log lỗi ra console/file.

## 8. Checklist tự test
- [ ] Thực hiện Update sản phẩm, kiểm tra table `audit_logs` có dữ liệu không.
- [ ] Kiểm tra trường `email` và `userId` có đúng người thực hiện không.
- [ ] Kiểm tra `ipAddress` có được capture đúng không.
- [ ] Test trường hợp Service chính lỗi, Audit Log có ghi nhận trạng thái `FAIL` không.

## 9. Checklist review trước khi commit
- [ ] Đã đánh dấu `@Async` chưa?
- [ ] Có mask các dữ liệu nhạy cảm (password) trong payload không?
- [ ] Entity `AuditLog` có Index cho `userId` và `timestamp` chưa?

## 10. Follow-up nâng cấp
- Lưu log vào **Elasticsearch** hoặc **MongoDB** thay vì MySQL nếu số lượng log quá lớn (Big Data).
- Tạo giao diện cho Admin để tra cứu log.
