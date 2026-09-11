package com.web.app.service;

import com.web.app.model.*;
import com.web.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {
    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private ChiTietGioHangRepository chiTietGioHangRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private BienTheSanPhamRepository bienTheSanPhamRepository;
    @Autowired private RealtimeService realtimeService;

    private GioHang getOrCreateGioHang(Integer customerId) {
        return gioHangRepository.findByKhachHangId(customerId).orElseGet(() -> {
            KhachHang customer = khachHangRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));
            return gioHangRepository.save(GioHang.builder().khachHang(customer).build());
        });
    }

    public List<ChiTietGioHang> getCartDetails(Integer customerId) {
        return gioHangRepository.findByKhachHangId(customerId)
                .map(cart -> chiTietGioHangRepository.findByGioHangId(cart.getId()))
                .orElseGet(ArrayList::new);
    }

    @Transactional
    public void addToCart(Integer customerId, Integer productId, int quantity) { addToCart(customerId, productId, null, quantity); }

    @Transactional
    public void addToCart(Integer customerId, Integer productId, Integer variantId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
        GioHang cart = getOrCreateGioHang(customerId);
        SanPham product = sanPhamRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));
        BienTheSanPham variant = variantId == null ? null : bienTheSanPhamRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Biến thể không tồn tại!"));
        if (variant != null && !variant.getSanPham().getId().equals(productId)) throw new IllegalArgumentException("Biến thể không thuộc sản phẩm này!");
        int stock = variant == null ? product.getSoLuong() : variant.getSoLuong();
        if (stock < quantity) throw new IllegalArgumentException("Số lượng trong kho không đủ!");
        Optional<ChiTietGioHang> existing = variant == null
                ? chiTietGioHangRepository.findByGioHangIdAndSanPhamId(cart.getId(), productId)
                : chiTietGioHangRepository.findByGioHangIdAndBienTheId(cart.getId(), variantId);
        if (existing.isPresent()) {
            ChiTietGioHang item = existing.get();
            if (stock < item.getSoLuong() + quantity) throw new IllegalArgumentException("Tổng số lượng vượt quá tồn kho!");
            item.setSoLuong(item.getSoLuong() + quantity);
            chiTietGioHangRepository.save(item);
        } else chiTietGioHangRepository.save(ChiTietGioHang.builder().gioHang(cart).sanPham(product).bienThe(variant).soLuong(quantity).build());
        realtimeService.publishForCustomer("CART", customerId);
    }

    @Transactional
    public void updateCartItemQuantityById(Integer customerId, Integer itemId, int quantity) {
        ChiTietGioHang item = getOwnedItem(customerId, itemId);
        if (quantity <= 0) { chiTietGioHangRepository.delete(item); realtimeService.publishForCustomer("CART", customerId); return; }
        if (item.getTonKho() < quantity) throw new IllegalArgumentException("Số lượng trong kho không đủ!");
        item.setSoLuong(quantity); chiTietGioHangRepository.save(item); realtimeService.publishForCustomer("CART", customerId);
    }

    @Transactional
    public void updateCartItemQuantity(Integer customerId, Integer productId, int quantity) {
        GioHang cart = getOrCreateGioHang(customerId);
        ChiTietGioHang item = chiTietGioHangRepository.findByGioHangIdAndSanPhamId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng!"));
        updateCartItemQuantityById(customerId, item.getId(), quantity);
    }

    @Transactional
    public void removeCartItemById(Integer customerId, Integer itemId) { chiTietGioHangRepository.delete(getOwnedItem(customerId, itemId)); realtimeService.publishForCustomer("CART", customerId); }

    @Transactional
    public void removeCartItem(Integer customerId, Integer productId) {
        gioHangRepository.findByKhachHangId(customerId).flatMap(cart ->
                chiTietGioHangRepository.findByGioHangIdAndSanPhamId(cart.getId(), productId))
                .ifPresent(item -> { chiTietGioHangRepository.delete(item); realtimeService.publishForCustomer("CART", customerId); });
    }

    private ChiTietGioHang getOwnedItem(Integer customerId, Integer itemId) {
        ChiTietGioHang item = chiTietGioHangRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng!"));
        if (!item.getGioHang().getKhachHang().getId().equals(customerId)) throw new IllegalArgumentException("Không có quyền thao tác giỏ hàng!");
        return item;
    }

    @Transactional
    public void clearCart(Integer customerId) {
        gioHangRepository.findByKhachHangId(customerId).ifPresent(cart -> { chiTietGioHangRepository.deleteAll(chiTietGioHangRepository.findByGioHangId(cart.getId())); realtimeService.publishForCustomer("CART", customerId); });
    }
}
