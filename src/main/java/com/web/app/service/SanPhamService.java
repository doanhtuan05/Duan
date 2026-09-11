package com.web.app.service;

import com.web.app.model.SanPham;
import com.web.app.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private RealtimeService realtimeService;

    public List<SanPham> findAll() {
        return sanPhamRepository.findAll();
    }

    public Page<SanPham> findAll(int page, int size) {
        return sanPhamRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    public Page<SanPham> getFilteredProducts(String search, Integer danhMucId, Integer thuongHieuId, Double minGia, Double maxGia, int page, int size) {
        // Clean empty search queries
        String searchPattern = (search == null || search.trim().isEmpty()) ? null : search.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        return sanPhamRepository.filterSanPham(searchPattern, danhMucId, thuongHieuId, minGia, maxGia, pageable);
    }

    public Optional<SanPham> findById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    public List<SanPham> getLatestProducts() {
        return sanPhamRepository.findTop8ByOrderByNgayTaoDesc();
    }

    @Transactional
    public SanPham save(SanPham sanPham) {
        SanPham saved = sanPhamRepository.save(sanPham);
        realtimeService.publish("CATALOG");
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        sanPhamRepository.deleteById(id);
        realtimeService.publish("CATALOG");
    }
}
