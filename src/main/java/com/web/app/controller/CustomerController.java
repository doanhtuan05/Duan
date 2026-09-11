package com.web.app.controller;

import com.web.app.model.*;
import com.web.app.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import com.web.app.repository.BienTheSanPhamRepository;

@Controller
public class CustomerController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DanhMucService danhMucService;

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private RealtimeService realtimeService;

    // Helper to get logged-in customer from session
    private KhachHang getSessionCustomer(HttpSession session) {
        return (KhachHang) session.getAttribute("user");
    }

    @GetMapping("/")
    public String homepage(@RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "danhMucId", required = false) Integer danhMucId,
                           @RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId,
                           @RequestParam(value = "minPrice", required = false) Double minPrice,
                           @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "6") int size,
                           Model model) {

        Page<SanPham> productPage = sanPhamService.getFilteredProducts(
                keyword, danhMucId, thuongHieuId, minPrice, maxPrice, page, size);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        // Dropdowns for filtering
        model.addAttribute("categories", danhMucService.findAll());
        model.addAttribute("brands", thuongHieuService.findAll());

        // Retain search parameters in the UI
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDanhMucId", danhMucId);
        model.addAttribute("selectedThuongHieuId", thuongHieuId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "index";
    }

    @GetMapping("/san-pham/{id}")
    public String productDetails(@PathVariable("id") Integer id, Model model) {
        SanPham sp = sanPhamService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));
        model.addAttribute("product", sp);
        model.addAttribute("variants", bienTheSanPhamRepository.findBySanPhamIdOrderByMauSacAscKichCoAsc(id));
        return "product_detail";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/cart";
        }
        List<ChiTietGioHang> items = gioHangService.getCartDetails(kh.getId());
        
        double tongTien = items.stream()
                .mapToDouble(item -> item.getDonGia() * item.getSoLuong())
                .sum();

        model.addAttribute("cartItems", items);
        model.addAttribute("tongTien", tongTien);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Integer productId,
                            @RequestParam(value = "variantId", required = false) Integer variantId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/cart";
        }
        try {
            gioHangService.addToCart(kh.getId(), productId, variantId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("productId") Integer productId,
                             @RequestParam("quantity") int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/cart";
        }
        try {
            gioHangService.updateCartItemQuantityById(kh.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giỏ hàng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCartAjax(@RequestParam("productId") Integer productId,
                                            @RequestParam("quantity") int quantity,
                                            HttpSession session) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Vui lòng đăng nhập!");
            return ResponseEntity.status(401).body(err);
        }
        try {
            gioHangService.updateCartItemQuantityById(kh.getId(), productId, quantity);
            
            List<ChiTietGioHang> items = gioHangService.getCartDetails(kh.getId());
            double tongTien = items.stream()
                    .mapToDouble(item -> item.getDonGia() * item.getSoLuong())
                    .sum();
            int cartSize = items.stream()
                    .mapToInt(item -> item.getSoLuong())
                    .sum();
            
            double itemSubtotal = items.stream()
                    .filter(item -> item.getId().equals(productId))
                    .mapToDouble(item -> item.getDonGia() * item.getSoLuong())
                    .findFirst()
                    .orElse(0.0);
            
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("quantity", quantity);
            res.put("itemSubtotal", itemSubtotal);
            res.put("tongTien", tongTien);
            res.put("cartSize", cartSize);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.ok(err);
        }
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeCartItemAjax(@RequestParam("productId") Integer productId,
                                                HttpSession session) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Vui lòng đăng nhập!");
            return ResponseEntity.status(401).body(err);
        }
        try {
            gioHangService.removeCartItemById(kh.getId(), productId);
            
            List<ChiTietGioHang> items = gioHangService.getCartDetails(kh.getId());
            double tongTien = items.stream()
                    .mapToDouble(item -> item.getDonGia() * item.getSoLuong())
                    .sum();
            int cartSize = items.stream()
                    .mapToInt(item -> item.getSoLuong())
                    .sum();
            
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("tongTien", tongTien);
            res.put("cartSize", cartSize);
            res.put("isEmpty", items.isEmpty());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.ok(err);
        }
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeCartItem(@PathVariable("productId") Integer productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/cart";
        }
        try {
            gioHangService.removeCartItem(kh.getId(), productId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/checkout";
        }
        List<ChiTietGioHang> items = gioHangService.getCartDetails(kh.getId());
        
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống! Vui lòng thêm sản phẩm trước khi thanh toán.");
            return "redirect:/cart";
        }

        double tongTien = items.stream()
                .mapToDouble(item -> item.getDonGia() * item.getSoLuong())
                .sum();

        model.addAttribute("cartItems", items);
        model.addAttribute("tongTien", tongTien);
        
        // Pass customer defaults
        model.addAttribute("hoTenNhan", kh.getHoTen());
        model.addAttribute("soDienThoaiNhan", kh.getSoDienThoai());
        model.addAttribute("diaChiNhan", kh.getDiaChi());

        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("hoTenNhan") String hoTenNhan,
                                  @RequestParam("soDienThoaiNhan") String soDienThoaiNhan,
                                  @RequestParam("diaChiNhan") String diaChiNhan,
                                  @RequestParam(value = "ghiChu", required = false) String ghiChu,
                                  @RequestParam(value = "couponCode", required = false) String couponCode,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/checkout";
        }
        try {
            DonHang order = donHangService.createOrder(kh.getId(), hoTenNhan, soDienThoaiNhan, diaChiNhan, ghiChu, couponCode);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn hàng của bạn là #" + order.getId());
            return "redirect:/orders";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @PostMapping("/checkout/apply-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyCoupon(
            @RequestParam("couponCode") String couponCode,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để áp dụng mã giảm giá!");
            return ResponseEntity.ok(response);
        }

        try {
            List<ChiTietGioHang> items = gioHangService.getCartDetails(kh.getId());
            if (items.isEmpty()) {
                response.put("success", false);
                response.put("message", "Giỏ hàng trống!");
                return ResponseEntity.ok(response);
            }

            double tongTien = items.stream()
                    .mapToDouble(item -> item.getDonGia() * item.getSoLuong())
                    .sum();

            if (couponCode == null || couponCode.trim().isEmpty()) {
                response.put("success", true);
                response.put("discount", 0.0);
                response.put("newTotal", tongTien);
                response.put("message", "Đã hủy áp dụng mã giảm giá.");
                return ResponseEntity.ok(response);
            }

            MaGiamGia coupon = maGiamGiaService.findByCode(couponCode)
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại hoặc đã hết hạn!"));

            maGiamGiaService.checkCouponValidity(coupon, tongTien);
            double discount = maGiamGiaService.calculateDiscount(coupon, tongTien);
            double newTotal = tongTien - discount;
            if (newTotal < 0) {
                newTotal = 0.0;
            }

            response.put("success", true);
            response.put("discount", discount);
            response.put("newTotal", newTotal);
            response.put("message", "Áp dụng mã giảm giá thành công: " + coupon.getTenKhuyenMai());
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public String orderHistory(HttpSession session, Model model) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/orders";
        }
        List<DonHang> orders = donHangService.getOrderHistory(kh.getId());
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable("id") Integer id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/orders/" + id;
        }
        DonHang order = donHangService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        // Security check: only allow viewing own orders
        if (!order.getKhachHang().getId().equals(kh.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này!");
            return "redirect:/orders";
        }

        List<ChiTietDonHang> details = donHangService.getOrderDetails(id);
        model.addAttribute("order", order);
        model.addAttribute("details", details);
        return "order_detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/orders/" + id;
        }
        DonHang order = donHangService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        if (!order.getKhachHang().getId().equals(kh.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy đơn hàng này!");
            return "redirect:/orders";
        }

        if (!order.getTrangThai().equals("PENDING")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể hủy đơn hàng ở trạng thái Đang chờ xử lý!");
            return "redirect:/orders/" + id;
        }

        try {
            donHangService.updateOrderStatus(id, "CANCELLED");
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/profile";
        }
        // Refresh customer from DB to make sure details are up to date
        KhachHang currentKh = khachHangService.findById(kh.getId())
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));
        
        session.setAttribute("user", currentKh); // update session
        model.addAttribute("customer", currentKh);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("hoTen") String hoTen,
                                @RequestParam("soDienThoai") String soDienThoai,
                                @RequestParam("email") String email,
                                @RequestParam("diaChi") String diaChi,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        KhachHang kh = getSessionCustomer(session);
        if (kh == null) {
            return "redirect:/login?error=login-required&redirect=/profile";
        }
        try {
            KhachHang updated = khachHangService.updateProfile(kh.getId(), hoTen, email, soDienThoai, diaChi);
            session.setAttribute("user", updated); // sync back to session immediately
            realtimeService.publishForCustomer("PROFILE", updated.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }
}
