package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "MaGiamGia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_code", unique = true, nullable = false, length = 50)
    private String maCode;

    @Column(name = "ten_khuyen_mai", columnDefinition = "NVARCHAR(255)")
    private String tenKhuyenMai;

    @Column(name = "loai_giam_gia", nullable = false, length = 50)
    private String loaiGiamGia; // "PERCENTAGE" or "FIXED_AMOUNT"

    @Column(name = "gia_tri_giam", nullable = false)
    private Double giaTriGiam;

    @Column(name = "gia_tri_toi_thieu")
    @Builder.Default
    private Double giaTriToiThieu = 0.0;

    @Column(name = "gia_tri_giam_toi_da")
    private Double giaTriGiamToiDa;

    @Column(name = "ngay_bat_dau")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "so_luong_da_dung")
    @Builder.Default
    private Integer soLuongDaDung = 0;

    @Column(name = "trang_thai")
    @Builder.Default
    private Boolean trangThai = true;
}
