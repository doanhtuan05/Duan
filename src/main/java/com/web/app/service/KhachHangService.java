package com.web.app.service;

import com.web.app.model.KhachHang;
import com.web.app.model.DonHang;
import com.web.app.dto.KhachHangAdminDTO;
import com.web.app.repository.KhachHangRepository;
import com.web.app.repository.DonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.time.LocalDateTime;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private DonHangRepository donHangRepository;

    public List<KhachHang> findAll() {
        return khachHangRepository.findAll();
    }

    public Optional<KhachHang> findById(Integer id) {
        return khachHangRepository.findById(id);
    }

    public Optional<KhachHang> findByTaiKhoanId(Integer taiKhoanId) {
        return khachHangRepository.findByTaiKhoanId(taiKhoanId);
    }

    public Optional<KhachHang> findByTenDangNhap(String tenDangNhap) {
        return khachHangRepository.findByTaiKhoanTenDangNhap(tenDangNhap);
    }

    public List<KhachHangAdminDTO> getAdminCustomers() {
        Map<Integer, List<DonHang>> ordersByCustomer = donHangRepository.findAllByOrderByNgayDatDesc().stream()
                .filter(order -> order.getKhachHang() != null)
                .collect(Collectors.groupingBy(order -> order.getKhachHang().getId()));
        return khachHangRepository.findAll().stream().map(customer -> {
            List<DonHang> orders = ordersByCustomer.getOrDefault(customer.getId(), List.of());
            double spent = orders.stream().filter(order -> "DELIVERED".equals(order.getTrangThai()))
                    .mapToDouble(order -> order.getTongTien() == null ? 0 : order.getTongTien()).sum();
            LocalDateTime firstOrder = orders.stream().map(DonHang::getNgayDat).filter(java.util.Objects::nonNull)
                    .min(Comparator.naturalOrder()).orElse(null);
            return new KhachHangAdminDTO(customer, orders.size(), spent, firstOrder);
        }).toList();
    }

    @Transactional
    public void toggleAccountStatus(Integer customerId) {
        KhachHang customer = khachHangRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại."));
        if (customer.getTaiKhoan() == null) {
            throw new IllegalArgumentException("Khách hàng chưa có tài khoản.");
        }
        customer.getTaiKhoan().setTrangThai("LOCKED".equals(customer.getTaiKhoan().getTrangThai()) ? "ACTIVE" : "LOCKED");
        khachHangRepository.save(customer);
    }

    @Transactional
    public KhachHang updateProfile(Integer id, String hoTen, String email, String soDienThoai, String diaChi) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));

        khachHang.setHoTen(hoTen);
        khachHang.setEmail(email);
        khachHang.setSoDienThoai(soDienThoai);
        khachHang.setDiaChi(diaChi);

        return khachHangRepository.save(khachHang);
    }
}
