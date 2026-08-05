package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ThuongHieu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_thuong_hieu", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenThuongHieu;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String moTa;
}
