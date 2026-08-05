package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KhachHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ho_ten", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String hoTen;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "dia_chi", columnDefinition = "NVARCHAR(255)")
    private String diaChi;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id", referencedColumnName = "id")
    private TaiKhoan taiKhoan;
}
