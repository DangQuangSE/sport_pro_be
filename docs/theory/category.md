# Lý Thuyết Spring Boot Chuyên Sâu - Module Category

Tài liệu này không chỉ giải thích code trong dự án, mà còn phân tích nguồn gốc, lý do ra đời của từng công nghệ, và cách người ta từng làm việc trước khi có những công nghệ này.

---

## 1. Kiến Trúc Layered (Phân Tầng: Controller -> Service -> Repository)

### 📖 Định nghĩa
Đây là mẫu kiến trúc phần mềm tiêu chuẩn chia dự án thành 3 tầng chức năng riêng biệt:
- **Controller (`@RestController`)**: Cửa ngõ, làm việc với giao thức HTTP (nhận Request, kiểm tra tham số, trả về HTTP Status và JSON Response).
- **Service (`@Service`)**: Trái tim nghiệp vụ (Business Logic). Nó xử lý các luật lệ, tính toán, và quyết định khi nào thì báo lỗi.
- **Repository (`@Repository`)**: Quản lý thao tác cơ sở dữ liệu (Đọc, ghi, xóa).

### 💡 Lý do ra đời
Áp dụng triết lý "Separation of Concerns" (Chia để trị). Mỗi tầng chỉ giỏi một việc. Điều này giúp dự án dễ bảo trì, dễ viết kiểm thử (Unit Test) và dễ làm việc nhóm. Ví dụ: Người viết giao diện API (Controller) không cần quan tâm DB ở dưới là MySQL hay Oracle. Người viết logic (Service) không cần bận tâm Client gửi dữ liệu lên qua HTTP hay gRPC.

### 🕰️ Kỷ nguyên trước khi có nó
Vào những năm 2000s với **Java Servlet thuần**, khái niệm "God Class" (Lớp Chúa tể) lên ngôi. Một file Servlet hứng request HTTP, lấy tham số, tự mở kết nối JDBC, viết câu SQL nối chuỗi String chọc vào Database, xử lý logic, rồi cuối cùng in ra thẻ `<html>` kết quả ngay trong cùng một hàm dài hàng nghìn dòng. Code cực kỳ khó đọc, không thể tái sử dụng (spaghetti code) và ác mộng khi debug.

### 🚀 Áp dụng trong dự án `sport_pro_be`
- Client gọi API -> Gặp `CategoryController` (nhận JSON).
- `CategoryController` đưa DTO cho `CategoryServiceImpl` xử lý logic (như check Category cha có tồn tại không, chống tham chiếu vòng - Circular Reference).
- Service gọi `CategoryRepository` để truy vấn/lưu dữ liệu vào DB.

---

## 2. Dependency Injection & `@RequiredArgsConstructor`

### 📖 Định nghĩa
Dependency Injection (DI - Tiêm phụ thuộc) là cốt lõi của Spring. Một đối tượng thay vì tự tạo (new) đối tượng khác để dùng, nó sẽ yêu cầu Spring "Bơm" đối tượng đó cho nó thông qua Constructor (Hàm khởi tạo).

`@RequiredArgsConstructor` là một tính năng của thư viện Lombok. Nó tự động sinh ra một Constructor chứa tham số cho toàn bộ các biến được đánh dấu là `private final`.

### 💡 Lý do ra đời
Để giảm sự phụ thuộc cứng (Loose Coupling). Việc sử dụng Constructor Injection được chính cha đẻ Spring khuyến nghị vì:
1. Bạn không thể tạo ra Object (như Service) nếu chưa cung cấp đủ phụ thuộc (Repository) cho nó. (Tránh lỗi NullPointerException lúc chạy).
2. Biến được đánh dấu `final` đảm bảo rằng phụ thuộc (Repository) không bị gán đè hay thay đổi bất ngờ trong quá trình Service đang hoạt động.
3. Cực kỳ dễ dàng cho việc viết Unit Test (Chỉ cần truyền Mock Object qua constructor).

### 🕰️ Kỷ nguyên trước khi có nó
- **Cách 1 - Tự khởi tạo**: Code sẽ là `private CategoryRepository repo = new CategoryRepositoryImpl();`. Việc này bó chết Service vào đúng một class Impl, muốn đổi DB khác phải sửa lại code.
- **Cách 2 - Dùng `@Autowired` trên field (Field Injection)**: 
```java
@Autowired
private CategoryRepository repo;
```
Trông có vẻ ngắn gọn, nhưng thực tế nó vi phạm nguyên lý thiết kế: class bị phụ thuộc vào Reflection của Spring. Nếu ta đem test class này bên ngoài môi trường Spring, biến `repo` sẽ vĩnh viễn là `null` vì không có hàm set/constructor nào để tiêm giá trị vào.

### 🚀 Áp dụng trong dự án
Chúng ta chỉ cần viết:
```java
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepository categoryRepository;
}
```
Mọi thứ còn lại (Tạo hàm constructor, bơm Bean từ vùng nhớ IoC) Spring và Lombok tự lo. Rất sạch sẽ!

---

## 3. Transaction Management (`@Transactional`)

### 📖 Định nghĩa
Một "Transaction" (Giao dịch) là một tập hợp các lệnh cơ sở dữ liệu được thực thi như một đơn vị duy nhất. **Quy tắc ALL OR NOTHING** (Tất cả thành công, hoặc không gì cả). `@Transactional` là một AOP Aspect của Spring dùng để bọc các phương thức lại trong một giao dịch cơ sở dữ liệu.

### 💡 Lý do ra đời
Khi thực hiện các nghiệp vụ phức tạp liên quan đến nhiều bảng (ví dụ: Chuyển tiền từ A sang B; Tạo đơn hàng và Trừ kho). Nếu bước 1 thành công mà bước 2 bị lỗi (mất mạng, lỗi logic), dữ liệu sẽ bị lệch (Tiền A mất nhưng B không nhận được). Transaction đảm bảo nếu có bất cứ lỗi (RuntimeException) nào văng ra, Database sẽ khôi phục lại trạng thái ban đầu (Rollback).

### 🕰️ Kỷ nguyên trước khi có nó
Với JDBC truyền thống, bạn phải viết rập khuôn một đống code rườm rà:
```java
Connection conn = dataSource.getConnection();
try {
    conn.setAutoCommit(false); // Bắt đầu Transaction
    // ... Code SQL bước 1
    // ... Code SQL bước 2
    conn.commit(); // Thành công thì lưu
} catch (Exception e) {
    conn.rollback(); // Lỗi thì đảo ngược
} finally {
    conn.close();
}
```
Code nghiệp vụ bị che lấp bởi quá nhiều "Code quản lý cơ sở hạ tầng". `@Transactional` dọn dẹp sạch sẽ đống lộn xộn này.

### 🚀 Áp dụng trong dự án
- Ở `CategoryServiceImpl`, hàm `createCategory` hay `updateCategory` đều gắn `@Transactional`. Khi tạo hoặc update, ta thực hiện nhiều dòng code (ví dụ: tạo entity, sinh slug duy nhất bằng vòng lặp while, gán dữ liệu). Nếu đoạn giữa ném ra lỗi, không có dòng DB nào được lưu sai lệch cả.

---

## 4. Xử lý Logic Memory bằng Java 8 Stream API

### 📖 Định nghĩa
Stream API là một khái niệm được đưa vào từ Java 8, cho phép xử lý các tập hợp dữ liệu (Collections) theo phong cách khai báo (Declarative Programming) thay vì viết vòng lặp rườm rà.

### 💡 Lý do ra đời
Giúp thao tác với dữ liệu List/Set/Map một cách ngắn gọn, súc tích, mô tả được "Bạn muốn làm gì" (Filter, Map, Group) thay vì "Bạn làm việc đó thế nào" (Vòng lặp for, tạo index, điều kiện if). Nó có khả năng tính toán song song (Parallel execution) để khai thác đa luồng CPU.

### 🕰️ Kỷ nguyên trước khi có nó
Ví dụ để gom nhóm List danh mục theo Category cha:
```java
// Java 7 trở xuống
Map<Long, List<Category>> map = new HashMap<>();
for (Category c : categories) {
    if (c.getParentId() != null) {
        if (!map.containsKey(c.getParentId())) {
            map.put(c.getParentId(), new ArrayList<>());
        }
        map.get(c.getParentId()).add(c);
    }
}
```
Cấu trúc lệnh lặp và điều kiện if làm che mờ đi ý định thực sự của nghiệp vụ.

### 🚀 Áp dụng trong dự án
Trong API dựng `CategoryTree`:
- Chỉ cần chọc xuống DB 1 lần duy nhất lấy lên mọi dữ liệu.
- Dùng `stream().filter(...).collect(Collectors.groupingBy(...))` để chia nhóm cây con.
- Kỹ thuật này giúp giải quyết bài toán đệ quy N-tầng mà không bị lỗi N+1 Query (Lỗi N+1 là lỗi cứ mỗi tầng thư mục lại chạy 1 câu query xuống DB -> Rất dễ làm sập Server nếu dữ liệu lớn). Mọi cấu trúc đệ quy (hàm `buildTreeResponse`) đều diễn ra với tốc độ cực cao trên thanh RAM.
