package com.web.app.repository;

import com.web.app.model.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByKhachHangIdOrderByNgayDatDesc(Integer khachHangId);
    List<DonHang> findAllByOrderByNgayDatDesc();

    @Query("SELECT d FROM DonHang d JOIN d.khachHang k WHERE " +
           "(:keyword IS NULL OR CAST(d.id AS string) LIKE %:keyword% OR LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR d.trangThai = :status) AND " +
           "(:fromDate IS NULL OR d.ngayDat >= :fromDate) AND " +
           "(:toDate IS NULL OR d.ngayDat < :toDate)")
    Page<DonHang> filterOrders(@Param("keyword") String keyword,
                               @Param("status") String status,
                               @Param("fromDate") LocalDateTime fromDate,
                               @Param("toDate") LocalDateTime toDate,
                               Pageable pageable);

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
