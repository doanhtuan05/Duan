package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChiTietDonHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "gia_ban", nullable = false)
    private Double giaBan;
}
