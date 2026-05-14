# Backend Development Roadmap: Professional Authentication System (Detailed Version)

Tài liệu này cung cấp tư duy logic và trình tự triển khai chi tiết cho hệ thống Auth chuyên nghiệp.

---

## Giai đoạn 1: Foundation - Xây dựng bộ khung tiêu chuẩn

### 1. Khởi tạo & Config "sống còn"
- **Properties Management**: Tạo một `AuthProperties` class sử dụng `@ConfigurationProperties` để quản lý các hằng số (JWT Secret, thời gian hết hạn Access/Refresh token, Cooldown gửi OTP, Tên Cookie...). *Tại sao? Để khi cần đổi cấu hình, bạn không phải tìm trong code.*
- **Security Context**: Cấu hình `BCryptPasswordEncoder` làm Bean để mã hóa mật khẩu. Tuyệt đối không lưu mật khẩu dạng plain text.

### 2. Standardized Response (ApiResponse)
- Định nghĩa định dạng JSON trả về: 
    - `message`: String (Thông báo cho FE hiển thị).
    - `data`: T (Dữ liệu trả về, null nếu không có).
    - `timestamp`: Instant (Thời gian phản hồi).
- *Tư duy Senior*: Frontend sẽ viết một `axios interceptor` chung dựa trên định dạng này để xử lý thông báo tự động.

### 3. Global Exception Handling (Trái tim của hệ thống lỗi)
- **AppException**: Chứa `HttpStatus` và `message`.
- **GlobalHandler**: Bắt các lỗi sau:
    - `MethodArgumentNotValidException`: Lỗi khi validate `@Valid` ở DTO (trả về danh sách các trường lỗi).
    - `AppException`: Các lỗi nghiệp vụ do bạn tự ném ra.
    - `Exception.class`: Lỗi hệ thống không xác định (phải mask message bằng "Internal Server Error" để bảo mật).

---

## Giai đoạn 2: Domain Layer - Thiết kế để mở rộng

### 4. Entity chi tiết
- **User**: 
    - `email`: unique, indexed (để tìm kiếm nhanh).
    - `tokenVersion`: Integer (dùng để vô hiệu hóa toàn bộ token cũ khi đổi mật khẩu/logout từ xa).
- **OtpVerification**: 
    - `otpType`: Enum (REGISTER, FORGOT_PASSWORD).
    - `attemptCount`: Đếm số lần nhập sai (ví dụ quá 5 lần thì khóa mã này).
    - `isVerified`: boolean (để đánh dấu đã qua bước OTP, tránh người dùng bypass thẳng đến bước register).
- **RefreshToken**:
    - `tokenHash`: Lưu hash của token thay vì token gốc (để nếu DB bị hack, hacker cũng không dùng được token).
    - `replacedByToken`: Lưu hash của token mới (dùng trong cơ chế Token Rotation).

---

## Giai đoạn 3: Security Infrastructure - Thiết lập hàng rào

### 5. JwtService Logic
- **Claims**: Access Token nên chứa `sub` (email), `uid` (user id), `roles`, và `tokenVersion`.
- **Validation**: Kiểm tra token có đúng định dạng không, đã hết hạn chưa, có đúng chữ ký không.

### 6. JwtAuthenticationFilter (Luồng xử lý)
1. Lấy Token từ Header `Authorization: Bearer <token>`.
2. Nếu không có hoặc sai format: Cho đi tiếp (Spring Security sẽ chặn ở bước sau nếu API yêu cầu auth).
3. Nếu có: Giải mã Token.
4. Kiểm tra `tokenVersion` trong Token có khớp với `tokenVersion` hiện tại của User trong DB không. *Nếu không khớp => Token đã bị vô hiệu hóa.*
5. Set `UsernamePasswordAuthenticationToken` vào `SecurityContextHolder`.

---

## Giai đoạn 4: Auth Module - Triển khai logic nghiệp vụ

### 7. Luồng Đăng ký (Register Flow)
- **Request OTP**:
    - Chuẩn hóa email (trim, lowercase).
    - Kiểm tra email đã tồn tại chưa.
    - **Cooldown**: Kiểm tra xem mã OTP trước đó gửi cách đây bao lâu (ví dụ phải đợi 60s mới được gửi lại).
    - Vô hiệu hóa toàn bộ OTP cũ của email này trước khi tạo mới.
- **Verify OTP**:
    - Kiểm tra mã có khớp không, có hết hạn không, đã dùng chưa.
    - Nếu sai: Tăng `attemptCount`. Nếu quá giới hạn => Hủy mã.
- **Complete Register**:
    - Kiểm tra cờ `isVerified` trong bảng OTP. Nếu chưa verify mà gọi API này => Chặn đứng.

### 8. Luồng Đăng nhập & Token Rotation (Quan trọng nhất)
- **Login**:
    - Kiểm tra mật khẩu bằng `passwordEncoder.matches()`.
    - Tạo cặp Access & Refresh Token.
    - Lưu Refresh Token vào DB.
- **Refresh Token (Cơ chế xoay vòng)**:
    1. Nhận Refresh Token cũ từ Cookie.
    2. Hash nó và tìm trong DB.
    3. **Nếu Token đã bị Revoked (thu hồi)**: Đây có thể là dấu hiệu bị tấn công reuse token. => *Xóa toàn bộ các refresh token đang hoạt động của User đó* (bắt login lại trên mọi thiết bị).
    4. Nếu hợp lệ: Tạo cặp Token mới, đánh dấu token cũ là revoked và lưu vết `replacedByToken` là token mới.

---

## Giai đoạn 5: Forgot Password - Bảo mật đa lớp

### 9. Luồng xử lý chi tiết
- **Verify OTP**: Thay vì cho phép đổi mật khẩu ngay, hãy trả về một **Reset Token** (JWT ngắn hạn, ví dụ 5-10 phút).
- **Reset Password**: 
    - Nhận Reset Token.
    - Verify Token để lấy Email.
    - Cập nhật mật khẩu mới.
    - **Quan trọng**: Tăng `tokenVersion` của User hoặc thu hồi toàn bộ Refresh Token cũ để đảm bảo an toàn tuyệt đối.

---

## Các kỹ thuật Senior nên áp dụng:
1. **Soft Delete**: Không bao giờ xóa cứng User, hãy dùng trường `deletedAt`.
2. **Auditing**: Sử dụng `CreatedBy`, `CreatedAt`, `LastModifiedAt` cho mọi Entity.
3. **Transaction Management**: Sử dụng `@Transactional` cho các method Service thực hiện nhiều thao tác ghi xuống DB để đảm bảo tính toàn vẹn (Atomicity).
4. **Validation Messages**: Luôn tách message ra `Constants` để dễ quản lý và bản địa hóa.
