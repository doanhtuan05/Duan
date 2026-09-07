package com.web.app.repository;

import com.web.app.model.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    List<BienTheSanPham> findBySanPhamIdOrderByMauSacAscKichCoAsc(Integer sanPhamId);
}
