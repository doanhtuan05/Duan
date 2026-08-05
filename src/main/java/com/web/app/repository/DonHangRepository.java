package com.web.app.repository;

import com.web.app.model.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByKhachHangIdOrderByNgayDatDesc(Integer khachHangId);
    List<DonHang> findAllByOrderByNgayDatDesc();

    @Query(value = "SELECT CONVERT(VARCHAR(10), ngay_dat, 120) AS date, SUM(tong_tien) AS revenue, COUNT(id) AS orders " +
                   "FROM don_hang WHERE trang_thai = N'DELIVERED' " +
                   "GROUP BY CONVERT(VARCHAR(10), ngay_dat, 120) " +
                   "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoNgay();

    @Query(value = "SELECT CONVERT(VARCHAR(7), ngay_dat, 120) AS month, SUM(tong_tien) AS revenue, COUNT(id) AS orders " +
                   "FROM don_hang WHERE trang_thai = N'DELIVERED' " +
                   "GROUP BY CONVERT(VARCHAR(7), ngay_dat, 120) " +
                   "ORDER BY month DESC", nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoThang();

    @Query(value = "SELECT YEAR(ngay_dat) AS year, SUM(tong_tien) AS revenue, COUNT(id) AS orders " +
                   "FROM don_hang WHERE trang_thai = N'DELIVERED' " +
                   "GROUP BY YEAR(ngay_dat) " +
                   "ORDER BY year DESC", nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoNam();
}
