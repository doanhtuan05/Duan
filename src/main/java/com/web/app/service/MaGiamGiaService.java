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

    @Autowired
    private RealtimeService realtimeService;

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
        if (ma.getMaCode() == null || ma.getMaCode().isEmpty()) throw new IllegalArgumentException("Mã giảm giá không được để trống.");
        if (!ma.getMaCode().matches("[A-Z0-9_-]+")) throw new IllegalArgumentException("Mã chỉ được chứa chữ cái, số, dấu gạch ngang hoặc gạch dưới.");
        if (ma.getGiaTriGiam() == null || ma.getGiaTriGiam() <= 0) throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
        if ("PERCENTAGE".equals(ma.getLoaiGiamGia()) && ma.getGiaTriGiam() > 100) throw new IllegalArgumentException("Giá trị phần trăm không được vượt quá 100%.");
        if (!"PERCENTAGE".equals(ma.getLoaiGiamGia()) && !"FIXED_AMOUNT".equals(ma.getLoaiGiamGia())) throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        if (ma.getGiaTriToiThieu() != null && ma.getGiaTriToiThieu() < 0) throw new IllegalArgumentException("Giá trị đơn tối thiểu không được âm.");
        if (ma.getNgayBatDau() != null && ma.getNgayKetThuc() != null && !ma.getNgayKetThuc().isAfter(ma.getNgayBatDau())) throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        if (ma.getSoLuong() != null && ma.getSoLuong() < 1) throw new IllegalArgumentException("Giới hạn lượt dùng phải lớn hơn 0.");
        if (ma.getId() != null) repository.findById(ma.getId()).ifPresent(existing -> ma.setSoLuongDaDung(existing.getSoLuongDaDung()));
        if (ma.getSoLuongDaDung() == null) ma.setSoLuongDaDung(0);
        MaGiamGia saved = repository.save(ma);
        realtimeService.publish("COUPONS");
        return saved;
    }

    public void delete(Integer id) {
        repository.deleteById(id);
        realtimeService.publish("COUPONS");
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
