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

    public List<DanhMuc> findAll() {
        return danhMucRepository.findAll();
    }

    public Optional<DanhMuc> findById(Integer id) {
        return danhMucRepository.findById(id);
    }

    @Transactional
    public DanhMuc save(DanhMuc danhMuc) {
        return danhMucRepository.save(danhMuc);
    }

    @Transactional
    public void delete(Integer id) {
        danhMucRepository.deleteById(id);
    }
}
