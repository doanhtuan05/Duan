package com.web.app.repository;

import com.web.app.model.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {
    boolean existsByBienTheId(Integer bienTheId);
    List<ChiTietDonHang> findByDonHangId(Integer donHangId);
    @Query("select coalesce(sum(c.soLuong), 0) from ChiTietDonHang c where c.sanPham.id = :productId and c.donHang.trangThai = 'SHIPPING'")
    Long sumShippingQuantityByProductId(@Param("productId") Integer productId);
}
