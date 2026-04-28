# Tài liệu chức năng Auth: Register + Login + OTP Email

## 1) Mục tiêu

Tài liệu này mô tả lại chức năng đã triển khai trong dự án `sport_pro_be`:

- Đăng ký tài khoản bằng `email + password`
- Đăng nhập bằng `email + password`
- Gửi OTP qua email để xác minh đăng nhập
- Trả về JWT sau khi xác minh OTP thành công

---

## 2) Kiến trúc nhanh

Flow hiện tại:

1. `POST /api/auth/register` tạo user mới (password hash bằng BCrypt).
2. `POST /api/auth/login` kiểm tra email/password, tạo OTP 6 số, lưu DB, gửi qua email.
3. `POST /api/auth/verify-otp` xác minh OTP hợp lệ -> đánh dấu OTP đã dùng -> cập nhật `emailVerified` -> trả JWT.

Các lớp chính:

- Controller: `src/main/java/com/sport_pro_be/auth/controller/AuthController.java`
- Service: `src/main/java/com/sport_pro_be/auth/service/AuthService.java`
- Service gửi mail: `src/main/java/com/sport_pro_be/auth/service/EmailService.java`
- Service tạo JWT: `src/main/java/com/sport_pro_be/auth/service/JwtService.java`
- Entity user: `src/main/java/com/sport_pro_be/auth/domain/User.java`
- Entity OTP: `src/main/java/com/sport_pro_be/auth/domain/EmailOtp.java`
- Repository: `src/main/java/com/sport_pro_be/auth/repository/UserRepository.java`
- Repository OTP: `src/main/java/com/sport_pro_be/auth/repository/EmailOtpRepository.java`
- Cấu hình auth properties: `src/main/java/com/sport_pro_be/config/AuthProperties.java`
- Cấu hình security: `src/main/java/com/sport_pro_be/config/SecurityConfig.java`
- Global exception handler: `src/main/java/com/sport_pro_be/common/ApiExceptionHandler.java`

---

## 3) API Contract

### 3.1 Register

**Endpoint:** `POST /api/auth/register`

Request body:

```json
{
  "email": "demo@sportpro.vn",
  "password": "Password@123"
}
```

Response thành công (`201`):

```json
{
  "message": "Đăng ký thành công"
}
```

Ràng buộc:

- `email`: bắt buộc, đúng định dạng email
- `password`: bắt buộc, từ 8 đến 72 ký tự

---

### 3.2 Login (gửi OTP)

**Endpoint:** `POST /api/auth/login`

Request body:

```json
{
  "email": "demo@sportpro.vn",
  "password": "Password@123"
}
```

Response thành công (`200`):

```json
{
  "message": "OTP đã được gửi vào email của bạn"
}
```

Lưu ý:

- Nếu login sai email/mật khẩu -> trả lỗi `401`
- Có cooldown gửi lại OTP để chống spam (`429` nếu gọi quá nhanh)

---

### 3.3 Verify OTP

**Endpoint:** `POST /api/auth/verify-otp`

Request body:

```json
{
  "email": "demo@sportpro.vn",
  "otp": "123456"
}
```

Response thành công (`200`):

```json
{
  "tokenType": "Bearer",
  "accessToken": "<jwt-token>",
  "expiresInSeconds": 3600,
  "email": "demo@sportpro.vn"
}
```

Ràng buộc:

- `otp` phải đúng 6 chữ số
- OTP phải đúng, chưa dùng, chưa hết hạn

---

## 4) Cấu hình quan trọng (`application.properties`)

```properties
# DB mặc định local
spring.datasource.url=${DB_URL:jdbc:h2:mem:sport_pro_be;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}

# Mail
spring.mail.host=${MAIL_HOST:localhost}
spring.mail.port=${MAIL_PORT:1025}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:false}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS:false}

# Auth / OTP
app.auth.jwt-secret=${APP_JWT_SECRET:change-me-to-a-very-long-secret-key-at-least-32-characters}
app.auth.jwt-expiration-minutes=${APP_JWT_EXP_MINUTES:60}
app.auth.otp-expiration-minutes=${APP_OTP_EXP_MINUTES:5}
app.auth.otp-resend-cooldown-seconds=${APP_OTP_RESEND_SECONDS:60}
app.auth.mail-from=${APP_MAIL_FROM:no-reply@sportpro.local}
```

Bắt buộc production:

- Đặt `APP_JWT_SECRET` tối thiểu 32 ký tự
- Cấu hình SMTP thật (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`)

---

## 5) Quy tắc nghiệp vụ hiện tại

- Email được chuẩn hóa về lowercase trước khi xử lý.
- Password được hash bằng BCrypt, không lưu plain text.
- OTP có TTL (`otp-expiration-minutes`).
- OTP one-time-use (`used=true` sau khi verify).
- Có cooldown resend OTP (`otp-resend-cooldown-seconds`).
- Khi verify OTP thành công lần đầu, `emailVerified=true`.

---

## 6) Mã lỗi thường gặp

- `400`: OTP sai / OTP hết hạn / dữ liệu request không hợp lệ
- `401`: Sai email hoặc mật khẩu
- `409`: Email đã tồn tại khi register
- `429`: Yêu cầu gửi OTP quá nhanh
- `503`: Không gửi được email OTP (SMTP lỗi)

---

## 7) Danh sách dependency đã thêm

Trong `pom.xml` đã bổ sung chính:

- `spring-boot-starter-validation`
- `spring-boot-starter-mail`
- `io.jsonwebtoken:jjwt-api`
- `io.jsonwebtoken:jjwt-impl`
- `io.jsonwebtoken:jjwt-jackson`
- `com.h2database:h2` (runtime local)
- `spring-boot-configuration-processor`

---

## 8) Trạng thái hiện tại

- Chức năng đã được code xong và tích hợp vào project.
- Test Maven gần nhất đã chạy thành công (`BUILD SUCCESS`).
- Có thể tiếp tục nâng cấp theo hướng production:
  - Hash OTP thay vì lưu plain OTP
  - Giới hạn số lần nhập sai OTP
  - Bổ sung refresh token + revoke token
  - Thêm Flyway migration cho bảng auth
