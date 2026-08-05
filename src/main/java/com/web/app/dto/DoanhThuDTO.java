package com.web.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoanhThuDTO {
    private String label; // e.g. "2026-06-20", "2026-06", "2026"
    private Double revenue;
    private Long orderCount;
}
