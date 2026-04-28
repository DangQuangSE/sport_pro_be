# Hướng dẫn code tay tính năng Refresh Token + Cookie cho `sport_pro_be`

## 1. Mô tả bài toán

### Mục tiêu business

- Giảm tần suất user phải đăng nhập lại khi access token hết hạn.
- Giữ trải nghiệm liền mạch trên web (reload trang vẫn duy trì phiên).
- Tăng bảo mật phiên đăng nhập bằng refresh token rotation + revoke.

### In-scope

- Bổ sung cơ chế refresh token cho module `auth`.
- Lưu refresh token qua cookie (`HttpOnly`, `Secure`, `SameSite`).
- Thêm API: `POST /api/auth/refresh-token`, `POST /api/auth/logout`.
- Hỗ trợ rotation (mỗi lần refresh cấp token mới và vô hiệu token cũ).
- Cập nhật response login để hỗ trợ web app biết trạng thái đăng nhập trước đó.

### Out-scope

- Không thay thuật toán hash password hiện tại.
- Không thay cơ chế authorization role/permission hiện hữu.
- Không triển khai social login / OAuth2 bên thứ ba trong scope này.

---

## 2. Thiết kế kỹ thuật (high-level)

### Kiến trúc luồng

1. User login thành công -> server trả `accessToken` (response body) + set `refreshToken` trong cookie.
2. Access token hết hạn -> frontend gọi `POST /api/auth/refresh-token` (cookie tự đính kèm).
3. Backend validate refresh token:
   - hợp lệ -> rotate: revoke token cũ, phát access token mới + refresh token mới.
   - không hợp lệ/reused/revoked -> trả lỗi, yêu cầu login lại.
4. Logout -> revoke refresh token hiện tại và clear cookie.

### Thành phần liên quan

- `controller`: mở rộng `AuthController` với endpoint refresh/logout.
- `service`: tách logic cấp/rotate/revoke refresh token (khuyến nghị service riêng).
- `repository`: CRUD/lookup refresh token theo token hash / user / trạng thái.
- `entity`: thêm bảng refresh token (session-level).
- `config`: cookie config + auth properties cho TTL, SameSite, Secure.
- `exception`: map lỗi refresh token qua `ApiExceptionHandler` hiện có.

### Mô hình dữ liệu đề xuất

Bảng `refresh_tokens` (gợi ý):

- `id` (PK)
- `user_id` (FK `app_users.id`)
- `token_hash` (unique, không lưu raw token)
- `expires_at`
- `revoked` (boolean)
- `revoked_at` (nullable)
- `replaced_by_token_hash` (nullable, phục vụ rotation trace)
- `created_at`
- `ip_address` (nullable)
- `user_agent` (nullable)

---

## 3. Thư viện đề xuất

### Bắt buộc

- **Không cần thêm thư viện mới** nếu giữ hướng dùng component sẵn có của Spring + JDK:
  - `ResponseCookie` (`org.springframework.http`) để set cookie.
  - `MessageDigest` (SHA-256) hoặc tương đương để hash refresh token trước khi lưu DB.

### Tùy chọn (nếu muốn production-ready hơn)

- `spring-boot-starter-data-redis`: lưu blacklist/revocation cache tốc độ cao.
- Lý do: giảm truy vấn DB ở tần suất refresh lớn.
- Rủi ro: tăng complexity vận hành (cần Redis infra).

---

## 4. Cấu hình cần thêm

### `application.properties` (hoặc `config/auth.properties`)

Thêm keys gợi ý:

- `app.auth.access-token-expiration-minutes=15`
- `app.auth.refresh-token-expiration-days=14`
- `app.auth.refresh-token-cookie-name=refreshToken`
- `app.auth.refresh-token-cookie-secure=true`
- `app.auth.refresh-token-cookie-same-site=None`
- `app.auth.refresh-token-cookie-path=/api/auth`
- `app.auth.refresh-token-rotation-enabled=true`
- `app.auth.refresh-token-max-sessions=5`

### Giá trị dev/prod gợi ý

- **Dev local HTTP**:
  - `secure=false`
  - `same-site=Lax` (hoặc `None` nếu test FE/BE khác domain qua HTTPS tunnel)
- **Prod**:
  - `secure=true`
  - `same-site=None` (nếu FE và BE khác site)
  - bắt buộc HTTPS.

### `pom.xml`

- Giữ nguyên dependencies hiện tại là đủ cho phiên bản đầu tiên.
- Không cần dependency mới bắt buộc cho refresh + cookie.

---

## 5. Kế hoạch triển khai code tay (step-by-step)

### Bước 1: Chuẩn bị contract API

**File cần sửa/tạo**

- Sửa: `src/main/java/com/sport_pro_be/auth/controller/AuthController.java`
- Tạo DTO request/response nếu thiếu trong `src/main/java/com/sport_pro_be/auth/dto/`

**Việc làm**

- Thêm endpoint `POST /api/auth/refresh-token` (không cần body nếu lấy từ cookie).
- Thêm endpoint `POST /api/auth/logout`.
- Cập nhật response login gồm access token metadata cần thiết.

**Tiêu chí hoàn thành**

- Swagger/OpenAPI thể hiện đủ 3 endpoint login/refresh/logout.

### Bước 2: Tạo Entity + Repository cho refresh token

**File cần tạo**

- `auth/domain/RefreshToken.java`
- `auth/repository/RefreshTokenRepository.java`

**Việc làm**

- Mapping bảng `refresh_tokens` với index trên `token_hash`, `user_id`.
- Method repo: tìm token active theo hash, revoke theo user/token.

**Tiêu chí hoàn thành**

- Ứng dụng khởi động không lỗi mapping JPA.
- DB sinh bảng/cột đúng theo entity.

### Bước 3: Tạo service xử lý refresh token

**File cần tạo/sửa**

- Tạo: `auth/interfaces/IRefreshTokenService.java`
- Tạo: `auth/service/RefreshTokenService.java`
- Sửa: `auth/service/AuthService.java`

**Việc làm**

- Sau login: tạo raw refresh token, hash và lưu DB.
- Refresh: validate -> rotate token cũ -> cấp token mới.
- Logout: revoke token hiện tại.

**Tiêu chí hoàn thành**

- Không còn logic token dồn hết vào `AuthService` (đảm bảo SRP).

### Bước 4: Set/Clear cookie chuẩn bảo mật

**File cần sửa**

- `AuthController` hoặc class helper chuyên cookie (khuyến nghị `auth/service/AuthCookieService.java`).

**Việc làm**

- Set cookie `HttpOnly`, `Secure`, `SameSite`, `Path`, `Max-Age`.
- Logout và refresh-fail phải clear cookie.

**Tiêu chí hoàn thành**

- Browser nhận `Set-Cookie` đúng thuộc tính.

### Bước 5: Exception handling + message constants

**File cần sửa**

- `auth/constant/AuthConstant.java`
- `common/ApiExceptionHandler.java`

**Việc làm**

- Thêm constants: invalid/revoked/expired/reused refresh token.
- Map status code hợp lý qua `ResponseStatusException`/custom exception.

**Tiêu chí hoàn thành**

- Response lỗi có format nhất quán (`timestamp`, `status`, `message`).

### Bước 6: Testing

**File cần tạo/sửa**

- `src/test/java/com/sport_pro_be/auth/service/RefreshTokenServiceTest.java`
- Cập nhật test hiện có trong `AuthServiceTest`.

**Tiêu chí hoàn thành**

- Unit test pass cho luồng login -> refresh -> logout.

---

## 6. Pseudo-code / code skeleton (không full implementation)

> Lưu ý: đây là skeleton để bạn code tay, không phải code chạy ngay.

### Entity skeleton

```java
@Entity
@Table(name = "refresh_tokens")
class RefreshToken {
    Long id;
    Long userId;
    String tokenHash;
    LocalDateTime expiresAt;
    boolean revoked;
    LocalDateTime revokedAt;
    String replacedByTokenHash;
    LocalDateTime createdAt;
    String ipAddress;
    String userAgent;
}
```

### Service contract skeleton

```java
public interface IRefreshTokenService {
    RefreshTokenIssueResult issue(User user, DeviceContext deviceContext);
    RefreshTokenIssueResult rotate(String rawRefreshToken, DeviceContext deviceContext);
    void revokeCurrent(String rawRefreshToken);
    void revokeAllByUser(Long userId);
}
```

### Auth flow skeleton

```java
login(request):
  validate credentials
  accessToken = jwtService.generateAccessToken(user)
  refresh = refreshTokenService.issue(user, deviceContext)
  setRefreshCookie(response, refresh.rawToken)
  return LoginSuccessResponse(accessToken, ...)

refreshToken(request):
  raw = readRefreshTokenFromCookie(request)
  result = refreshTokenService.rotate(raw, deviceContext)
  setRefreshCookie(response, result.rawToken)
  return AccessTokenResponse(result.accessToken, ...)

logout(request):
  raw = readRefreshTokenFromCookie(request)
  refreshTokenService.revokeCurrent(raw)
  clearRefreshCookie(response)
  return ApiMessageResponse("Logged out successfully")
```

### Cookie helper skeleton

```java
ResponseCookie buildRefreshCookie(String token) {
  // name, value, httpOnly, secure, sameSite, path, maxAge
}

ResponseCookie clearRefreshCookie() {
  // set Max-Age=0
}
```

---

## 7. Exception handling & validation

### Danh sách lỗi chính

- Refresh token thiếu trong cookie.
- Refresh token hết hạn.
- Refresh token đã bị revoke.
- Refresh token không tồn tại / sai hash.
- Refresh token reuse (token cũ sau rotation bị dùng lại).

### Mapping HTTP status gợi ý

- `400 Bad Request`: thiếu token/cookie sai format.
- `401 Unauthorized`: token không hợp lệ/hết hạn.
- `409 Conflict`: phát hiện reuse token (nghi ngờ token theft).
- `500 Internal Server Error`: lỗi ngoài dự kiến.

### Message gợi ý (English)

- `Refresh token is required`
- `Refresh token is invalid or expired`
- `Refresh token has been revoked`
- `Refresh token reuse detected. Please login again`

---

## 8. Checklist tự test

### Happy path

- Login đúng -> nhận access token + `Set-Cookie(refreshToken=...)`.
- Access token hết hạn -> gọi refresh thành công, nhận access token mới.
- Logout -> cookie bị clear, refresh sau logout thất bại.

### Edge cases

- Nhiều tab browser gọi refresh gần đồng thời.
- User login trên nhiều thiết bị (nếu hỗ trợ multi-session).
- Cookie path/samesite khác môi trường dev/prod.

### Negative cases

- Gửi refresh token đã revoke.
- Gửi refresh token giả mạo.
- Dùng lại refresh token cũ sau rotation.

---

## 9. Checklist review trước khi commit

- [ ] Build Maven thành công.
- [ ] Unit test auth + refresh token pass.
- [ ] Không còn hard-code message (đưa vào constant).
- [ ] Cookie có `HttpOnly` + `Secure` đúng môi trường.
- [ ] Không log raw refresh token.
- [ ] Kiểm tra transaction scope không ôm phần chậm không cần thiết.
- [ ] Đảm bảo endpoint refresh/logout không mở lỗ hổng CSRF ngoài dự tính.

---

## 10. Follow-up nâng cấp

- Thêm endpoint quản lý session: xem/thu hồi theo thiết bị.
- Tích hợp Redis cache cho revocation list để scale lớn.
- Thêm audit log cho login/refresh/logout (mask thông tin nhạy cảm).
- Áp dụng rate-limit riêng cho `refresh-token` endpoint.
- Bổ sung cảnh báo bảo mật khi detect token reuse nhiều lần.

---

## Gợi ý triển khai frontend (ngắn)

- App start: gọi nhẹ endpoint refresh để restore session.
- API interceptor: nếu 401 do access token hết hạn -> refresh 1 lần rồi retry request.
- Khi refresh fail -> clear state và redirect login.
