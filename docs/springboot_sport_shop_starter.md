# Bộ khởi tạo dự án thực chiến — Sport Shop + Custom Printing (Spring Boot)

## 1) Mục tiêu

Bộ khởi tạo này dành cho dự án:

- bán đồ thể thao
- có client side và admin side riêng
- hỗ trợ in ấn theo yêu cầu: tên, số áo, logo, thiết kế
- dễ scale theo hướng modular monolith

## 2) Stack chốt

- Backend: Java 21, Spring Boot 4
- Database: PostgreSQL
- Cache: Redis
- Migration: Flyway
- Auth: Spring Security + JWT
- Docs: springdoc OpenAPI
- Mapping: MapStruct
- Boilerplate: Lombok
- Build tool: Maven
- Infra: Docker Compose

## 3) Cấu trúc monorepo

```text
sport-ecommerce/
  apps/
    backend/
    client-web/
    admin-web/
  infra/
    docker/
    nginx/
  docs/
    architecture/
    api/
    db/
```

## 4) Cấu trúc backend

```text
apps/backend/
  src/main/java/com/sportshop/
    common/
      config/
      security/
      exception/
      response/
      util/
      constant/
    modules/
      auth/
        controller/
        service/
        domain/
        repository/
        dto/
        mapper/
      user/
      catalog/
      category/
      cart/
      order/
      payment/
      customization/
      pricing/
      inventory/
      media/
      notification/
      report/
      admin/
    SportShopApplication.java
  src/main/resources/
    application.yml
    application-dev.yml
    application-prod.yml
    db/migration/
  pom.xml
  Dockerfile
```

## 5) pom.xml đề xuất

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.4</version>
        <relativePath/>
    </parent>

    <groupId>com.sportshop</groupId>
    <artifactId>sport-shop-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>sport-shop-api</name>
    <description>Sport ecommerce with customization</description>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.8.6</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.38</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 6) application.yml

```yaml
spring:
  application:
    name: sport-shop-api
  profiles:
    active: dev

server:
  port: 8080

app:
  jwt:
    secret: ${JWT_SECRET:change-me-super-secret-key-change-me}
    access-token-expiration-minutes: 60
    refresh-token-expiration-days: 14
```

## 7) application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sport_shop
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  flyway:
    enabled: true
  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    org.springframework.security: INFO
    com.sportshop: DEBUG
```

## 8) application-prod.yml

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}

logging:
  level:
    root: INFO
    com.sportshop: INFO
```

## 9) Common base classes

### BaseEntity.java

```java
package com.sportshop.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

### ApiResponse.java

```java
package com.sportshop.common.response;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }
}
```

### GlobalExceptionHandler.java

```java
package com.sportshop.common.exception;

import com.sportshop.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null ? "Invalid" : fieldError.getDefaultMessage(),
                        (a, b) -> a
                ));

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Validation failed",
                "errors", errors,
                "timestamp", Instant.now()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "timestamp", Instant.now()
                ));
    }
}
```

## 10) Security starter

### SecurityConfig.java

```java
package com.sportshop.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/categories/**", "/api/v1/brands/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 11) Auth module starter

### User.java

```java
package com.sportshop.modules.auth.domain;

import com.sportshop.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active;
}
```

### UserRole.java

```java
package com.sportshop.modules.auth.domain;

public enum UserRole {
    CUSTOMER,
    STAFF,
    ADMIN,
    SUPER_ADMIN
}
```

### UserRepository.java

```java
package com.sportshop.modules.auth.repository;

import com.sportshop.modules.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### RegisterRequest.java

```java
package com.sportshop.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password
) {}
```

### AuthService.java

```java
package com.sportshop.modules.auth.service;

import com.sportshop.modules.auth.domain.User;
import com.sportshop.modules.auth.domain.UserRole;
import com.sportshop.modules.auth.dto.RegisterRequest;
import com.sportshop.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
```

### AuthController.java

```java
package com.sportshop.modules.auth.controller;

import com.sportshop.common.response.ApiResponse;
import com.sportshop.modules.auth.domain.User;
import com.sportshop.modules.auth.dto.RegisterRequest;
import com.sportshop.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Register successful", authService.register(request));
    }
}
```

## 12) Catalog module starter

### Product.java

```java
package com.sportshop.modules.catalog.domain;

import com.sportshop.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Boolean active;
}
```

### ProductRepository.java

```java
package com.sportshop.modules.catalog.repository;

import com.sportshop.modules.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlugAndActiveTrue(String slug);
    Page<Product> findByActiveTrue(Pageable pageable);
}
```

### ProductResponse.java

```java
package com.sportshop.modules.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String shortDescription,
        BigDecimal basePrice
) {}
```

### ProductService.java

```java
package com.sportshop.modules.catalog.service;

import com.sportshop.modules.catalog.dto.ProductResponse;
import com.sportshop.modules.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getSlug(),
                        p.getShortDescription(),
                        p.getBasePrice()
                ));
    }
}
```

### ProductController.java

```java
package com.sportshop.modules.catalog.controller;

import com.sportshop.common.response.ApiResponse;
import com.sportshop.modules.catalog.dto.ProductResponse;
import com.sportshop.modules.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(productService.getProducts(PageRequest.of(page, size)));
    }
}
```

## 13) Pricing + customization starter

### PrintType.java

```java
package com.sportshop.modules.customization.domain;

public enum PrintType {
    NAME_PRINT,
    NUMBER_PRINT,
    LOGO_PRINT,
    FULL_CUSTOM_PRINT
}
```

### PrintPosition.java

```java
package com.sportshop.modules.customization.domain;

public enum PrintPosition {
    FRONT_CHEST,
    BACK,
    LEFT_SLEEVE,
    RIGHT_SLEEVE
}
```

### PricingRule.java

```java
package com.sportshop.modules.pricing.domain;

import com.sportshop.modules.customization.domain.PrintPosition;
import com.sportshop.modules.customization.domain.PrintType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRule {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PrintType printType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PrintPosition printPosition;

    @Column(precision = 10, scale = 2)
    private BigDecimal minArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxArea;

    private Integer minQuantity;
    private Integer maxQuantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal setupFee;

    @Column(nullable = false)
    private Boolean active;
}
```

### EstimateCustomizationRequest.java

```java
package com.sportshop.modules.customization.dto;

import com.sportshop.modules.customization.domain.PrintPosition;
import com.sportshop.modules.customization.domain.PrintType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EstimateCustomizationRequest(
        @NotNull PrintType printType,
        @NotNull PrintPosition printPosition,
        @NotNull BigDecimal area,
        @NotNull @Min(1) Integer quantity
) {}
```

### PricingService.java

```java
package com.sportshop.modules.pricing.service;

import com.sportshop.modules.customization.dto.EstimateCustomizationRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    public BigDecimal estimate(EstimateCustomizationRequest request) {
        BigDecimal base = switch (request.printType()) {
            case NAME_PRINT -> new BigDecimal("20000");
            case NUMBER_PRINT -> new BigDecimal("25000");
            case LOGO_PRINT -> new BigDecimal("30000");
            case FULL_CUSTOM_PRINT -> new BigDecimal("50000");
        };

        return base.multiply(BigDecimal.valueOf(request.quantity()));
    }
}
```

### CustomizationController.java

```java
package com.sportshop.modules.customization.controller;

import com.sportshop.common.response.ApiResponse;
import com.sportshop.modules.customization.dto.EstimateCustomizationRequest;
import com.sportshop.modules.pricing.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/customizations")
@RequiredArgsConstructor
public class CustomizationController {
    private final PricingService pricingService;

    @PostMapping("/estimate")
    public ApiResponse<BigDecimal> estimate(@Valid @RequestBody EstimateCustomizationRequest request) {
        return ApiResponse.ok("Estimate successful", pricingService.estimate(request));
    }
}
```

## 14) Migration starter

### V1\_\_init_extensions.sql

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

### V2\_\_create_users_table.sql

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### V3\_\_create_products_table.sql

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    short_description VARCHAR(500),
    description TEXT,
    base_price NUMERIC(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_active ON products(active);
```

### V4\_\_create_pricing_rules_table.sql

```sql
CREATE TABLE pricing_rules (
    id UUID PRIMARY KEY,
    print_type VARCHAR(50) NOT NULL,
    print_position VARCHAR(50) NOT NULL,
    min_area NUMERIC(10,2),
    max_area NUMERIC(10,2),
    min_quantity INT,
    max_quantity INT,
    unit_price NUMERIC(12,2) NOT NULL,
    setup_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

## 15) Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/sport-shop-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

## 16) docker-compose.yml

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:18
    container_name: sportshop-postgres
    environment:
      POSTGRES_DB: sport_shop
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:8
    container_name: sportshop-redis
    ports:
      - "6379:6379"

  api:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: sportshop-api
    depends_on:
      - postgres
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/sport_shop
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: please-change-this-secret
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

## 17) Checklist khởi tạo theo thứ tự

1. Tạo repo monorepo
2. Tạo `apps/backend`
3. Dán `pom.xml`
4. Tạo `application.yml`, `application-dev.yml`, `application-prod.yml`
5. Tạo `docker-compose.yml`
6. Tạo `common/` trước
7. Tạo `auth` module
8. Tạo `catalog` module
9. Tạo `pricing/customization` module
10. Thêm Flyway migrations
11. Chạy local bằng Postgres + Redis
12. Mở Swagger test API
13. Sau đó mới làm cart, order, media, inventory, admin

## 18) Thứ tự module nên code tiếp

- auth
- category
- catalog
- cart
- order
- customization
- pricing rule dynamic
- media upload
- inventory
- admin dashboard

## 19) Những phần cần nâng cấp sau starter

- JWT filter + refresh token hoàn chỉnh
- phân quyền chi tiết bằng permission
- pagination response chuẩn riêng
- media upload S3/MinIO
- cart/order đầy đủ
- payment integration
- audit log
- Redis cache thật sự
- test integration với Testcontainers

## 20) Quy tắc kiến trúc phải giữ

- controller mỏng
- business logic nằm ở service
- không trả entity raw ra frontend trong production
- mọi thay đổi schema qua Flyway
- pricing không hard-code lâu dài
- module tách rõ ngay từ đầu

## 21) Triển khai thực tế: Login email + password + OTP email (đã áp dụng trong repo hiện tại)

### 21.1 Contract API

- `POST /api/auth/register`
  - Request:
    - `email`
    - `password` (tối thiểu 8 ký tự)
  - Response: `201 Created` + message đăng ký
- `POST /api/auth/login`
  - Request:
    - `email`
    - `password`
  - Response: `200 OK` + message đã gửi OTP
- `POST /api/auth/verify-otp`
  - Request:
    - `email`
    - `otp` (6 chữ số)
  - Response: `200 OK` + access token (JWT)

### 21.2 Flow nghiệp vụ

1. User đăng ký email/password, mật khẩu được hash bằng BCrypt.
2. User login bằng email/password:
   - kiểm tra thông tin đăng nhập
   - sinh OTP 6 số
   - lưu OTP vào DB kèm thời hạn
   - gửi OTP qua email
3. User gọi verify OTP:
   - OTP đúng + chưa dùng + chưa hết hạn
   - đánh dấu OTP đã dùng
   - set `emailVerified=true`
   - trả JWT để dùng cho các API cần auth

### 21.3 Các điểm bảo vệ đã có

- Cooldown gửi lại OTP (`app.auth.otp-resend-cooldown-seconds`)
- OTP có TTL (`app.auth.otp-expiration-minutes`)
- OTP one-time-use (`used=true` sau verify)
- Không lộ chi tiết sai ở bước login (email/mật khẩu sai dùng chung 1 message)

### 21.4 Cấu hình cần thiết (`application.properties`)

- DB local mặc định: H2 in-memory (để chạy nhanh ở môi trường dev)
- SMTP:
  - `spring.mail.host`
  - `spring.mail.port`
  - `spring.mail.username`
  - `spring.mail.password`
- Auth:
  - `app.auth.jwt-secret` (bắt buộc >= 32 ký tự)
  - `app.auth.jwt-expiration-minutes`
  - `app.auth.otp-expiration-minutes`
  - `app.auth.otp-resend-cooldown-seconds`
  - `app.auth.mail-from`

### 21.5 Ví dụ payload nhanh

Register:

```json
{
  "email": "demo@sportpro.vn",
  "password": "Password@123"
}
```

Login:

```json
{
  "email": "demo@sportpro.vn",
  "password": "Password@123"
}
```

Verify OTP:

```json
{
  "email": "demo@sportpro.vn",
  "otp": "123456"
}
```

### 21.6 Edge cases cần kiểm thử thêm

- OTP sai
- OTP hết hạn
- verify OTP đã dùng
- spam login nhận OTP liên tục (cooldown)
- SMTP lỗi/timeout

### 21.7 Nâng cấp tiếp theo (khuyến nghị production)

- Lưu OTP dạng hash thay vì plain text
- Giới hạn số lần nhập sai OTP
- JWT refresh token + revoke token
- Flyway migration cho bảng `app_users`, `email_otp_codes`
- Tách provider gửi mail (SMTP/SES/SendGrid)
