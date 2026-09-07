package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChiTietGioHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietGioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gio_hang_id", nullable = false)
    private GioHang gioHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bien_the_id")
    private BienTheSanPham bienThe;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    public double getDonGia() { return bienThe != null ? bienThe.getGia() : sanPham.getGia(); }
    public int getTonKho() { return bienThe != null ? bienThe.getSoLuong() : sanPham.getSoLuong(); }
}
