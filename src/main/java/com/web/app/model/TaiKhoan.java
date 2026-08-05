package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TaiKhoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_dang_nhap", unique = true, nullable = false, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "vai_tro", nullable = false, length = 20)
    private String vaiTro; // "ADMIN", "USER"

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai; // "ACTIVE", "LOCKED"
}
