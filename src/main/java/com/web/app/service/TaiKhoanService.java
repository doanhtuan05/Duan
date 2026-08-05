package com.web.app.service;

import com.web.app.model.KhachHang;
import com.web.app.model.TaiKhoan;
import com.web.app.repository.KhachHangRepository;
import com.web.app.repository.TaiKhoanRepository;
import com.web.app.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TaiKhoanService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    public Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap) {
        return taiKhoanRepository.findByTenDangNhap(tenDangNhap);
    }

    @Transactional
    public TaiKhoan dangKy(String tenDangNhap, String matKhau, String hoTen, String email, String soDienThoai, String diaChi) {
        if (taiKhoanRepository.findByTenDangNhap(tenDangNhap).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .tenDangNhap(tenDangNhap)
                .matKhau(HashUtil.hashPassword(matKhau))
                .vaiTro("USER")
                .trangThai("ACTIVE")
                .build();

        TaiKhoan savedAcc = taiKhoanRepository.save(taiKhoan);

        KhachHang khachHang = KhachHang.builder()
                .hoTen(hoTen)
                .email(email)
                .soDienThoai(soDienThoai)
                .diaChi(diaChi)
                .taiKhoan(savedAcc)
                .build();

        khachHangRepository.save(khachHang);
        return savedAcc;
    }

    public Optional<TaiKhoan> dangNhap(String tenDangNhap, String matKhau) {
        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhap(tenDangNhap);
        if (taiKhoanOpt.isPresent()) {
            TaiKhoan tk = taiKhoanOpt.get();
            if (tk.getMatKhau().equals(HashUtil.hashPassword(matKhau)) && "ACTIVE".equals(tk.getTrangThai())) {
                return Optional.of(tk);
            }
        }
        return Optional.empty();
    }
}
