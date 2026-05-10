# Lý Thuyết Spring Boot Chuyên Sâu - Module Product

Tài liệu này không chỉ giải thích code trong dự án, mà còn phân tích nguồn gốc, lý do ra đời của từng công nghệ, và cách người ta từng làm việc trước khi có những công nghệ này.

---

## 1. JPA Auditing (`@EnableJpaAuditing` & Entity Listeners)

### 📖 Định nghĩa
JPA Auditing là một bộ công cụ của Spring Data JPA, cho phép hệ thống tự động ghi lại vết (audit) của các thao tác trên thực thể (Entity). Điển hình là việc tự động điền `thời gian tạo` (Created Date), `người tạo` (Created By), `thời gian sửa đổi` (Last Modified Date) vào database mà không cần lập trình viên phải gọi hàm thủ công.

### 💡 Lý do ra đời
Bất kỳ một hệ thống phần mềm thực tế nào cũng cần lưu lại lịch sử tạo mới và cập nhật bản ghi để phục vụ việc giải quyết tranh chấp, tìm lỗi (debug) và theo dõi dữ liệu (monitoring). Nếu hệ thống có hàng trăm bảng, việc xử lý thời gian lặp đi lặp lại rất tốn thời gian. Auditing sinh ra để "Tự động hóa" vòng lặp nhàm chán này.

### 🕰️ Kỷ nguyên trước khi có nó
- **Cách 1 - Ở mức mã nguồn (Code-level)**: Developer phải lặp lại lệnh `entity.setCreatedAt(LocalDateTime.now())` ở hằng trăm chỗ gọi `repository.save()`. Nếu lỡ tay quên, dữ liệu ngày tháng sẽ bằng null hoặc sai lệch.
- **Cách 2 - Ở mức Database (DB-level)**: Các DBA thường viết các `Trigger` (Ví dụ `BEFORE INSERT`) ngay dưới Database hoặc đặt default value ở table (`DEFAULT CURRENT_TIMESTAMP`). Nhược điểm là logic hệ thống bị phân mảnh, những dev chỉ đọc code Java sẽ không hiểu vì sao field đó lại tự có dữ liệu. Khó debug.

### 🚀 Áp dụng trong dự án `sport_pro_be`
- Chúng ta dùng class `AbstractAuditingEntity` chứa các cột dùng chung, đánh dấu bằng `@MappedSuperclass` để các bảng khác (`Product`, `ProductVariant`...) kế thừa mà không cần tạo bảng mới.
- Cấu hình `@EntityListeners(AuditingEntityListener.class)` kích hoạt "tai nghe" của Spring JPA, để ngay khi thực thể chuyển trạng thái chuẩn bị vào DB, hệ thống sẽ chèn giờ vào các thuộc tính gắn nhãn `@CreatedDate` và `@LastModifiedDate`.

---

## 2. Spring Data JPA Repository (Giao diện thay vì Class)

### 📖 Định nghĩa
Spring Data JPA là một lớp (layer) bọc bên trên JPA (thường là bộ máy Hibernate). Thay vì phải viết các câu lệnh truy vấn hay quản lý Session phiền phức, bạn chỉ cần khai báo một "Interface" kế thừa `JpaRepository`, Spring sẽ tự động sinh mã nguồn ảo lúc runtime (Runtime Proxies) để thực thi lệnh cho bạn.

### 💡 Lý do ra đời
Việc viết các truy vấn Database (CRUD - Tạo, Đọc, Sửa, Xóa) chiếm đến 60% thời lượng của một dự án thông thường, nhưng chúng hoàn toàn có cấu trúc giống hệt nhau ở mọi bảng. Viết tay từng hàm truy vấn chỉ tạo ra vô số dòng code vô giá trị và dễ sinh lỗi typo (đánh máy sai).

### 🕰️ Kỷ nguyên trước khi có nó
Sử dụng JDBC thuần (Java Database Connectivity):
```java
// Cơn ác mộng JDBC cũ: Mở kết nối -> Viết SQL -> Map từng cột
String sql = "SELECT * FROM product WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
if (rs.next()) {
    Product p = new Product();
    p.setId(rs.getLong("id"));
    p.setName(rs.getString("name"));
    //... Map 20 field nữa ...
}
```
Nhìn đoạn code trên, bạn có thể thấy nó rất khổ sở, phụ thuộc chặt chẽ vào cấu trúc bảng. Đổi tên 1 cột trong Database là phải sửa tay hằng loạt chỗ code Java.

### 🚀 Áp dụng trong dự án
Ta chỉ việc khai báo:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
```
Nhờ cơ chế **Query Derivation** (Suy luận truy vấn từ tên hàm), Spring phân tích chữ `findBySlug`, tự hiểu bạn muốn gọi lệnh `SELECT * FROM product WHERE slug = ?` và làm tất cả việc kết nối, map dữ liệu trả về kiểu `Optional` an toàn (tránh NullPointer).

---

## 3. Dynamic Query Với JPA Specification & Criteria API

### 📖 Định nghĩa
`Specification` là tính năng thiết kế xoay quanh mẫu (Pattern) Specification của Domain-Driven Design (DDD). Được xây dựng dựa trên `JPA Criteria API`, nó cung cấp một công cụ mạnh mẽ, **an toàn về kiểu (type-safe)** để xây dựng các câu truy vấn linh hoạt (Dynamic query) theo nhiều điều kiện khác nhau tại Runtime.

### 💡 Lý do ra đời
Khi bạn làm API tìm kiếm sản phẩm cho e-commerce, người dùng có thể truyền lên vài chục tổ hợp filter (tìm theo: category, brand, khoảng giá, màu sắc, size...). Có người truyền lên, có người không. Bạn không thể định nghĩa trước tên hàm Spring Data JPA (Ví dụ: `findByCategoryIdAndBrandIdAndGenderAndPriceBetween(...)` - tên hàm sẽ dài như một cuốn từ điển).

### 🕰️ Kỷ nguyên trước khi có nó
- **Sử dụng nối chuỗi HQL/SQL truyền thống**:
```java
String query = "SELECT p FROM Product p WHERE 1=1 ";
if (categoryId != null) query += " AND p.category.id = " + categoryId; // Nguy cơ dính SQL Injection
if (minPrice != null) query += " AND p.price > " + minPrice;
```
Cách này đầy rẫy rủi ro bảo mật (nếu không dùng parameterized). IDE cũng không thể báo lỗi giúp bạn nếu bạn gõ nhầm chữ `p.category.id` thành `p.categori.id` cho đến khi chạy server.

### 🚀 Áp dụng trong dự án
- Nằm trong `ProductSpecification.java`. Khai báo `Predicate` động.
- Nó an toàn về kiểu (Type-safe) thông qua `CriteriaBuilder`.
- Dễ dàng gom nhóm logic: Tách điều kiện filter ra một chỗ tách biệt, ProductService chỉ việc mang Specification bỏ vào `productRepository.findAll(spec, pageable)` để truy vấn.

---

## 4. Phân Trang & Sắp Xếp (Pagination & Sorting) với `Pageable`

### 📖 Định nghĩa
Thay vì trả về hàng vạn dữ liệu cùng lúc làm sập đường truyền mạng, `Pageable` và `Page` là cấu trúc dữ liệu chuẩn của Spring giúp chia nhỏ dữ liệu thành nhiều trang, có thể sắp xếp động.

### 💡 Lý do ra đời
Phân trang là kỹ năng bắt buộc để làm ứng dụng có hiệu suất cao. Tuy nhiên, logic phân trang liên quan đến rất nhiều toán học phức tạp (tính offset từ trang, đếm tổng phần tử để tính số trang cuối...). Đẩy việc này lên Spring giúp code gọn nhẹ.

### 🕰️ Kỷ nguyên trước khi có nó
Cực kỳ cồng kềnh vì bạn phải tự chạy **HAI** câu lệnh DB độc lập:
1. `SELECT count(*) FROM product` (Để biết tổng số sp, từ đó chia ra xem trang web có mấy nút 1 2 3 4).
2. `SELECT * FROM product LIMIT 10 OFFSET 20` (Để lấy đúng data cho trang 3).
Và tự viết code tính toán `offset = page * size`. Đổi sang hệ quản trị DB khác (như SQL Server dùng `OFFSET FETCH`), bạn phải viết lại câu lệnh SQL.

### 🚀 Áp dụng trong dự án
- API nhận trực tiếp tham số `Pageable pageable` (thông qua params URL `?page=0&size=10&sort=id,desc`).
- Gọi DB truyền Pageable vào, trả về cấu trúc dữ liệu khổng lồ: Data danh sách sản phẩm trang đó (`content`), tổng trang (`totalPages`), tổng phần tử (`totalElements`).
- Dùng `products.map(...)` để vừa bảo lưu toàn bộ metadata phân trang, vừa chuyển đổi thực thể `Product` ẩn giấu trong DB ra DTO `ProductListResponse` để giấu thông tin nhạy cảm trước khi ném về cho Client.
