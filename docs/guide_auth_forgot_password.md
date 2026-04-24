# Hướng dẫn code tay tính năng Forgot Password cho `sport_pro_be`

## 1) Mô tả bài toán

### Mục tiêu business

- Cho phép người dùng đặt lại mật khẩu khi quên mà không cần liên hệ thủ công.
- Giảm rủi ro takeover tài khoản bằng cơ chế token/OTP có hạn dùng, giới hạn thử sai, và chống spam request.
- Đảm bảo UX mượt: flow rõ ràng, thông điệp nhất quán, không lộ thông tin tài khoản tồn tại hay không.

### In-scope

- API yêu cầu quên mật khẩu (`request reset`).
- API xác thực mã reset + đổi mật khẩu (`confirm reset`).
- Cơ chế gửi email reset bất đồng bộ.
- Cơ chế hết hạn/thu hồi mã cũ và rate-limit cơ bản theo email.
- Thu hồi toàn bộ refresh token/session sau khi đổi mật khẩu thành công.

### Out-scope

- Không đổi cơ chế login hiện tại ngoài phần invalidate session sau reset.
- Không thêm MFA bắt buộc trong scope đầu tiên.
- Không thêm giao diện frontend (chỉ backend APIs + contract).

---

## 2) Thiết kế kỹ thuật (High-level)

### Luồng nghiệp vụ đề xuất

1. User nhập email tại “Forgot Password”.
2. Backend luôn trả về message trung tính (để tránh user enumeration), nhưng chỉ tạo mã reset nếu email tồn tại.
3. Tạo reset OTP/token mới, vô hiệu hóa mã cũ chưa dùng của email đó.
4. Gửi email reset async.
5. User gửi `email + resetCode + newPassword` để xác nhận đổi mật khẩu.
6. Backend kiểm tra: mã mới nhất, chưa dùng, chưa hết hạn, chưa vượt số lần sai.
7. Hợp lệ -> cập nhật password hash, đánh dấu mã đã dùng, revoke refresh tokens/session.

### Thành phần liên quan

- `auth/controller/AuthController`
  - `POST /api/auth/forgot-password/request`
  - `POST /api/auth/forgot-password/confirm`
- `auth/service/AuthService` hoặc tách `PasswordResetService` (khuyến nghị tách để SRP).
- `auth/domain`
  - Tạo entity mới `PasswordResetOtp` (hoặc tái sử dụng `EmailOtp` nhưng thêm purpose enum).
- `auth/repository`
  - `PasswordResetOtpRepository` với query lấy OTP mới nhất + invalidate mã cũ.
- `auth/service/EmailService`
  - Gửi mail template reset password.
- `common/ApiExceptionHandler`
  - Dùng chung chuẩn lỗi hiện có.

### Data model đề xuất

`password_reset_otps`

- `id`
- `email`
- `resetCode`
- `used`
- `attemptCount`
- `expiresAt`
- `createdAt`

Index gợi ý:

- `(email, created_at desc)`
- `email`

---

## 3) Thư viện đề xuất

### Bắt buộc

- Không cần thêm third-party library mới.
- Dùng:
  - Spring Boot Validation
  - Spring Data JPA
  - Spring Mail
  - Spring Async (`@Async`) đã có trong dự án

### Tùy chọn nâng cao (phase sau)

- Bucket4j/Redis rate limit cho endpoint forgot password.
- Audit logging framework để giám sát security events.

---

## 4) Cấu hình cần thêm

### `src/main/resources/config/auth.properties`

Đề xuất thêm:

- `app.auth.password-reset-otp-expiration-minutes=10`
- `app.auth.password-reset-resend-cooldown-seconds=60`
- `app.auth.password-reset-max-attempts=5`
- `app.auth.password-reset-email-subject=[Sport Pro] Password reset code`

### `AuthProperties` (`config/AuthProperties.java`)

- Bổ sung fields tương ứng và `@Min` validation.

### Dev/Prod gợi ý

- Dev: expiration 10–15 phút, cooldown 30–60 giây.
- Prod: expiration 10 phút, cooldown 60–120 giây, attempt max 5.

---

## 5) Kế hoạch triển khai code tay (step-by-step)

### Bước 1: Chuẩn hóa API contract + DTO

**Files**

- Tạo `auth/dto/ForgotPasswordRequest.java` (email)
- Tạo `auth/dto/ForgotPasswordConfirmRequest.java` (email, code, newPassword)

**Done criteria**

- DTO có validation đầy đủ (`@Email`, `@NotBlank`, `@Size`, `@Pattern`).

### Bước 2: Tạo entity + repository cho reset OTP

**Files**

- Tạo `auth/domain/PasswordResetOtp.java`
- Tạo `auth/repository/PasswordResetOtpRepository.java`

**Done criteria**

- Có method:
  - tìm OTP mới nhất theo email
  - invalidate mã cũ chưa dùng

### Bước 3: Implement service request reset

**Files**

- Sửa/tạo service trong `auth/service/`

**Logic**

- Normalize email.
- Không lộ email tồn tại/không tồn tại.
- Nếu user tồn tại: check cooldown, invalidate old codes, tạo code mới, gửi mail async.
- Nếu không tồn tại: trả message trung tính giống hệt.

**Done criteria**

- Endpoint luôn trả message chung, không phân biệt account exists.

### Bước 4: Implement service confirm reset

**Logic**

- Query OTP mới nhất theo email.
- Validate used/expired/code/attemptCount.
- Sai code -> tăng `attemptCount`, quá ngưỡng thì khóa OTP.
- Đúng code -> update password hash user, mark used OTP, revoke refresh tokens/session.

**Done criteria**

- Reset thành công thì login cũ không còn hiệu lực (nếu đã có refresh token).

### Bước 5: Expose controller endpoints

**Files**

- `auth/controller/AuthController.java`

**Done criteria**

- Có 2 API mới với response chuẩn `ApiMessageResponse`.

### Bước 6: Constant + exception message

**Files**

- `auth/constant/AuthConstant.java`

**Done criteria**

- Không hardcode message trong service.

### Bước 7: Testing

**Files**

- `src/test/java/.../auth/service/...Test.java`

**Done criteria**

- Unit test pass cho luồng happy + edge + negative.

---

## 6) Pseudo-code / Code skeleton (không full code)

### DTO skeleton

```java
public record ForgotPasswordRequest(
    @NotBlank @Email String email
) {}

public record ForgotPasswordConfirmRequest(
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "^\\d{6}$") String resetCode,
    @NotBlank @Size(min = 8, max = 72) String newPassword
) {}
```

### Service skeleton

```java
ApiMessageResponse requestForgotPassword(ForgotPasswordRequest request) {
  String email = normalize(request.email());
  Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

  if (userOpt.isEmpty()) {
    return genericMessage(); // không lộ thông tin
  }

  validateCooldown(email);
  passwordResetOtpRepository.invalidateAllActiveByEmail(email);

  String code = generateOtp();
  saveResetOtp(email, code, expiresAt, attempt=0, used=false);
  emailService.sendPasswordResetEmail(email, code, expirationMinutes);

  return genericMessage();
}

ApiMessageResponse confirmForgotPassword(ForgotPasswordConfirmRequest request) {
  String email = normalize(request.email());
  PasswordResetOtp latest = findLatestOrThrow(email);

  validateNotUsedAndNotExpired(latest);

  if (!latest.code.equals(request.resetCode())) {
    increaseAttemptOrLock(latest);
    throw invalidCodeException();
  }

  User user = findUserOrThrow(email);
  user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
  markOtpUsed(latest);
  refreshTokenService.revokeAllByUser(user.getId());

  return successMessage();
}
```

---

## 7) Exception handling & validation

### Validation rules

- Email: `@Email`, `@NotBlank`
- Reset code: đúng format (6 chữ số)
- New password: 8–72 ký tự

### Lỗi và HTTP status gợi ý

- `400 Bad Request`
  - Invalid/expired/used reset code
  - Password không hợp lệ
- `429 Too Many Requests`
  - Request reset quá nhanh (cooldown)
- `409 Conflict` (tùy chọn)
  - Reset code bị khóa vì sai quá nhiều lần

### Message gợi ý (English)

- `If your email exists in our system, a reset code has been sent.`
- `Reset code is invalid.`
- `Reset code has expired.`
- `Reset code is locked due to too many incorrect attempts.`
- `Password has been reset successfully.`

---

## 8) Checklist tự test

### Happy path

- Email tồn tại -> nhận mail reset.
- Nhập đúng code + password mới -> đổi mật khẩu thành công.
- Login bằng mật khẩu mới thành công.

### Edge cases

- Request reset nhiều lần trong cooldown.
- OTP cũ bị invalid khi request OTP mới.
- OTP hết hạn.

### Negative cases

- Email không tồn tại vẫn trả message trung tính.
- Sai code liên tục -> khóa OTP.
- Dùng lại OTP đã used -> bị từ chối.

---

## 9) Checklist review trước khi commit

- [ ] Build Maven pass.
- [ ] Unit tests cho forgot password pass.
- [ ] Không hardcode message, dùng constants.
- [ ] Không log reset code/password.
- [ ] Transaction không giữ lâu phần gửi email (đã async).
- [ ] API không lộ email tồn tại hay không.
- [ ] Reset thành công có revoke session/refresh token.

---

## 10) Follow-up nâng cấp

- Thêm CAPTCHA cho endpoint request reset.
- Thêm rate-limit theo IP + email (Redis).
- Thêm audit event: request reset, confirm reset, failed attempts.
- Template email chuyên nghiệp + đa ngôn ngữ.
- Cơ chế reset link (signed token) song song OTP (tuỳ UX sản phẩm).
