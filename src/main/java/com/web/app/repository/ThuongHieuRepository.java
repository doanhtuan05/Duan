package com.web.app.repository;

import com.web.app.model.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    boolean existsByTenThuongHieuIgnoreCase(String tenThuongHieu);
    boolean existsByTenThuongHieuIgnoreCaseAndIdNot(String tenThuongHieu, Integer id);
}
