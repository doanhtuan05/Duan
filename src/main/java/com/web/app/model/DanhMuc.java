package com.web.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DanhMuc")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_danh_muc", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenDanhMuc;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String moTa;
}
