# Hướng dẫn triển khai Dashboard Thống kê cho Admin (Admin Analytics & Reports)

## 1. Mô tả bài toán
### Mục tiêu business
Cung cấp cho Admin cái nhìn tổng quan về tình hình kinh doanh của hệ thống Sport Pro thông qua các chỉ số quan trọng (KPIs) như doanh thu, sản phẩm bán chạy, xu hướng thiết kế và tỉ lệ vận hành đơn hàng.

### Phạm vi
- **In-scope**: 
  - API thống kê doanh thu theo ngày/tháng.
  - API thống kê Top 10 sản phẩm bán chạy nhất.
  - API thống kê Xu hướng Custom Design (mẫu thiết kế được đặt nhiều nhất).
  - API thống kê Tỷ lệ đơn hàng (Thành công, Hủy, Hoàn).
- **Out-scope**: 
  - Đồ thị (Chart) phía Frontend (API chỉ trả về data thô).
  - Xuất file Excel/PDF (tính năng nâng cao).

---

## 2. Thiết kế kỹ thuật
### Kiến trúc flow
- **Client (Admin Portal)** -> **AnalyticsController** -> **AnalyticsService** -> **Repositories (Order, OrderItem, CustomDesign)**.
- Sử dụng **JPQL** hoặc **Native Query** để tối ưu hóa việc aggregation (SUM, COUNT, GROUP BY) thay vì lấy toàn bộ list về rồi xử lý trong Java.

### Thành phần liên quan
- **Module mới**: `modules/analytics`
- **Controller**: `AnalyticsController`
- **Interface**: `IAnalyticsService`
- **Service**: `AnalyticsService`
- **DTOs**: `RevenueReportResponse`, `TopProductResponse`, `OrderStatsResponse`, `TrendingDesignResponse`.

---

## 3. Thư viện đề xuất
- Không cần thư viện ngoài mới. Tận dụng **Spring Data JPA** và **Java Stream API**.

---

## 4. Cấu hình cần thêm
- Không yêu cầu cấu hình mới trong `pom.xml` hay `application.properties`.

---

## 5. Kế hoạch triển khai code tay (step-by-step)

### Bước 1: Tạo các DTO cho Response
- Tạo package `com.sport_pro_be.modules.analytics.dto`.
- Định nghĩa các record/class để chứa kết quả thống kê.
- *Tiêu chí hoàn thành*: Các DTO bao quát được các trường dữ liệu cần trả về.

### Bước 2: Tạo Interface IAnalyticsService
- Tạo package `com.sport_pro_be.modules.analytics.interfaces`.
- Định nghĩa các phương thức: `getRevenueReport`, `getTopSellingProducts`, `getTrendingDesigns`, `getOrderStatistics`.
- *Tiêu chí hoàn thành*: Interface rõ ràng, đúng naming convention.

### Bước 3: Triển khai AnalyticsService
- Tạo package `com.sport_pro_be.modules.analytics.service`.
- Inject `OrderRepository`, `OrderItemRepository`, `CustomDesignRepository`.
- Viết logic aggregation. Lưu ý sử dụng `@Query` trong Repository nếu logic SQL phức tạp.
- *Tiêu chí hoàn thành*: Logic xử lý được các trường hợp không có dữ liệu (trả về 0 hoặc list rỗng).

### Bước 4: Tạo AnalyticsController
- Tạo package `com.sport_pro_be.modules.analytics.controller`.
- Định nghĩa các endpoint `@GetMapping`.
- Sử dụng `@PreAuthorize("hasRole('ADMIN')")` để bảo mật.
- *Tiêu chí hoàn thành*: API trả về dữ liệu đúng cấu trúc `ApiResponse`.

---

## 6. Pseudo-code / code skeleton

### AnalyticsController
```java
@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final IAnalyticsService analyticsService;

    @GetMapping("/revenue")
    public ApiResponse<List<RevenueReportResponse>> getRevenue(...) {
        // Gọi service lấy doanh thu theo khoảng thời gian
    }

    @GetMapping("/top-products")
    public ApiResponse<List<TopProductResponse>> getTopProducts(...) {
        // Thống kê sản phẩm bán chạy
    }
}
```

### AnalyticsService Logic (Example Query)
```java
// Ví dụ Query trong OrderRepository cho doanh thu
@Query("SELECT new com...RevenueReportResponse(CAST(o.createdAt AS date), SUM(o.totalAmount)) " +
       "FROM Order o WHERE o.status = 'DELIVERED' " +
       "AND o.createdAt BETWEEN :start AND :end " +
       "GROUP BY CAST(o.createdAt AS date)")
List<RevenueReportResponse> calculateDailyRevenue(LocalDateTime start, LocalDateTime end);
```

---

## 7. Exception handling & validation
- `400 Bad Request`: Nếu `startDate` > `endDate`.
- `403 Forbidden`: Nếu User không phải Admin.
- Message hằng số đặt tại `AnalyticsMessageConstant.java`.

---

## 8. Checklist tự test
- [ ] Happy path: Admin lấy được thống kê khi có dữ liệu.
- [ ] No data: Hệ thống không crash khi bảng `orders` rỗng.
- [ ] Time range: Thống kê đúng trong khoảng ngày được chọn.
- [ ] Status filter: Doanh thu chỉ tính trên đơn hàng `DELIVERED`.

---

## 9. Checklist review trước khi commit
- [ ] Check N+1 query (Sử dụng aggregation SQL thay vì loop Java).
- [ ] Check quyền truy cập (Chỉ Admin).
- [ ] Format code, xóa import thừa.

---

## 10. Follow-up nâng cấp
- Caching kết quả thống kê (Spring Cache) nếu dữ liệu lớn để tăng tốc độ phản hồi.
- Hỗ trợ xuất dữ liệu ra CSV.
