 package com.web.app.config;

import com.web.app.model.*;
import com.web.app.repository.*;
import com.web.app.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Override
    public void run(String... args) throws Exception {
        // Init Admin Account
        if (taiKhoanRepository.findByTenDangNhap("admin").isEmpty()) {
            TaiKhoan admin = TaiKhoan.builder()
                    .tenDangNhap("admin")
                    .matKhau(HashUtil.hashPassword("admin123"))
                    .vaiTro("ADMIN")
                    .trangThai("ACTIVE")
                    .build();
            taiKhoanRepository.save(admin);
            System.out.println("--> Seeded admin account: admin / admin123");
        }

        // Init Default User Account
        if (taiKhoanRepository.findByTenDangNhap("user").isEmpty()) {
            TaiKhoan userAcc = TaiKhoan.builder()
                    .tenDangNhap("user")
                    .matKhau(HashUtil.hashPassword("user123"))
                    .vaiTro("USER")
                    .trangThai("ACTIVE")
                    .build();

            // Create associated customer profile
            KhachHang customer = KhachHang.builder()
                    .hoTen("Nguyễn Văn A")
                    .email("nguyenvana@gmail.com")
                    .soDienThoai("0987654321")
                    .diaChi("123 Đường Lê Lợi, Quận 1, TP. Hồ Chí Minh")
                    .taiKhoan(userAcc)
                    .build();
            khachHangRepository.save(customer);
            System.out.println("--> Seeded customer account: user / user123");
        }

        // Init Categories
        if (danhMucRepository.count() == 0) {
            List<DanhMuc> categories = Arrays.asList(
                    DanhMuc.builder().tenDanhMuc("Mũ Lưỡi Trai").moTa("Mũ lưỡi trai phong cách trẻ trung, năng động").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Snapback").moTa("Mũ Snapback cá tính, đậm chất streetwear").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Bucket (Tai bèo)").moTa("Mũ bucket thời trang, tiện lợi khi đi chơi dã ngoại").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Len (Beanie)").moTa("Mũ len ấm áp cho mùa đông").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Fedora").moTa("Mũ fedora lịch lãm, sang trọng").build()
            );
            danhMucRepository.saveAll(categories);
            System.out.println("--> Seeded categories");
        }

        // Init Brands
        if (thuongHieuRepository.count() == 0) {
            List<ThuongHieu> brands = Arrays.asList(
                    ThuongHieu.builder().tenThuongHieu("Nike").moTa("Thương hiệu thể thao hàng đầu thế giới").build(),
                    ThuongHieu.builder().tenThuongHieu("Adidas").moTa("Thương hiệu phong cách thể thao ba sọc cổ điển").build(),
                    ThuongHieu.builder().tenThuongHieu("MLB").moTa("Thương hiệu thời trang bóng chày Hàn Quốc cực hot").build(),
                    ThuongHieu.builder().tenThuongHieu("New Era").moTa("Thương hiệu mũ snapback và lưỡi trai huyền thoại").build(),
                    ThuongHieu.builder().tenThuongHieu("Puma").moTa("Thương hiệu thời trang thể thao năng động").build()
            );
            thuongHieuRepository.saveAll(brands);
            System.out.println("--> Seeded brands");
        }

        // Seed products if empty
        if (sanPhamRepository.count() == 0) {
            List<DanhMuc> cats = danhMucRepository.findAll();
            List<ThuongHieu> brs = thuongHieuRepository.findAll();

            DanhMuc luoiTrai = cats.stream().filter(c -> c.getTenDanhMuc().equals("Mũ Lưỡi Trai")).findFirst().orElse(cats.get(0));
            DanhMuc snapback = cats.stream().filter(c -> c.getTenDanhMuc().equals("Mũ Snapback")).findFirst().orElse(cats.get(0));
            DanhMuc bucket = cats.stream().filter(c -> c.getTenDanhMuc().equals("Mũ Bucket (Tai bèo)")).findFirst().orElse(cats.get(0));

            ThuongHieu nike = brs.stream().filter(b -> b.getTenThuongHieu().equals("Nike")).findFirst().orElse(brs.get(0));
            ThuongHieu adidas = brs.stream().filter(b -> b.getTenThuongHieu().equals("Adidas")).findFirst().orElse(brs.get(0));
            ThuongHieu mlb = brs.stream().filter(b -> b.getTenThuongHieu().equals("MLB")).findFirst().orElse(brs.get(0));
            ThuongHieu newEra = brs.stream().filter(b -> b.getTenThuongHieu().equals("New Era")).findFirst().orElse(brs.get(0));

            List<SanPham> products = Arrays.asList(
                    SanPham.builder()
                            .tenSanPham("Mũ Lưỡi Trai Nike Heritage86")
                            .moTa("Mũ lưỡi trai Nike Heritage86 làm từ chất liệu cotton thấm hút mồ hôi tốt, kiểu dáng thể thao ôm đầu, màu sắc trung tính dễ phối đồ.")
                            .gia(350000.0)
                            .soLuong(50)
                            .anhUrl("https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=600")
                            .danhMuc(luoiTrai)
                            .thuongHieu(nike)
                            .ngayTao(LocalDateTime.now())
                            .build(),
                    SanPham.builder()
                            .tenSanPham("Mũ Lưỡi Trai Adidas Superlite")
                            .moTa("Thiết kế siêu nhẹ dành cho các hoạt động thể thao ngoài trời như chạy bộ, tennis. Công nghệ chống tia UV bảo vệ da đầu.")
                            .gia(380000.0)
                            .soLuong(30)
                            .anhUrl("https://images.unsplash.com/photo-1534215754734-18e55d13e346?w=600")
                            .danhMuc(luoiTrai)
                            .thuongHieu(adidas)
                            .ngayTao(LocalDateTime.now())
                            .build(),
                    SanPham.builder()
                            .tenSanPham("Mũ Snapback New Era 9FIFTY NY")
                            .moTa("Mũ Snapback NY huyền thoại từ thương hiệu New Era. Phía sau có khấc nhựa điều chỉnh kích thước dễ dàng, phong cách hiphop cực chất.")
                            .gia(490000.0)
                            .soLuong(20)
                            .anhUrl("https://images.unsplash.com/photo-1516257984-b1b4d707412e?w=600")
                            .danhMuc(snapback)
                            .thuongHieu(newEra)
                            .ngayTao(LocalDateTime.now().minusDays(1))
                            .build(),
                    SanPham.builder()
                            .tenSanPham("Mũ Bucket MLB Boston Red Sox")
                            .moTa("Mũ tai bèo (bucket) in họa tiết logo Boston cá tính. Vải kaki dày dặn giữ form tốt, thích hợp cho cả nam và nữ.")
                            .gia(450000.0)
                            .soLuong(25)
                            .anhUrl("https://images.unsplash.com/photo-1529958030586-3aae4ca485ff?w=600")
                            .danhMuc(bucket)
                            .thuongHieu(mlb)
                            .ngayTao(LocalDateTime.now().minusDays(2))
                            .build(),
                    SanPham.builder()
                            .tenSanPham("Mũ Lưỡi Trai MLB LA Dodgers")
                            .moTa("Mũ MLB thêu chữ LA nổi bật màu xanh dương thanh lịch. Chất liệu cao cấp, đường chỉ thêu tỉ mỉ chuẩn auth.")
                            .gia(420000.0)
                            .soLuong(40)
                            .anhUrl("https://images.unsplash.com/photo-1576871337632-b9aef4c17ab9?w=600")
                            .danhMuc(luoiTrai)
                            .thuongHieu(mlb)
                            .ngayTao(LocalDateTime.now().minusDays(3))
                            .build(),
                    SanPham.builder()
                            .tenSanPham("Mũ Bucket Nike Sportswear")
                            .moTa("Mũ bucket Nike chất liệu dù chống nước nhẹ, thiết kế thêu logo Nike Swoosh nhỏ nhắn tinh tế.")
                            .gia(390000.0)
                            .soLuong(15)
                            .anhUrl("https://images.unsplash.com/photo-1534215754734-18e55d13e346?w=600")
                            .danhMuc(bucket)
                            .thuongHieu(nike)
                            .ngayTao(LocalDateTime.now().minusDays(4))
                            .build()
            );
            sanPhamRepository.saveAll(products);
            System.out.println("--> Seeded sample products");
        }

        // Correct the legacy sample URL that points to a shirt instead of a hat.
        sanPhamRepository.findAll().stream()
                .filter(product -> "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600"
                        .equals(product.getAnhUrl()))
                .forEach(product -> {
                    product.setAnhUrl("https://images.unsplash.com/photo-1534215754734-18e55d13e346?w=600");
                    sanPhamRepository.save(product);
                });

        // A product that has variants always exposes the sum of its variant stock.
        sanPhamRepository.findAll().forEach(product -> {
            List<BienTheSanPham> variants = bienTheSanPhamRepository
                    .findBySanPhamIdOrderByMauSacAscKichCoAsc(product.getId());
            if (!variants.isEmpty()) {
                int totalStock = variants.stream().mapToInt(BienTheSanPham::getSoLuong).sum();
                if (!Integer.valueOf(totalStock).equals(product.getSoLuong())) {
                    product.setSoLuong(totalStock);
                    sanPhamRepository.save(product);
                }
            }
        });
    }
}
