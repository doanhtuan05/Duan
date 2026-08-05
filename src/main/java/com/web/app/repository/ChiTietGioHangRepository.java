package com.web.app.repository;

import com.web.app.model.ChiTietGioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Integer> {
    List<ChiTietGioHang> findByGioHangId(Integer gioHangId);
    Optional<ChiTietGioHang> findByGioHangIdAndSanPhamId(Integer gioHangId, Integer sanPhamId);
    void deleteByGioHangId(Integer gioHangId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ChiTietGioHang c WHERE c.gioHang.id = ?1 AND c.sanPham.id = ?2")
    void deleteByGioHangIdAndSanPhamId(Integer gioHangId, Integer sanPhamId);
}
