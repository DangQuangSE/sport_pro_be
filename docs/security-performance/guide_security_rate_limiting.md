# Hướng dẫn triển khai: Giới hạn truy cập (Rate Limiting)

## 1. Mô tả bài toán
- **Mục tiêu business**: Bảo vệ hệ thống khỏi các cuộc tấn công Brute-force (vào Login/OTP) và ngăn chặn việc quét dữ liệu (scraping) quá mức, đảm bảo tính ổn định cho server.
- **Phạm vi**:
    - Áp dụng cho các API nhạy cảm: `/auth/login`, `/auth/register`, `/auth/verify-otp`.
    - Giới hạn dựa trên: IP Address hoặc UserId.

## 2. Thiết kế kỹ thuật (High-level)
- **Kiến trúc**: Sử dụng thuật toán **Token Bucket** (thông qua thư viện Bucket4j).
- **Lưu trữ**: Sử dụng **Redis** để lưu trữ trạng thái bucket. Điều này quan trọng vì nếu bạn có nhiều instance server, giới hạn sẽ được áp dụng thống nhất (Distributed Rate Limiting).
- **Thành phần**:
    - `RateLimitFilter` (hoặc `Interceptor`): Kiểm tra quyền truy cập trước khi vào Controller.
    - `RateLimitService`: Logic tính toán token còn lại trong Redis.

## 3. Thư viện đề xuất
- **Bucket4j**: Thư viện Java mạnh mẽ cho rate limiting.
- **Lettuce/Jedis**: Kết nối Redis (Đã có sẵn trong Spring Boot Starter Data Redis).

## 4. Cấu hình cần thêm
- `pom.xml`:
  ```xml
  <dependency>
      <groupId>com.bucket4j</groupId>
      <artifactId>bucket4j-core</artifactId>
      <version>8.x.x</version>
  </dependency>
  ```
- `application.properties`: Cấu hình số lượng request tối đa (VD: 5 requests/phút cho Login).

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Cấu hình Redis
- Đảm bảo Redis đã chạy. Tạo `RedisConfig.java` nếu chưa có để cấu hình `RedisTemplate`.

### Bước 2: Tạo `RateLimitService`
- Sử dụng `ProxyManager` của Bucket4j kết hợp với Redis để quản lý bucket theo Key (IP/UserId).

### Bước 3: Tạo Custom Filter/Interceptor
- Folder: `config/security/filter`
- Logic:
    1. Lấy IP của Client từ Request.
    2. Gọi `RateLimitService` để kiểm tra.
    3. Nếu hết lượt: Trả về 429 Too Many Requests.
    4. Nếu còn lượt: Cho phép đi tiếp.

### Bước 4: Đăng ký Filter vào SecurityConfig
- Gắn Filter vào các URL cụ thể trong chuỗi `SecurityFilterChain`.

## 6. Pseudo-code / Code skeleton

```java
public class RateLimitService {
    public boolean tryConsume(String key) {
        // key có thể là IP hoặc UserId
        Bucket bucket = bucketManager.resolveBucket(key);
        return bucket.tryConsume(1);
    }
}

// Trong Filter
if (!rateLimitService.tryConsume(clientIp)) {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.getWriter().write("Too many requests. Please try again later.");
    return;
}
```

## 7. Exception handling & validation
- HTTP Status: **429 Too Many Requests**.
- Trả về Header `X-Rate-Limit-Remaining` để người dùng biết họ còn bao nhiêu lượt.
- Trả về Header `X-Rate-Limit-Retry-After-Seconds` để biết khi nào được thử lại.

## 8. Checklist tự test
- [ ] Dùng Postman hoặc `curl` gọi liên tiếp vào API Login.
- [ ] Kiểm tra xem sau N lần gọi (theo cấu hình) có bị trả về 429 không.
- [ ] Kiểm tra sau khoảng thời gian cooldown (VD: 1 phút) có truy cập lại được không.
- [ ] Đổi sang IP khác xem có truy cập được bình thường không.

## 9. Checklist review trước khi commit
- [ ] Key trong Redis có được đặt TTL (Time To Live) không? (Tránh làm đầy bộ nhớ Redis).
- [ ] Có xử lý trường hợp Redis die không? (Nên cho phép bypass nếu Redis lỗi để tránh sập hệ thống - Fail-safe).

## 10. Follow-up nâng cấp
- Phân cấp giới hạn: User thường (ít lượt), User VIP (nhiều lượt).
- Chặn IP (Blacklist) vĩnh viễn nếu vi phạm quá nhiều lần.
