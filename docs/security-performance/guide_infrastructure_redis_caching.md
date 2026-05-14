# Hướng dẫn triển khai: Caching (Redis)

## 1. Mô tả bài toán
- **Mục tiêu business**: Tăng tốc độ phản hồi của API cho các dữ liệu ít thay đổi nhưng được truy cập thường xuyên (như Danh mục, Thương hiệu, Chi tiết sản phẩm), đồng thời giảm tải cho Database.
- **Phạm vi**:
    - Cache danh sách Category, Brand.
    - Cache chi tiết Product theo Slug/Id.
    - Tự động xóa cache (Evict) khi dữ liệu bị thay đổi.

## 2. Thiết kế kỹ thuật (High-level)
- **Kiến trúc**: Sử dụng **Spring Cache Abstraction** kết hợp với **Redis** làm Provider.
- **Cơ chế**:
    - `Cache-Aside Pattern`: Kiểm tra Redis trước -> Nếu có (Hit) thì trả về ngay -> Nếu không (Miss) thì vào DB lấy dữ liệu và lưu vào Redis cho lần sau.
- **Thành phần**:
    - `RedisConfig`: Cấu hình TTL (thời gian sống) cho từng loại dữ liệu.
    - `@Cacheable`, `@CachePut`, `@CacheEvict`: Các annotation của Spring để quản lý cache.

## 3. Thư viện đề xuất
- **spring-boot-starter-data-redis**: Thư viện chính chủ của Spring Boot.

## 4. Cấu hình cần thêm
- `pom.xml`:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
  </dependency>
  ```
- `application.properties`:
  ```properties
  spring.data.redis.host=localhost
  spring.data.redis.port=6379
  spring.cache.type=redis
  ```

## 5. Kế hoạch triển khai code tay (Step-by-step)

### Bước 1: Kích hoạt Caching
- Gắn `@EnableCaching` vào class Main hoặc `RedisConfig`.

### Bước 2: Cấu hình `RedisCacheManager`
- Folder: `config`
- File: `RedisConfig.java`
- Định nghĩa các "Cache Name" với TTL khác nhau (VD: categories: 24h, products: 1h).

### Bước 3: Áp dụng vào Service (Read)
- Tại `CategoryService.getAllCategories`, gắn `@Cacheable(value = "categories", key = "'all'")`.
- Tại `ProductService.getProductBySlug`, gắn `@Cacheable(value = "products", key = #slug)`.

### Bước 4: Xử lý làm mới Cache (Update/Delete)
- Tại `ProductService.updateProduct`, gắn `@CacheEvict(value = "products", key = #request.slug)` hoặc xóa toàn bộ list products.

## 6. Pseudo-code / Code skeleton

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình mặc định: Serialize sang JSON thay vì Binary (để dễ debug)
        // Cấu hình TTL cho từng cache name
    }
}

// Trong Service
@Cacheable(value = "categories", key = "'list'")
public List<CategoryResponse> getAll() {
    return repository.findAll()...
}

@CacheEvict(value = "categories", allEntries = true)
public void createCategory(...) {
    repository.save(...);
}
```

## 7. Exception handling & validation
- **Redis Connection Fail**: Hệ thống phải tự động fallback về DB (không được làm sập ứng dụng). Cấu hình một `CacheErrorHandler` tùy chỉnh để xử lý việc này.

## 8. Checklist tự test
- [ ] Gọi API lấy danh sách Category lần 1: Check log SQL (Phải có query DB).
- [ ] Gọi API lần 2: Check log SQL (Không được có query DB).
- [ ] Dùng lệnh `redis-cli KEYS *` để xem dữ liệu có trong Redis không.
- [ ] Thực hiện cập nhật Category và gọi lại API: Dữ liệu trả về phải là dữ liệu mới.

## 9. Checklist review trước khi commit
- [ ] Dữ liệu lưu trong Redis có phải là JSON không? (Nên dùng `GenericJackson2JsonRedisSerializer`).
- [ ] Đã cấu hình TTL chưa? (Không được để cache sống vĩnh viễn gây tràn bộ nhớ).
- [ ] Đã có `@CacheEvict` ở các hàm xóa/sửa chưa?

## 10. Follow-up nâng cấp
- Sử dụng **Redis Pub/Sub** để thông báo xóa cache nếu bạn triển khai Microservices.
- Áp dụng **Redisson** để sử dụng các tính năng nâng cao như Distributed Lock.
