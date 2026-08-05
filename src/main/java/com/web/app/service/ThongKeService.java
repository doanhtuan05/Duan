package com.web.app.service;

import com.web.app.dto.DoanhThuDTO;
import com.web.app.repository.DonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ThongKeService {

    @Autowired
    private DonHangRepository donHangRepository;

    private List<DoanhThuDTO> mapToDOanhThuDTOList(List<Object[]> rawData) {
        List<DoanhThuDTO> list = new ArrayList<>();
        if (rawData != null) {
            for (Object[] row : rawData) {
                String label = row[0] != null ? row[0].toString() : "";
                Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                Long orderCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                list.add(new DoanhThuDTO(label, revenue, orderCount));
            }
        }
        return list;
    }

    public List<DoanhThuDTO> getDoanhThuTheoNgay() {
        return mapToDOanhThuDTOList(donHangRepository.thongKeDoanhThuTheoNgay());
    }

    public List<DoanhThuDTO> getDoanhThuTheoThang() {
        return mapToDOanhThuDTOList(donHangRepository.thongKeDoanhThuTheoThang());
    }

    public List<DoanhThuDTO> getDoanhThuTheoNam() {
        return mapToDOanhThuDTOList(donHangRepository.thongKeDoanhThuTheoNam());
    }
}
