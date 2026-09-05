package com.web.app.dto;

import com.web.app.model.KhachHang;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KhachHangAdminDTO {
    private KhachHang customer;
    private long orderCount;
    private double totalSpent;
    private LocalDateTime firstOrderDate;
}
