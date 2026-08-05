package com.web.app.service;

import com.web.app.model.MaGiamGia;
import com.web.app.repository.MaGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaGiamGiaService {

    @Autowired
    private MaGiamGiaRepository repository;

    public List<MaGiamGia> findAll() {
        return repository.findAll();
    }

    public Optional<MaGiamGia> findById(Integer id) {
        return repository.findById(id);
    }

    public Optional<MaGiamGia> findByCode(String code) {
        if (code == null) return Optional.empty();
        return repository.findByMaCode(code.trim().toUpperCase());
    }

    public MaGiamGia save(MaGiamGia ma) {
        if (ma.getMaCode() != null) {
            ma.setMaCode(ma.getMaCode().trim().toUpperCase());
        }
        return repository.save(ma);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public void checkCouponValidity(MaGiamGia coupon, Double orderAmount) {
        if (coupon == null) {
            throw new IllegalArgumentException("Mã giảm giá không tồn tại!");
        }
        if (coupon.getTrangThai() == null || !coupon.getTrangThai()) {
            throw new IllegalArgumentException("Mã giảm giá này đã bị vô hiệu hóa!");
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getNgayBatDau() != null && now.isBefore(coupon.getNgayBatDau())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến thời gian áp dụng!");
        }
        if (coupon.getNgayKetThuc() != null && now.isAfter(coupon.getNgayKetThuc())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng!");
        }
        if (coupon.getSoLuong() != null && coupon.getSoLuongDaDung() != null && coupon.getSoLuongDaDung() >= coupon.getSoLuong()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng!");
        }
        if (coupon.getGiaTriToiThieu() != null && orderAmount < coupon.getGiaTriToiThieu()) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + 
                String.format("%,.0f", coupon.getGiaTriToiThieu()) + " ₫ để sử dụng mã này!");
        }
    }

    public Double calculateDiscount(MaGiamGia coupon, Double orderAmount) {
        if (coupon == null) return 0.0;
        
        Double discount = 0.0;
        if ("PERCENTAGE".equals(coupon.getLoaiGiamGia())) {
            discount = orderAmount * (coupon.getGiaTriGiam() / 100.0);
            if (coupon.getGiaTriGiamToiDa() != null && discount > coupon.getGiaTriGiamToiDa()) {
                discount = coupon.getGiaTriGiamToiDa();
            }
        } else if ("FIXED_AMOUNT".equals(coupon.getLoaiGiamGia())) {
            discount = coupon.getGiaTriGiam();
        }
        
        if (discount > orderAmount) {
            discount = orderAmount;
        }
        return discount;
    }
}
