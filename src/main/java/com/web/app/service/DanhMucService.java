package com.web.app.service;

import com.web.app.model.DanhMuc;
import com.web.app.repository.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DanhMucService {

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private RealtimeService realtimeService;

    public List<DanhMuc> findAll() {
        return danhMucRepository.findAll();
    }

    public Optional<DanhMuc> findById(Integer id) {
        return danhMucRepository.findById(id);
    }

    public boolean nameExists(String name) {
        return danhMucRepository.existsByTenDanhMucIgnoreCase(name);
    }

    public boolean nameExistsForOtherCategory(String name, Integer id) {
        return danhMucRepository.existsByTenDanhMucIgnoreCaseAndIdNot(name, id);
    }

    @Transactional
    public DanhMuc save(DanhMuc danhMuc) {
        DanhMuc saved = danhMucRepository.save(danhMuc);
        realtimeService.publish("CATALOG");
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        danhMucRepository.deleteById(id);
        realtimeService.publish("CATALOG");
    }
}
