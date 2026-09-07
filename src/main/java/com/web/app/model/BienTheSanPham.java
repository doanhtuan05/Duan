package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BienTheSanPham", uniqueConstraints = @UniqueConstraint(columnNames = {"san_pham_id", "mau_sac", "kich_co"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienTheSanPham {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @Column(name = "mau_sac", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String mauSac;

    @Column(name = "kich_co", nullable = false, length = 30, columnDefinition = "NVARCHAR(30)")
    private String kichCo;

    @Column(name = "sku", length = 80, unique = true)
    private String sku;

    @Column(name = "gia", nullable = false)
    private Double gia;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
}
