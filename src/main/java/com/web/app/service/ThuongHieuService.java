package com.web.app.service;

import com.web.app.model.ThuongHieu;
import com.web.app.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ThuongHieuService {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    public List<ThuongHieu> findAll() {
        return thuongHieuRepository.findAll();
    }

    public Optional<ThuongHieu> findById(Integer id) {
        return thuongHieuRepository.findById(id);
    }

    public boolean nameExists(String name) {
        return thuongHieuRepository.existsByTenThuongHieuIgnoreCase(name);
    }

    public boolean nameExistsForOtherBrand(String name, Integer id) {
        return thuongHieuRepository.existsByTenThuongHieuIgnoreCaseAndIdNot(name, id);
    }

    @Transactional
    public ThuongHieu save(ThuongHieu thuongHieu) {
        return thuongHieuRepository.save(thuongHieu);
    }

    @Transactional
    public void delete(Integer id) {
        thuongHieuRepository.deleteById(id);
    }
}
