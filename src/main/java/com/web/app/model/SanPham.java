package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SanPham")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_san_pham", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String tenSanPham;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "gia", nullable = false)
    private Double gia;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "anh_url", length = 255)
    private String anhUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "danh_muc_id")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "thuong_hieu_id")
    private ThuongHieu thuongHieu;

    // =================== Thuộc tính mũ ===================

    @Column(name = "kieu_dang", columnDefinition = "NVARCHAR(100)")
    private String kieuDang;

    @Column(name = "chu_vi_vong_dau", columnDefinition = "NVARCHAR(50)")
    private String chuViVongDau;

    @Column(name = "chat_lieu", columnDefinition = "NVARCHAR(100)")
    private String chatLieu;

    @Column(name = "mau_sac", columnDefinition = "NVARCHAR(100)")
    private String mauSac;

    @Column(name = "doi_tuong", columnDefinition = "NVARCHAR(100)")
    private String doiTuong;

    @Column(name = "do_tuoi", columnDefinition = "NVARCHAR(100)")
    private String doTuoi;

    // ================================================

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}