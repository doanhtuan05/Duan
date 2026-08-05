package com.web.app.repository;

import com.web.app.model.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {
    List<ChiTietDonHang> findByDonHangId(Integer donHangId);
}
