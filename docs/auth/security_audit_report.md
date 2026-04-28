
Báo cáo chi tiết về các lỗ hổng bảo mật được phát hiện trong module `@auth` và các bước khắc phục để chuẩn bị cho môi trường **Production**.

## 1. Bảng tổng hợp các vấn đề (Summary)

| ID | Vấn đề | Mức độ | Trạng thái |
| :--- | :--- | :--- | :--- |
| **SEC-01** | Lộ thông tin người dùng qua Email Enumeration | Trung bình | Cần khắc phục |
| **SEC-02** | Lỗ hổng Brute Force trên endpoint Đăng nhập | **Nghiêm trọng** | Cần khắc phục |
| **SEC-03** | Token Reset Password có thể tái sử dụng (JWT stateless) | Trung bình | Cần khắc phục |
| **SEC-04** | Cấu hình Cookie thiếu thuộc tính `Secure` | **Cao** | Cần khắc phục |
| **SEC-05** | Nguy cơ DoS Database tại Filter xác thực | Thấp | Cần tối ưu |
| **SEC-06** | Rủi ro CSRF đối với Refresh Token Cookie | Thấp/Trung bình | Cần lưu ý |

---

## 2. Chi tiết các lỗ hổng và Cách khắc phục

### SEC-01: Lộ thông tin người dùng (Email Enumeration)
- **Mô tả:** Tại `AuthService.requestRegistrationOtp`, khi email đã tồn tại, hệ thống ném ra lỗi `409 Conflict`.
- **Nguy cơ:** Kẻ tấn công có thể dùng danh sách email thu thập được để quét và biết được ai đang sử dụng dịch vụ của bạn.
- **Cách khắc phục:** 
    - Luôn trả về `200 OK` với thông báo trung lập: *"Nếu email của bạn hợp lệ, một mã OTP đã được gửi."*
    - Thực hiện gửi email thông báo "Tài khoản đã tồn tại" nếu email đã có trong hệ thống thay vì báo lỗi trực tiếp qua API.

### SEC-02: Lỗ hổng Brute Force (Tấn công vét cạn)
- **Mô tả:** Endpoint `/api/auth/login` không có giới hạn số lần thử sai.
- **Nguy cơ:** Kẻ tấn công có thể dùng bot để thử hàng nghìn mật khẩu cho một tài khoản cho đến khi thành công.
- **Cách khắc phục:**
    - Sử dụng thư viện như **Bucket4j** hoặc **Spring Boot Rate Limiter** để giới hạn số lần gọi API login trên mỗi IP/Email.
    - Triển khai cơ chế "Lock Account" (khóa tài khoản) sau 5-10 lần nhập sai mật khẩu liên tiếp.

### SEC-03: Token Reset Password tái sử dụng
- **Mô tả:** JWT dùng để reset mật khẩu không được thu hồi sau lần sử dụng đầu tiên.
- **Nguy cơ:** Nếu token bị lộ (qua log, browser history...), kẻ tấn công có thể đổi lại mật khẩu của người dùng nhiều lần cho đến khi token hết hạn (15 phút).
- **Cách khắc phục:**
    - Thay vì dùng JWT, hãy lưu một mã token ngẫu nhiên (UUID) vào Database kèm trường `isUsed`.
    - Sau khi đổi mật khẩu thành công, đánh dấu token đó là đã sử dụng.

### SEC-04: Cấu hình Cookie thiếu thuộc tính `Secure`
- **Mô tả:** Thuộc tính `refreshTokenCookieSecure` đang mặc định là `false`.
- **Nguy cơ:** Cookie chứa Refresh Token có thể bị gửi qua kết nối HTTP không mã hóa, dẫn đến bị bắt trộm (Man-in-the-middle).
- **Cách khắc phục:**
    - Thiết lập mặc định là `true` trong `AuthProperties`.
    - Đảm bảo môi trường Production chạy trên **HTTPS**.

### SEC-05: Nguy cơ DoS Database (Performance)
- **Mô tả:** `JwtAuthenticationFilter` truy vấn DB để kiểm tra `tokenVersion` trên **mọi request**.
- **Nguy cơ:** Kẻ tấn công gửi hàng loạt request giả mạo (thậm chí với token hợp lệ) khiến Database bị quá tải do số lượng truy vấn tăng đột biến.
- **Cách khắc phục:**
    - Sử dụng **Redis** để lưu trữ thông tin User hoặc ít nhất là `tokenVersion` với thời gian sống bằng thời gian sống của Access Token.
    - Chỉ truy vấn DB khi dữ liệu trong Cache không tồn tại.

### SEC-06: Rủi ro CSRF (Cross-Site Request Forgery)
- **Mô tả:** `csrf().disable()` trong `SecurityConfig` trong khi vẫn dùng Cookie cho Refresh Token.
- **Nguy cơ:** Một trang web độc hại có thể kích hoạt request `/api/auth/refresh-token` của người dùng (vì trình duyệt tự gửi cookie).
- **Cách khắc phục:**
    - Mặc dù Access Token nằm ở Header nên an toàn, nhưng tốt nhất nên cấu hình `SameSite=Strict` cho Cookie.
    - Nếu có thể, hãy bật CSRF và cấu hình `CookieCsrfTokenRepository`.

---

## 3. Danh sách kiểm tra (Checklist) cho Production

- [ ] Thay đổi `jwtSecret` thành một chuỗi ngẫu nhiên dài ít nhất 32 ký tự và lưu trong biến môi trường.
- [ ] Bật `app.auth.refresh-token-cookie-secure=true`.
- [ ] Cấu hình Mail Server thực tế (thay vì dùng log/dev).
- [ ] Thiết lập giới hạn Rate Limit (ví dụ: tối đa 5 request login/phút cho mỗi IP).
- [ ] Chuyển đổi cơ chế Reset Password từ JWT sang Database-stored token.
- [ ] (Nâng cao) Triển khai Logging/Audit để theo dõi các hành vi đăng nhập bất thường.

---
*Báo cáo được thực hiện bởi Antigravity AI.*
