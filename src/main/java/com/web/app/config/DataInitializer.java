package com.web.app.config;

import com.web.app.model.*;
import com.web.app.repository.*;
import com.web.app.util.HashUtil;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

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
    private DataSource dataSource;

    @Autowired
    private EntityManager entityManager;

    @Override
    public void run(String... args) throws Exception {

        // ===================== ADMIN =====================
        if (taiKhoanRepository.findByTenDangNhap("admin").isEmpty()) {
            TaiKhoan admin = TaiKhoan.builder()
                    .tenDangNhap("admin")
                    .matKhau(HashUtil.hashPassword("admin123"))
                    .vaiTro("ADMIN")
                    .trangThai("ACTIVE")
                    .build();

            taiKhoanRepository.save(admin);
            System.out.println("--> Seeded admin account");
        }

        // ===================== USER =====================
        if (taiKhoanRepository.findByTenDangNhap("user").isEmpty()) {

            TaiKhoan userAcc = TaiKhoan.builder()
                    .tenDangNhap("user")
                    .matKhau(HashUtil.hashPassword("user123"))
                    .vaiTro("USER")
                    .trangThai("ACTIVE")
                    .build();

            KhachHang customer = KhachHang.builder()
                    .hoTen("Nguyễn Văn A")
                    .email("nguyenvana@gmail.com")
                    .soDienThoai("0987654321")
                    .diaChi("123 Đường Lê Lợi")
                    .taiKhoan(userAcc)
                    .build();

            khachHangRepository.save(customer);
            System.out.println("--> Seeded user account");
        }

        // ===================== DANH MỤC =====================
        if (danhMucRepository.count() == 0) {

            danhMucRepository.saveAll(Arrays.asList(
                    DanhMuc.builder().tenDanhMuc("Mũ Lưỡi Trai").moTa("Mũ lưỡi trai").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Snapback").moTa("Snapback").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Bucket (Tai bèo)").moTa("Bucket").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Len (Beanie)").moTa("Beanie").build(),
                    DanhMuc.builder().tenDanhMuc("Mũ Fedora").moTa("Fedora").build()
            ));

            System.out.println("--> Seeded categories");
        }

        // ===================== THƯƠNG HIỆU =====================
        if (thuongHieuRepository.count() == 0) {

            thuongHieuRepository.saveAll(Arrays.asList(
                    ThuongHieu.builder().tenThuongHieu("Nike").moTa("Nike").build(),
                    ThuongHieu.builder().tenThuongHieu("Adidas").moTa("Adidas").build(),
                    ThuongHieu.builder().tenThuongHieu("MLB").moTa("MLB").build(),
                    ThuongHieu.builder().tenThuongHieu("New Era").moTa("New Era").build(),
                    ThuongHieu.builder().tenThuongHieu("Puma").moTa("Puma").build()
            ));

            System.out.println("--> Seeded brands");
        }

        // =====================================================
        // DEBUG
        // =====================================================

        System.out.println("\n================ DEBUG =================");

        Connection conn = dataSource.getConnection();

        System.out.println("JDBC URL  : " + conn.getMetaData().getURL());
        System.out.println("User      : " + conn.getMetaData().getUserName());

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT @@SERVERNAME, DB_NAME()");

        if (rs.next()) {
            System.out.println("SERVER    : " + rs.getString(1));
            System.out.println("DATABASE  : " + rs.getString(2));
        }

        rs.close();

        rs = st.executeQuery("SELECT COUNT(*) FROM SanPham");

        if (rs.next()) {
            System.out.println("COUNT SQL : " + rs.getInt(1));
        }

        rs.close();
        st.close();
        conn.close();

        System.out.println(entityManager
                .createNativeQuery("SELECT * FROM SanPham")
                .getResultList().size());

        System.out.println(entityManager
                .createNativeQuery("SELECT * FROM dbo.SanPham")
                .getResultList().size());
        System.out.println(
                entityManager
                        .createQuery("from SanPham", SanPham.class)
                        .getResultList()
                        .size()
        );

        System.out.println("\n========== ENTITY TEST ==========");

        Object nativeCount = entityManager
                .createNativeQuery("SELECT COUNT(*) FROM SanPham")
                .getSingleResult();

        System.out.println("Native SQL = " + nativeCount);

        var metamodel = entityManager.getMetamodel();

        var entity = metamodel.entity(SanPham.class);

        System.out.println("Entity Name : " + entity.getName());
        System.out.println("Java Class  : " + entity.getJavaType());

        entity.getAttributes().forEach(a ->
                System.out.println(a.getName() + " -> " + a.getJavaType().getSimpleName()));

        System.out.println("=================================\n");

        // =====================================================
        // KHÔNG SEED SẢN PHẨM
        // =====================================================
    }
}