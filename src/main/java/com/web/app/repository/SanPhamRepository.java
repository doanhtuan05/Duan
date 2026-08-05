package com.web.app.repository;

import com.web.app.model.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    
    @Query("SELECT s FROM SanPham s WHERE " +
           "(:search IS NULL OR s.tenSanPham LIKE %:search% OR s.moTa LIKE %:search%) AND " +
           "(:danhMucId IS NULL OR s.danhMuc.id = :danhMucId) AND " +
           "(:thuongHieuId IS NULL OR s.thuongHieu.id = :thuongHieuId) AND " +
           "(:minGia IS NULL OR s.gia >= :minGia) AND " +
           "(:maxGia IS NULL OR s.gia <= :maxGia)")
    Page<SanPham> filterSanPham(@Param("search") String search,
                                @Param("danhMucId") Integer danhMucId,
                                @Param("thuongHieuId") Integer thuongHieuId,
                                @Param("minGia") Double minGia,
                                @Param("maxGia") Double maxGia,
                                Pageable pageable);

    List<SanPham> findTop8ByOrderByNgayTaoDesc();
}
