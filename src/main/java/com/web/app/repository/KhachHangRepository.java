package com.web.app.repository;

import com.web.app.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findByTaiKhoanId(Integer taiKhoanId);
    Optional<KhachHang> findByTaiKhoanTenDangNhap(String tenDangNhap);
}
