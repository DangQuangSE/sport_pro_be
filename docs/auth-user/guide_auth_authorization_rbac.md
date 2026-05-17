# Hướng dẫn code tay RBAC Authorization cho module Auth

## 1. Mô tả bài toán

### Mục tiêu business

- Bổ sung cơ chế phân quyền rõ ràng theo vai trò (RBAC) sau khi đã hoàn thiện Authentication + Forgot Password.
- Chặn truy cập trái phép theo nguyên tắc least privilege.
- Tạo nền tảng để mở rộng permission chi tiết theo module trong tương lai.
- **Bảo mật chặt chẽ:** Triệt tiêu các lỗ hổng phổ biến như leo thang đặc quyền (Privilege Escalation), thay đổi trái phép dữ liệu người khác (IDOR).

### In-scope

- Thiết kế và triển khai RBAC mức Role-based cho API hiện có.
- Trấn áp nguy cơ lỗ hổng: Mass Assignment (leo thang đặc quyền lúc đăng ký) và Stale Token (admin bị hạ/band quyền nhưng token vẫn còn hạn).
- Triển khai **Role Hierarchy**: ADMIN mặc định có tất cả quyền hợp lệ của USER mà không cần gọi trùng lặp (ví dụ `hasAnyRole`).
- Đồng bộ quyền từ DB -> JWT claim -> SecurityContext -> kiểm tra ở endpoint.
- Bổ sung endpoint mẫu cần role đặc thù (ADMIN/USER) để kiểm chứng luồng.
- Chuẩn hóa lỗi 401/403 cho frontend tiêu thụ nhất quán.

### Out-scope

- Chưa triển khai ABAC (attribute-based) ở diện rộng, chỉ check cơ bản Ownership.
- Chưa triển khai UI quản trị role/permission.
- Chưa triển khai audit log đầy đủ theo chuẩn compliance (chỉ đề xuất follow-up).

---

## 2. Thiết kế kỹ thuật (high-level)

### Kiến trúc flow

1. User đăng nhập thành công.
2. Backend nạp danh sách role và `tokenVersion` của user từ DB.
3. Khi tạo access token, nhúng claim `roles` (vd: `["USER"]`) và claim `token_version`.
4. Request vào API protected:
   - JWT filter parse token.
   - Validate chữ ký + hạn token.
   - **Tùy chọn:** Bắt buộc đối chiếu `token_version` trong claim với User DB, hoặc check Redis Blacklist để hủy tức thời token bị đánh dấu.
   - Trích role claim -> map thành `GrantedAuthority`.
   - Set `Authentication` vào `SecurityContext`.
5. Security layer kiểm tra quyền:
   - URL-level (`requestMatchers`) cho rule chung (coarse-grained).
   - Method-level (`@PreAuthorize`) kiểm tra Role và **Luật chủ sở hữu (Ownership)** cho rule chi tiết.

### Mô hình dữ liệu RBAC đề xuất

Với giai đoạn hiện tại (nhanh gọn + an toàn), sử dụng Option Enum kèm Token Version:

- Thêm cột `role` vào `app_users` (dùng `enum Role { USER, ADMIN }`).
- Thêm cột `token_version` (kiểu `int`, mặc định là `1`) vào `app_users` để quản lý Stale Token do Redis Cloud nếu dùng có thể mất phí.

---

## 3. Thư viện đề xuất & Phương án chặn lỗ hổng "Stale Token"

Trường hợp User / Admin bị khóa tài khoản hoặc tước quyền, Access Token thời hạn 15 phút của họ vẫn "sống sót" gây nguy hiểm. Để chặn ngay lỗ hổng này:

**Nếu hệ thống của bạn có thể dùng Redis tự host (Miễn phí hoàn toàn nếu cài chung một VPS):**
Bạn có thể lưu các `Token ID (jti)` bị hủy vào Redis với thời gian sống (TTL) bằng chính thời gian còn lại của token. Filter sẽ từ chối gọi vào hệ thống nếu check dính blacklist.

**Nếu bạn không muốn cài cắm hạ tầng phức tạp rườm rà (Miễn phí, 100% bằng source code hiện có):**
Dùng phương án **Token Version**. Thêm field `tokenVersion = 1` ở Entity User. Khi phát sinh JWT, gắn thêm claim `"tv": 1`. Bất cứ rủi ro nào xảy ra (User bị ban, đổi pass, hạ quyền), chỉ việc nâng `tokenVersion` lên trong DB. Khi call API, Filter lấy email query DB kiểm tra Version không khớp sẽ đánh dấu Unauthorized 401 ngay lập tức. Đây là cách **tối ưu và sẽ được áp dụng xuyên suốt hướng dẫn này**.

---

## 4. Cấu hình cần thêm

### `application.properties` (gợi ý thêm)

```properties
# Bat/tat method security mo rong
app.auth.method-security-enabled=true

# Prefix role trong Spring Security
app.auth.role-prefix=ROLE_

# Access token time
app.auth.jwt-expiration-minutes=15
```

### Cấu hình Role Hierarchy trong SecurityConfig

Khai báo Bean này ở phần config để `ADMIN` kế thừa quyền của `USER`, tránh việc phải code `@PreAuthorize("hasAnyRole('ADMIN', 'USER')")` dài dòng ở mọi nơi:
```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    // ADMIN mặc định bao gồm quyền USER
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    return roleHierarchy;
}
```

---

## 5. Kế hoạch triển khai code tay (step-by-step)

### Bước 1: Bổ sung role và token version vào User model

File cần sửa/tạo:
- Tạo: `src/main/java/com/sport_pro_be/auth/domain/Role.java` (`USER`, `ADMIN`).
- Sửa: `src/main/java/com/sport_pro_be/auth/domain/User.java`.

Việc làm:
- Thêm `@Enumerated(EnumType.STRING) Role role;`
- Thêm `Integer tokenVersion = 1;`
- **[QUAN TRỌNG - CHỐNG MASS ASSIGNMENT]**: Ở DTO dành cho API Đăng ký (`RegisterRequest`) hoặc Update profile, tuyệt đối KHÔNG ĐƯỢC có field `role` và `tokenVersion`. Nếu client cố tình nhét vào JSON, hệ thống phải ignore, chỉ set role bằng code backend.

### Bước 2: Nhúng role & token_version vào JWT access token

File cần sửa: `src/main/java/com/sport_pro_be/auth/service/JwtService.java`

Việc làm:
- Generate token thêm claim `roles` và `tv` (token_version).
- Thêm phụ trợ `getRoleClaim(Token)`, `getTokenVersion(Token)`.

### Bước 3: Tạo/Sửa JWT authentication filter + principal

Việc làm:
- Đọc `Authorization: Bearer <token>`.
- Trích xuất token và lấy `email`, `tv` ra.
- Parse `roles` -> `SimpleGrantedAuthority`. Chú ý tự đồng bộ tiền tố `ROLE_`.
- (Chống Stale Token) Query user từ DB (hoặc Cache), kiểm tra `user.getTokenVersion() == tv_trong_token`. Nếu không khớp, chặn cuộc gọi và return `401 Unauthorized`.
- Đặt vào SecurityContextHolder.

### Bước 4: Áp rule role và chặn IDOR ở Endpoint/Service

Việc làm:
- Dùng `@PreAuthorize("hasRole('ADMIN')")` cho API admin.
- Lỗ hổng **IDOR (Truy cập dữ liệu người khác)**: `@PreAuthorize` với expression chặn cứng quyền Ownership.
Ví dụ: User A chỉ được sửa data của User A, còn ADMIN được sửa của bất cứ ai:
```java
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
@PutMapping("/api/users/{id}")
```

### Bước 5: Chuẩn hóa exception 401/403

- Cập nhật Handler `ApiExceptionHandler` xử lý các Security Exceptions thành response JSON, không trả thẻ HTML.

### Bước 6: Viết test cho RBAC & Vulnerabilities

- Role test: thiếu Role -> 403, Đủ Role -> 200.
- Stale Token test: Login lấy JWT -> Tăng `tokenVersion` lên dưới DB -> Gọi Endpoint bằng token cũ -> 401.
- Mass Assignment Test: Gọi đăng ký cố tình nạp JSON `{ "role": "ADMIN" }`, kiểm tra DB xem nó có còn là USER không. Đảm bảo test này xanh.

---

## 6. Pseudo-code / Code skeleton (Mẫu để dễ hình dung)

### User entity skeleton

```java
@Entity
@Table(name = "app_users")
public class User {
    Long id;
    String email;
    String passwordHash;
    boolean emailVerified;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; // Mặc định luôn là USER khi register

    @Column(nullable = false)
    private Integer tokenVersion = 1; // Khởi tạo ban đầu là 1
    
    // ... getter setter
}
```

### JWT issue skeleton

```java
public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", List.of(user.getRole().name()));
    claims.put("tv", user.getTokenVersion()); // Nhét version vào để đối chiếu 
    // ...
    return builder.setClaims(claims).signWith(secretKey).compact();
}
```

### JWT filter logic (bổ sung check Version)

```java
protected void doFilterInternal(request, response, chain) {
    String token = resolveBearerToken(request);
    if (token == null) {
        chain.doFilter(request, response); return;
    }

    Claims claims = jwtService.parse(token);
    String email = claims.getSubject();
    Integer tokenVersion = claims.get("tv", Integer.class);

    // Chặn tức thời (Stale Token Prevention)
    User user = userRepository.findByEmail(email).orElseThrow();
    if (!user.getTokenVersion().equals(tokenVersion)) {
        // Token này thuộc về một phiên bản quá khứ đã bị thu hồi
        throw new RuntimeException("Token was revoked"); 
    }

    // Role setup
    List<String> roles = claims.get("roles", List.class);
    List<GrantedAuthority> authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .collect(Collectors.toList());

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(auth);
    chain.doFilter(request, response);
}
```

### Ngăn IDOR bằng SpEL Security

```java
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    // CHỈ dành cho chính user đang login (ID trùng) HOẶC là ADMIN
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @PutMapping("/{id}")
    public Response updateProfile(@PathVariable Long id, @RequestBody ProfileDto dto) {
        // an toàn tuyệt đối, user A gửi request ID của B sẽ ăn 403 Forbidden
    }
}
```

---

## 7. Exception handling & validation

### 3 Lỗi mới cập nhật cần xử lý:

- Token đúng chữ ký, nhưng bị thu hồi (Version sai) -> Trả về `401 Unauthorized` (Message: *Session expired or access revoked*).
- User tự tiện lấy object người khác (Lỗi IDOR / SpEL chặn lại) -> Bắn `AccessDeniedException` -> Catch ở global và trả `403 Forbidden` (Message: *You are not allowed to access this resource*).
- Mass assignment json báo lỗi (nếu validator chặt) -> Trả `400 Bad Request`.

---

## 8. Checklist tự test bảo mật (Red-Team)

1. **Test Leo quyền lúc tạo mới (Mass Assignment):** Lấy Postman gọi `/api/auth/register`, cố tình chèn `"role": "ADMIN"`, `"tokenVersion": 99`. Vào DB check role vẫn chỉ là `USER` => ĐẠT.
2. **Test IDOR (Broken Object Level Authorization):** 
   - Đăng nhập Account `test1` (ID=1). 
   - Lấy token đó gọi API `PUT /api/profiles/2`. 
   - Nếu trả về `400/200` => XỊT (Lỗ hổng chết người). 
   - Nếu trả về `403 Forbidden` => ĐẠT.
3. **Test Thu Hồi Quyền Tức Thì (Ban user / Đổi token version):** 
   - Đăng nhập, lấy được Token A. 
   - Xung đột xảy ra: Sửa trong DB đổi `tokenVersion` thành `2`. 
   - Đem Token A ném vào API `/api/profiles/1` -> Ăn ngay `401 Unauthorized` => ĐẠT.
4. **Test Role Hierarchy:** Đăng nhập dưới dạng ADMIN, gọi endpoint vốn chỉ định `@PreAuthorize("hasRole('USER')")` mà vẫn thành công (không bị lỗi 403) => ĐẠT.

---

## 9. Follow-up nâng cấp (Tương lai)

- Nếu sau này hệ thống mở rộng và có sẵn Server Redis lớn: Loại bỏ query DB check `token_version` trong Security Filter. Chuyển sang ném thẳng những token cần bị thu hồi vào một `blacklist:` trong Redis.
- Chuyển `Role` thành bảng DB chuẩn thay vì Enum nếu xuất hiện khái niệm `Permission` cấp phân quyền sâu hơn (như `CREATE_POST`, `EDIT_PAYMENT`).
- Cấu hình Spring Security ghi lại Log các hành động thất bại (401, 403) vào 1 module Logging để nhận diện nếu hacker lạm dụng hệ thống.
