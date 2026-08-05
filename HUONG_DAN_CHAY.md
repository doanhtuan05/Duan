# Hướng Dẫn Đóng Gói Và Chạy Dự Án Website Bán Mũ Thời Trang

Tài liệu này hướng dẫn cách build, đóng gói thành 1 file chạy duy nhất (`.jar`) và khởi chạy ứng dụng.

---

## 1. Cấu Hình Cơ Sở Dữ Liệu (SQL Server)
Trước khi chạy ứng dụng, hãy đảm bảo SQL Server đang hoạt động và cơ sở dữ liệu đã được cấu hình đúng:
* **Tên Database**: `WebsiteBanMu`
* **Tài khoản truy cập**:
  * **Username**: `banmu_user`
  * **Password**: `Password123`
* **Địa chỉ kết nối**: `localhost:1433`

---

## 2. Lệnh Đóng Gói Dự Án Thành 1 File Duy Nhất
Mở terminal tại thư mục gốc của dự án (`app/`) và chạy lệnh:

```powershell
# Sử dụng Maven Wrapper đi kèm dự án (Khuyên dùng)
.\mvnw.cmd clean package -DskipTests

# Hoặc nếu hệ thống của bạn đã cài đặt sẵn Maven toàn cục
mvn clean package -DskipTests
```

Sau khi chạy xong và hiển thị `BUILD SUCCESS`, bạn sẽ tìm thấy file chạy độc lập tại đường dẫn:
* **`target/app-0.0.1-SNAPSHOT.jar`**

---

## 3. Lệnh Khởi Chạy File JAR Đã Đóng Gói
Sau khi đóng gói thành công, khởi chạy ứng dụng bằng lệnh:

```powershell
java -jar target/app-0.0.1-SNAPSHOT.jar
```

---

## 4. Lệnh Chạy Dự Án Ở Chế Độ Phát Triển (Development)
Nếu bạn đang chỉnh sửa mã nguồn và muốn chạy trực tiếp không cần đóng gói:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 5. Truy Cập Ứng Dụng
Sau khi ứng dụng khởi chạy thành công trên cổng `8085`:
* **Địa chỉ trang chủ**: [http://localhost:8085](http://localhost:8085)
* **Tài khoản Admin mặc định**:
  * **Tên đăng nhập**: `admin`
  * **Mật khẩu**: `admin123`
* **Tài khoản Người dùng (Customer) mặc định**:
  * **Tên đăng nhập**: `user`
  * **Mật khẩu**: `user123`
