package com.web.app.service;

import com.web.app.model.ChiTietGioHang;
import com.web.app.model.GioHang;
import com.web.app.model.KhachHang;
import com.web.app.model.SanPham;
import com.web.app.repository.ChiTietGioHangRepository;
import com.web.app.repository.GioHangRepository;
import com.web.app.repository.KhachHangRepository;
import com.web.app.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private ChiTietGioHangRepository chiTietGioHangRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    private GioHang getOrCreateGioHang(Integer khachHangId) {
        return gioHangRepository.findByKhachHangId(khachHangId)
                .orElseGet(() -> {
                    KhachHang kh = khachHangRepository.findById(khachHangId)
                            .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));
                    GioHang gh = GioHang.builder().khachHang(kh).build();
                    return gioHangRepository.save(gh);
                });
    }

    public List<ChiTietGioHang> getCartDetails(Integer khachHangId) {
        Optional<GioHang> ghOpt = gioHangRepository.findByKhachHangId(khachHangId);
        if (ghOpt.isEmpty()) {
            return new ArrayList<>();
        }
        return chiTietGioHangRepository.findByGioHangId(ghOpt.get().getId());
    }

    @Transactional
    public void addToCart(Integer khachHangId, Integer sanPhamId, int soLuong) {
        GioHang gh = getOrCreateGioHang(khachHangId);
        SanPham sp = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));

        if (sp.getSoLuong() < soLuong) {
            throw new IllegalArgumentException("Số lượng trong kho không đủ!");
        }

        Optional<ChiTietGioHang> ctOpt = chiTietGioHangRepository.findByGioHangIdAndSanPhamId(gh.getId(), sanPhamId);
        if (ctOpt.isPresent()) {
            ChiTietGioHang ct = ctOpt.get();
            int newQty = ct.getSoLuong() + soLuong;
            if (sp.getSoLuong() < newQty) {
                throw new IllegalArgumentException("Tổng số lượng vượt quá tồn kho!");
            }
            ct.setSoLuong(newQty);
            chiTietGioHangRepository.save(ct);
        } else {
            ChiTietGioHang ct = ChiTietGioHang.builder()
                    .gioHang(gh)
                    .sanPham(sp)
                    .soLuong(soLuong)
                    .build();
            chiTietGioHangRepository.save(ct);
        }
    }

    @Transactional
    public void updateCartItemQuantity(Integer khachHangId, Integer sanPhamId, int soLuong) {
        GioHang gh = getOrCreateGioHang(khachHangId);
        ChiTietGioHang ct = chiTietGioHangRepository.findByGioHangIdAndSanPhamId(gh.getId(), sanPhamId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng!"));

        if (soLuong <= 0) {
            chiTietGioHangRepository.delete(ct);
            return;
        }

        SanPham sp = ct.getSanPham();
        if (sp.getSoLuong() < soLuong) {
            throw new IllegalArgumentException("Số lượng trong kho không đủ!");
        }

        ct.setSoLuong(soLuong);
        chiTietGioHangRepository.save(ct);
    }

    @Transactional
    public void removeCartItem(Integer khachHangId, Integer sanPhamId) {
        Optional<GioHang> ghOpt = gioHangRepository.findByKhachHangId(khachHangId);
        if (ghOpt.isPresent()) {
            Optional<ChiTietGioHang> ctOpt = chiTietGioHangRepository.findByGioHangIdAndSanPhamId(ghOpt.get().getId(), sanPhamId);
            ctOpt.ifPresent(ct -> {
                chiTietGioHangRepository.delete(ct);
                chiTietGioHangRepository.flush();
            });
        }
    }

    @Transactional
    public void clearCart(Integer khachHangId) {
        Optional<GioHang> ghOpt = gioHangRepository.findByKhachHangId(khachHangId);
        if (ghOpt.isPresent()) {
            // Need a transactional delete query or deleting items in loop
            List<ChiTietGioHang> items = chiTietGioHangRepository.findByGioHangId(ghOpt.get().getId());
            chiTietGioHangRepository.deleteAll(items);
        }
    }
}
