# Lý Thuyết Spring Boot Chuyên Sâu - Module Auth & Security

Tài liệu này không chỉ giải thích code trong dự án, mà còn phân tích nguồn gốc, lý do ra đời của từng công nghệ, và cách người ta từng làm việc trước khi có những công nghệ này.

---

## 1. Global Exception Handling (`@RestControllerAdvice` / `@ExceptionHandler`)

### 📖 Định nghĩa
Đây là một cơ chế hoạt động dựa trên mô hình **AOP (Aspect-Oriented Programming - Lập trình hướng khía cạnh)**. `@RestControllerAdvice` đóng vai trò là một "Interceptor" (kẻ đánh chặn). Bất cứ khi nào một ngoại lệ (Exception) bị ném ra từ bất kỳ Controller nào trong toàn bộ ứng dụng, nó sẽ bị cơ chế này "tóm lại" và chuyển đến hàm xử lý tương ứng (`@ExceptionHandler`).

### 💡 Lý do ra đời
Trong lập trình Web API, việc trả về một thông báo lỗi chuẩn hóa (ví dụ: HTTP 400 kèm file JSON báo lỗi rõ ràng) là bắt buộc. Nếu không có cơ chế chặn lỗi toàn cục, các lập trình viên sẽ phải viết khối `try-catch` lặp đi lặp lại ở mọi endpoint trong mọi Controller. Điều này dẫn đến mã nguồn bị rác, khó bảo trì, và đôi khi quên bắt lỗi sẽ khiến server trả về một trang HTML 500 trắng xóa hoặc lộ chi tiết stacktrace ra ngoài (rất nguy hiểm về bảo mật).

### 🕰️ Kỷ nguyên trước khi có nó
**Chúng ta từng sử dụng:**
```java
// Ngày xưa: Lặp lại try-catch ở mọi nơi
@GetMapping("/api/users")
public ResponseEntity<?> getUsers() {
    try {
        userService.doSomething();
        return ResponseEntity.ok(data);
    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(404).body("Not found");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error");
    }
}
```
**Nhược điểm:** Code lặp lại quá nhiều, định dạng lỗi trả về không đồng nhất giữa các team làm chung dự án.

### 🚀 Áp dụng trong dự án `sport_pro_be`
- Chúng ta có file `ApiExceptionHandler.java`. Khi ở tầng Auth (hay bất kỳ đâu), nếu thông tin không hợp lệ, ta chỉ việc quăng lỗi: `throw new UnauthorizedException("Sai mật khẩu");`.
- Tiến trình sẽ dừng ngay lập tức, văng ra ngoài và bị `@ExceptionHandler(AppException.class)` đón lấy, bọc vào một JSON `ApiResponse` xinh đẹp và trả về cho người dùng HTTP 401.

---

## 2. Spring IoC, Cấu hình (`@Configuration`) và Bean

### 📖 Định nghĩa
- **Bean**: Là các đối tượng do **Spring IoC Container** tạo ra, quản lý vòng đời và tự động tiêm (inject) vào những nơi cần thiết.
- **`@Configuration`**: Là annotation đánh dấu một class chứa các định nghĩa để tạo ra Bean.

### 💡 Lý do ra đời
Khái niệm "Inversion of Control (IoC)" (Đảo ngược quyền điều khiển) ra đời để giải quyết bài toán phụ thuộc cứng (Tight Coupling). Thay vì Class A phải tự `new ClassB()` để dùng, thì Class A chỉ cần khai báo "Tôi cần ClassB", hệ thống sẽ tự động tìm và bơm (Inject) Class B vào. `@Configuration` giúp Spring biết phải tìm các định nghĩa tạo Bean này ở đâu trong mã nguồn Java (Java-based configuration) một cách an toàn và dễ kiểm tra kiểu (Type-safe).

### 🕰️ Kỷ nguyên trước khi có nó
Trước khi có `@Configuration` (Spring 2.x trở về trước), mọi Bean đều phải được định nghĩa trong các **File cấu hình XML** khổng lồ và rối rắm.
```xml
<!-- Ngày xưa: Rất khó debug vì không báo lỗi lúc biên dịch -->
<bean id="passwordEncoder" class="org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder"/>
```
Nếu gõ sai tên class, chỉ khi chạy chương trình (Runtime) ứng dụng mới chết. Với `@Configuration`, nếu gõ sai, IDE sẽ báo lỗi ngay lập tức lúc đang code.

### 🚀 Áp dụng trong dự án
- Nằm trong `SecurityConfig.java`. Lớp này chứa các phương thức có `@Bean` để báo cho Spring biết: "Hãy chạy hàm này, lấy kết quả trả về bỏ vào kho Bean của hệ thống để dùng chung".

---

## 3. `SecurityFilterChain` (Bảo mật cốt lõi)

### 📖 Định nghĩa
`SecurityFilterChain` là một chuỗi các "màng lọc" (filters) mà bất cứ HTTP Request nào gửi vào server cũng đều phải đi qua trước khi chạm được tới Controller của bạn.

### 💡 Lý do ra đời
Dựa trên kiến trúc "Filter" của Java Servlet. Việc xác thực (Authentication - Mày là ai?) và phân quyền (Authorization - Mày được làm gì?) phải được thực hiện *TRƯỚC* khi đụng vào logic nghiệp vụ. Filter Chain ra đời để lắp ghép nhiều bộ lọc bảo mật lại với nhau một cách tuần tự và linh hoạt.

### 🕰️ Kỷ nguyên trước khi có nó
- **Thời Cổ Đại**: Tự viết các lệnh if-else check session ở ngay đầu từng hàm Controller (rất dễ dính lỗ hổng do quên check).
- **Thời Spring Cũ (trước Spring Boot 2.7 / Security 5.7)**: Chúng ta dùng class kế thừa `WebSecurityConfigurerAdapter` và ghi đè (override) phương thức `configure(HttpSecurity)`. Nhưng nó bị phản đối (deprecated) do kiến trúc kế thừa cứng nhắc. Việc dùng `SecurityFilterChain` dưới dạng Bean (Composition) giúp code độc lập và tuân thủ thiết kế hiện đại tốt hơn.

### 🚀 Áp dụng trong dự án
- Tắt CSRF (`csrf().disable()`) vì chúng ta dùng cơ chế Stateless (API thuần túy, không dùng Session Cookie của Browser).
- Chặn phân quyền URL: Các url `/api/auth/**` thì `permitAll()` (Ai cũng vô được).
- Xử lý ngoại lệ bảo mật: cấu hình `authenticationEntryPoint` để hứng lỗi 401 (chưa đăng nhập) và `accessDeniedHandler` để hứng lỗi 403 (không đủ quyền) bằng JSON thay vì trả về trang HTML "White label error page" vô hồn của Spring mặc định.

---

## 4. `PasswordEncoder` (BCrypt)

### 📖 Định nghĩa
`BCryptPasswordEncoder` là thuật toán băm (hashing) mật khẩu một chiều mạnh mẽ. Nó tự động sinh ra một "salt" (muối - một chuỗi ngẫu nhiên) trộn chung với mật khẩu trước khi băm.

### 💡 Lý do ra đời
Để đối phó với nguy cơ hacker đánh cắp toàn bộ Database. Nếu lưu mật khẩu thật, user sẽ mất tài khoản. Nếu băm mật khẩu cơ bản (không có salt), hacker sẽ dùng kỹ thuật "Rainbow Tables" (Bảng dò tìm từ điển băm sẵn) để dò ngược ra mật khẩu rất nhanh.

### 🕰️ Kỷ nguyên trước khi có nó
- **Cổ đại**: Lưu mật khẩu dạng chuỗi văn bản thuần (Plain-text) "123456" vào cột DB.
- **Trung đại**: Dùng MD5 hoặc SHA-1. Đây là các thuật toán tốc độ quá nhanh, ban đầu dùng để checksum (kiểm tra tính toàn vẹn file), nên hacker có thể dùng GPU để đoán hàng tỷ mật khẩu/giây. Hơn nữa do thiếu "Salt" mặc định, 2 người cùng đặt pass "123456" sẽ ra chuỗi mã hóa y hệt nhau -> Lộ bài.

### 🚀 Áp dụng trong dự án
Khai báo dưới dạng Bean. Khi Đăng ký: Gọi `passwordEncoder.encode("matKhau123")` để lưu DB (Mỗi lần gọi encode cùng 1 chuỗi nó sẽ ra một kết quả Hash dài ngoằng khác nhau nhờ Salt). Khi Đăng nhập: Gọi `passwordEncoder.matches("matKhau123", hashTrongDB)` để kiểm tra.

---

## 5. `OncePerRequestFilter` (JwtAuthenticationFilter)

### 📖 Định nghĩa
Một class con của Spring giúp đảm bảo đoạn logic nằm bên trong nó CHỈ được thực thi ĐÚNG 1 LẦN cho mỗi một Request (từ lúc Client gửi lên cho tới lúc nhận về).

### 💡 Lý do ra đời
Kiến trúc Java Servlet mặc định có class `Filter`. Tuy nhiên, trong một số tình huống (như Request Dispatcher forward request nội bộ sang một endpoint khác để xử lý lỗi), bộ lọc `Filter` gốc có thể bị kích hoạt chạy lại lần thứ 2, thứ 3. Điều này gây tốn tài nguyên và sai logic bảo mật. Spring tạo ra `OncePerRequestFilter` để "nhớ" xem filter này đã chạy cho request này chưa, nếu chạy rồi thì bỏ qua.

### 🕰️ Kỷ nguyên trước khi có nó
Các lập trình viên phải tự khai báo `implements Filter` của `javax.servlet`, sau đó phải tự viết logic cờ hiệu (flag) `request.setAttribute("DA_CHAY", true)` để chặn filter chạy 2 lần, rất thủ công.

### 🚀 Áp dụng trong dự án
- Nằm trong `JwtAuthenticationFilter`. Nó đứng chắn ở đầu ngõ, "bóc" Header `Authorization` lấy Token JWT.
- Token JWT (JSON Web Token) tự nó chứa được thông tin `email` và `roles`, nên ta không cần cấp phát Session trên RAM server. 
- Filter này kiểm tra Version Token (Versioning). Nếu version trong JWT trùng khớp DB, ta đóng mộc `UsernamePasswordAuthenticationToken` và ném vào `SecurityContextHolder`. Kể từ giây phút đó, hệ thống mặc định User này đã là người "Hợp Pháp".
