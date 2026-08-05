package com.web.app.service;

import com.web.app.model.KhachHang;
import com.web.app.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    public List<KhachHang> findAll() {
        return khachHangRepository.findAll();
    }

    public Optional<KhachHang> findById(Integer id) {
        return khachHangRepository.findById(id);
    }

    public Optional<KhachHang> findByTaiKhoanId(Integer taiKhoanId) {
        return khachHangRepository.findByTaiKhoanId(taiKhoanId);
    }

    public Optional<KhachHang> findByTenDangNhap(String tenDangNhap) {
        return khachHangRepository.findByTaiKhoanTenDangNhap(tenDangNhap);
    }

    @Transactional
    public KhachHang updateProfile(Integer id, String hoTen, String email, String soDienThoai, String diaChi) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));

        khachHang.setHoTen(hoTen);
        khachHang.setEmail(email);
        khachHang.setSoDienThoai(soDienThoai);
        khachHang.setDiaChi(diaChi);

        return khachHangRepository.save(khachHang);
    }
}
