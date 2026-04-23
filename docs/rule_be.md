# Quy tắc lập trình Backend (Senior Guidelines)

Là một lập trình viên Backend Senior, dưới đây là những bộ quy tắc cốt lõi được đúc kết từ kinh nghiệm thực chiến nhằm đảm bảo hệ thống **an toàn, dễ bảo trì, hiệu năng cao và có thể mở rộng**.

---

## 1. Clean Code & Naming Conventions
- **Tên mang ý nghĩa (Meaningful Names):** Đặt tên biến, hàm, class thể hiện rõ chức năng. KHÔNG dùng từ viết tắt tối nghĩa (VD: dùng `userRepository` thay vì `ur`).
- **Hàm nhỏ và đơn nhiệm (Single Responsibility):** Một hàm chỉ nên làm **một việc duy nhất**. Nếu hàm dài hơn 30-50 dòng, hãy cân nhắc tách hàm.
- **Tránh Magic Numbers/Strings:** Sử dụng `constant` (hằng số) hoặc `enum` thay vì hardcode số hoặc chuỗi trực tiếp trong code.
- **Quy tắc trinh sát (Boy Scout Rule):** Luôn để lại code sạch hơn so với lúc bạn tìm thấy nó.

## 2. Kiến trúc & Thiết kế (Architecture & Design)
- **SOLID Principles:** Luôn hướng tới các nguyên lý SOLID. Đặc biệt chú trọng Single Responsibility và Dependency Inversion (Sử dụng Interface thay vì Implementation).
- **Controller mỏng, Service dày (Thin Controller, Fat Service):** 
  - Controller chỉ làm nhiệm vụ nhận Request, validate cơ bản, gọi Service và trả về Response.
  - Mọi logic nghiệp vụ (Business Logic) phức tạp phải nằm ở Service.
- **Tách biệt DTO và Entity:** 
  - Không bao giờ trả trực tiếp `Entity` (mapping trực tiếp với DB) ra ngoài API. Luôn map sang `Response DTO`.
  - Không dùng `Entity` làm tham số nhận Request. Luôn dùng `Request DTO`.

## 3. Quản lý lỗi (Error Handling & Logging)
- **Global Exception Handling:** Sử dụng `@ControllerAdvice` hoặc `@RestControllerAdvice` để bắt và xử lý exception tập trung. Tránh việc try-catch lặp đi lặp lại ở mọi nơi.
- **Mã lỗi chuẩn mực:** Tận dụng đúng các HTTP Status Code (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 500 Internal Server Error).
- **Log có ý nghĩa:** 
  - Đừng log tràn lan. Log `INFO` cho các luồng nghiệp vụ quan trọng, `WARN` cho các lỗi có thể bỏ qua/thử lại, và `ERROR` cho lỗi hệ thống kèm theo Stacktrace.
  - Phải giấu (mask) thông tin nhạy cảm trong log (password, thẻ tín dụng, PII).

## 4. Bảo mật (Security)
- **Không tin tưởng dữ liệu đầu vào (Never trust user input):** Luôn Validate DTO (`@Valid`, `@NotBlank`, `@Pattern`...).
- **Nguyên tắc quyền tối thiểu (Least Privilege):** Phân quyền chặt chẽ, user chỉ được làm những gì hệ thống cho phép.
- **Bảo vệ API:** Áp dụng Rate Limiting cho các API nhạy cảm (Login, Register, Send OTP) để chống Brute-force và DDoS.
- **Mã hóa an toàn:** Tuyệt đối không lưu plain-text password. Sử dụng các thuật toán băm mạnh (BCrypt, Argon2).

## 5. Hiệu năng & Cơ sở dữ liệu (Performance & Database)
- **N+1 Query Problem:** Luôn cẩn thận với lỗi N+1 khi truy vấn quan hệ (OneToMany, ManyToMany). Dùng `JOIN FETCH` hoặc `@EntityGraph` trong JPA/Hibernate để giải quyết.
- **Index:** Đánh index trên các cột thường xuyên được dùng trong điều kiện `WHERE`, `JOIN`, hoặc `ORDER BY`.
- **Cẩn thận với `@Transactional`:**
  - Không ôm những logic chạy chậm (gọi API ngoài, gửi email, đọc ghi file lớn) vào trong một block `@Transactional`. Việc này sẽ giữ (hold) Database Connection quá lâu.
  - Transaction nên càng ngắn càng tốt.
- **Phân trang (Pagination):** Mọi API trả về danh sách đều phải có phân trang. Không bao giờ query `findAll()` cho một bảng có khả năng phình to.

## 6. Xử lý bất đồng bộ (Asynchronous Processing)
- Đẩy các tác vụ không cần thiết phải phản hồi ngay lập tức (như gửi email, push notification, tạo report) sang luồng xử lý bất đồng bộ (ví dụ: dùng `@Async` hoặc Message Broker như RabbitMQ, Kafka).

## 7. Testing
- **Viết Test không phải là tùy chọn:** Luôn viết Unit Test cho các logic nghiệp vụ quan trọng trong Service.
- Test phải chạy nhanh, độc lập và có thể chạy lại bất cứ lúc nào (Idempotent).

---
*“Bất kỳ kẻ ngốc nào cũng có thể viết code cho máy tính hiểu. Những lập trình viên giỏi viết code cho con người hiểu.” – Martin Fowler*
