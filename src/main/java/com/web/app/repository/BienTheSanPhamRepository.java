package com.web.app.repository;

import com.web.app.model.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    List<BienTheSanPham> findBySanPhamIdOrderByMauSacAscKichCoAsc(Integer sanPhamId);

    @Query("SELECT DISTINCT v.mauSac FROM BienTheSanPham v WHERE v.mauSac IS NOT NULL ORDER BY v.mauSac")
    List<String> findDistinctColors();

    @Query("SELECT DISTINCT v.kichCo FROM BienTheSanPham v WHERE v.kichCo IS NOT NULL ORDER BY v.kichCo")
    List<String> findDistinctSizes();
}
