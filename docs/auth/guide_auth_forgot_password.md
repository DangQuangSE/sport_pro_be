# Hướng dẫn triển khai `forgot-password` (bản cải thiện kiến trúc) cho `sport_pro_be`

## 1) Mục tiêu thay đổi

Tài liệu này cập nhật theo định hướng mới trước khi code chính thức:

- **Không tạo thêm entity `PasswordResetOtp` riêng** để tránh tăng độ phức tạp.
- **Tái sử dụng entity OTP hiện tại** bằng cách đổi tên `EmailOtp` thành một entity trung tính hơn.
- **Phân loại OTP bằng enum** để dùng chung cho nhiều luồng (register, forgot-password, ...).
- **Tách module forgot-password** (controller/interface/repository/service riêng), không dồn logic vào các class authentication hiện có.

---

## 2) Thiết kế mới (High-level)

### 2.1 Entity OTP dùng chung

Đổi tên:

- `EmailOtp` -> `OtpVerification` (hoặc tên trung tính tương đương theo convention của project).

Thêm enum phân loại:

- `OtpType.REGISTER`
- `OtpType.FORGOT_PASSWORD`
- (mở rộng sau: `CHANGE_EMAIL`, `MFA_LOGIN`, ...)

Các field chính cần có trong entity OTP trung tính:

- `id`
- `email`
- `otpCode`
- `otpType` (enum)
- `used`
- `attemptCount`
- `expiresAt`
- `createdAt`
- `updatedAt`

> Lưu ý: mọi query/validate OTP bắt buộc filter theo **`email + otpType`** để tránh lẫn OTP giữa register và forgot-password.

### 2.2 Tách module forgot-password khỏi authentication chung

Tạo nhóm class riêng (có thể nằm dưới package `auth`, nhưng tách theo namespace rõ ràng):

- `auth/forgotpassword/controller/ForgotPasswordController`
- `auth/forgotpassword/interfaces/ForgotPasswordService`
- `auth/forgotpassword/service/ForgotPasswordServiceImpl`
- `auth/forgotpassword/repository/ForgotPasswordOtpQueryRepository` (nếu cần query chuyên biệt)
- `auth/forgotpassword/dto/*`

Các thành phần dùng lại:

- `OtpVerificationRepository` (repository chính cho OTP dùng chung)
- `UserRepository`
- `EmailService`
- `RefreshTokenService` (hoặc service revoke session tương đương)

---

## 3) Luồng nghiệp vụ forgot-password (yêu cầu mới)

###+ Bước 1: Request forgot-password (nhập email)

1. Client gửi email.
2. Backend chuẩn hóa email (trim/lowercase).
3. Kiểm tra email có tồn tại trong database user hay không.
4. Nếu **có tồn tại**:
   - kiểm tra resend cooldown giống luồng register;
   - sử dụng Transaction (ví dụ `@Transactional`) để đảm bảo toàn vẹn dữ liệu:
     - vô hiệu các OTP active cũ của `otpType = FORGOT_PASSWORD`;
     - tạo OTP mới với loại `FORGOT_PASSWORD`;
   - gửi email OTP bất đồng bộ.
5. Nếu **không tồn tại**:
   - không tạo OTP;
   - vẫn trả về response trung tính.

Response khuyến nghị (trung tính chống enumerate):

- `If your email exists in our system, an OTP has been sent.`

###+ Bước 2: Verify OTP chủ sở hữu email

1. Client gửi `email + otpCode`.
2. Backend lấy OTP mới nhất theo `email + FORGOT_PASSWORD`.
3. Validate:
   - chưa dùng;
   - chưa hết hạn;
   - chưa vượt quá số lần nhập sai;
   - mã OTP khớp.
4. Nếu đúng OTP:
   - đánh dấu OTP verified/used theo thiết kế đang áp dụng;
   - phát hành token phiên ngắn hạn cho bước đổi mật khẩu (ví dụ `forgotPasswordToken` dạng JWT). **Lưu ý quan trọng**: Token này phải chứa `email` hoặc `userId` bên trong payload và có thời gian sống ngắn (ví dụ 5-15 phút).

###+ Bước 3: Đặt mật khẩu mới

1. Chỉ cho phép gọi khi có `forgotPasswordToken` hợp lệ. **Backend tuyệt đối phải giải mã token này để lấy `email`/`userId`, không được dùng `email` do client truyền lên ở body** để tránh lỗ hổng bảo mật.
2. Validate `newPassword` theo policy hiện hành.
3. Sử dụng Transaction (ví dụ `@Transactional`) để đảm bảo tính toàn vẹn:
   - Update password hash cho user.
   - Revoke toàn bộ refresh token/session cũ của user.
4. Trả về thông báo thành công.

---

## 4) Anti-spam / Anti-attack bắt buộc

- Áp dụng resend OTP limit **giống register** (cooldown theo email).
- Giới hạn số lần nhập OTP sai (`maxAttempts`).
- OTP cũ phải bị vô hiệu khi phát OTP mới cùng `email + FORGOT_PASSWORD`.
- Không log plain OTP hoặc password.
- Response message không phân biệt email có tồn tại hay không ở bước request.

---

## 5) API contract đề xuất

### 5.1 Request OTP for forgot-password

- `POST /api/forgot-password/request-otp`

Request:

- `email`

Response:

- `ApiMessageResponse` trung tính.

### 5.2 Verify OTP for forgot-password

- `POST /api/forgot-password/verify-otp`

Request:

- `email`
- `otpCode`

Response:

- thành công: message + token/flag cho phép reset password.

### 5.3 Reset password

- `POST /api/forgot-password/reset`

Request:

- `newPassword`
- `forgotPasswordToken` (Bắt buộc có, backend tự giải mã token để lấy `email`/`userId` bên trong)

Response:

- `Password has been reset successfully.`

---

## 6) Cấu hình đề xuất (reuse + bổ sung)

Trong `auth.properties` (hoặc nhóm config OTP dùng chung):

- `app.auth.otp.expiration-minutes=10`
- `app.auth.otp.resend-cooldown-seconds=60`
- `app.auth.otp.max-attempts=5`
- `app.auth.forgot-password.token-expiration-minutes=15`
- `app.auth.forgot-password.email-subject=[Sport Pro] Forgot password OTP`

Nếu project đang dùng config tách theo từng luồng, có thể giữ key cũ nhưng nên chuẩn hóa naming để dùng chung OTP engine.

---

## 7) Kế hoạch code tay (đã chỉnh theo yêu cầu)

### Bước 1: Refactor entity OTP

- Đổi tên `EmailOtp` sang tên trung tính (`OtpVerification`).
- Thêm enum `OtpType` và field `otpType`.
- Update migration/schema/index theo `email + otp_type + created_at`.

### Bước 2: Cập nhật repository OTP dùng chung

- Query OTP mới nhất theo `email + otpType`.
- Invalidate active OTP theo `email + otpType`.
- Query cooldown theo `email + otpType`.

### Bước 3: Tạo module forgot-password riêng

- Tạo controller/service/interface/repository/dto cho forgot-password.
- Không nhồi thêm vào `AuthController`/`AuthService` hiện tại.

### Bước 4: Implement logic 3 bước

- request OTP -> verify OTP -> reset password.
- Revoke session sau khi đổi mật khẩu thành công.

### Bước 5: Test

- Happy path: email tồn tại -> verify OTP -> reset pass thành công.
- Edge: resend quá nhanh, OTP hết hạn, OTP cũ bị invalid khi request mới.
- Negative: email không tồn tại, nhập sai OTP nhiều lần, dùng OTP đã used.

---

## 8) Checklist review trước khi commit

- [ ] Không còn tạo entity `PasswordResetOtp` riêng.
- [ ] `EmailOtp` đã được đổi tên thành entity OTP trung tính.
- [ ] Có enum `OtpType` để phân biệt luồng OTP.
- [ ] Luồng forgot-password đã tách module riêng (controller/interface/service/repository).
- [ ] Có giới hạn resend OTP như register.
- [ ] Chỉ cho reset password sau khi xác nhận đúng OTP.
- [ ] Reset xong có revoke refresh token/session.
- [ ] Không lộ thông tin email tồn tại/không tồn tại ở bước request.

---

## 9) Ghi chú triển khai thực tế

- Nếu muốn giảm thêm complexity, có thể dùng chung một `OtpApplicationService` cho phần tạo/verify OTP, và module forgot-password chỉ orchestration business.
- Nếu cần backward compatibility ngắn hạn, có thể giữ alias/table mapping tạm thời trong migration rồi dọn ở bản release kế tiếp.
