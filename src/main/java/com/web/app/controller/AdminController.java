package com.web.app.controller;

import com.web.app.dto.DoanhThuDTO;
import com.web.app.model.*;
import com.web.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ThongKeService thongKeService;

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DanhMucService danhMucService;

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    // 1. Dashboard & Statistics
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        List<DoanhThuDTO> dsNgay = thongKeService.getDoanhThuTheoNgay();
        List<DoanhThuDTO> dsThang = thongKeService.getDoanhThuTheoThang();
        List<DoanhThuDTO> dsNam = thongKeService.getDoanhThuTheoNam();

        // Calculate summaries
        double tongDoanhThu = dsNgay.stream().mapToDouble(DoanhThuDTO::getRevenue).sum();
        long tongDonHang = dsNgay.stream().mapToLong(DoanhThuDTO::getOrderCount).sum();

        model.addAttribute("dsNgay", dsNgay);
        model.addAttribute("dsThang", dsThang);
        model.addAttribute("dsNam", dsNam);
        model.addAttribute("tongDoanhThu", tongDoanhThu);
        model.addAttribute("tongDonHang", tongDonHang);

        return "admin/dashboard";
    }

    // 2. Product Management (CRUD)
    @GetMapping("/products")
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        Page<SanPham> prodPage = sanPhamService.getFilteredProducts(keyword, null, null, null, null, page, size);
        model.addAttribute("products", prodPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prodPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        return "admin/products";
    }

    @GetMapping("/products/create")
    public String createProductForm(Model model) {
        model.addAttribute("product", new SanPham());
        model.addAttribute("categories", danhMucService.findAll());
        model.addAttribute("brands", thuongHieuService.findAll());
        return "admin/product_form";
    }

    @PostMapping("/products/create")
    public String createProduct(@ModelAttribute SanPham sp,
                                @RequestParam("imageFile") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                sp.setAnhUrl(fileName);
            }
            sanPhamService.save(sp);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tải ảnh lên: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Integer id, Model model) {
        SanPham sp = sanPhamService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));
        model.addAttribute("product", sp);
        model.addAttribute("categories", danhMucService.findAll());
        model.addAttribute("brands", thuongHieuService.findAll());
        return "admin/product_form";
    }

    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable("id") Integer id,
                              @ModelAttribute SanPham sp,
                              @RequestParam("imageFile") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            SanPham existing = sanPhamService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));

            existing.setTenSanPham(sp.getTenSanPham());
            existing.setGia(sp.getGia());
            existing.setSoLuong(sp.getSoLuong());
            existing.setMoTa(sp.getMoTa());
            existing.setDanhMuc(sp.getDanhMuc());
            existing.setThuongHieu(sp.getThuongHieu());

// ===== Thuộc tính mũ =====
            existing.setKieuDang(sp.getKieuDang());
            existing.setChuViVongDau(sp.getChuViVongDau());
            existing.setChatLieu(sp.getChatLieu());
            existing.setMauSac(sp.getMauSac());
            existing.setDoiTuong(sp.getDoiTuong());
            existing.setDoTuoi(sp.getDoTuoi());

            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                existing.setAnhUrl(fileName);
            }

            sanPhamService.save(existing);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi lưu ảnh sản phẩm!");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm này do đã có đơn đặt hàng liên quan!");
        }
        return "redirect:/admin/products";
    }

    // 3. Category Management
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", danhMucService.findAll());
        model.addAttribute("newCategory", new DanhMuc());
        return "admin/categories";
    }

    @PostMapping("/categories/create")
    public String createCategory(@ModelAttribute DanhMuc dm, RedirectAttributes redirectAttributes) {
        danhMucService.save(dm);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Integer id, @RequestParam("tenDanhMuc") String name, RedirectAttributes redirectAttributes) {
        DanhMuc dm = danhMucService.findAll().stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại!"));
        dm.setTenDanhMuc(name);
        danhMucService.save(dm);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            danhMucService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục này do đang có sản phẩm thuộc danh mục!");
        }
        return "redirect:/admin/categories";
    }

    // 4. Brand Management
    @GetMapping("/brands")
    public String listBrands(Model model) {
        model.addAttribute("brands", thuongHieuService.findAll());
        model.addAttribute("newBrand", new ThuongHieu());
        return "admin/brands";
    }

    @PostMapping("/brands/create")
    public String createBrand(@ModelAttribute ThuongHieu th, RedirectAttributes redirectAttributes) {
        thuongHieuService.save(th);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm thương hiệu thành công!");
        return "redirect:/admin/brands";
    }

    @PostMapping("/brands/edit/{id}")
    public String editBrand(@PathVariable("id") Integer id, @RequestParam("tenThuongHieu") String name, RedirectAttributes redirectAttributes) {
        ThuongHieu th = thuongHieuService.findAll().stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Thương hiệu không tồn tại!"));
        th.setTenThuongHieu(name);
        thuongHieuService.save(th);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thương hiệu thành công!");
        return "redirect:/admin/brands";
    }

    @GetMapping("/brands/delete/{id}")
    public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            thuongHieuService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thương hiệu này do đang có sản phẩm thuộc thương hiệu!");
        }
        return "redirect:/admin/brands";
    }

    // 5. Customer Directory
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", khachHangService.findAll());
        return "admin/customers";
    }

    // 6. Order Management
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", donHangService.getAllOrders());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable("id") Integer id, Model model) {
        DonHang order = donHangService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));
        model.addAttribute("order", order);
        model.addAttribute("details", donHangService.getOrderDetails(id));
        return "admin/order_detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Integer id,
                                    @RequestParam("trangThai") String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            donHangService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // 6. Coupon Management
    @GetMapping("/coupons")
    public String listCoupons(Model model) {
        model.addAttribute("coupons", maGiamGiaService.findAll());
        return "admin/coupons";
    }

    @GetMapping("/coupons/add")
    public String showAddCouponForm(Model model) {
        model.addAttribute("coupon", new MaGiamGia());
        return "admin/coupon_form";
    }

    @PostMapping("/coupons/add")
    public String saveCoupon(@ModelAttribute("coupon") MaGiamGia coupon, RedirectAttributes redirectAttributes) {
        try {
            if (maGiamGiaService.findByCode(coupon.getMaCode()).isPresent()) {
                throw new IllegalArgumentException("Mã giảm giá này đã tồn tại!");
            }
            maGiamGiaService.save(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mã giảm giá mới thành công!");
            return "redirect:/admin/coupons";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/coupons/add";
        }
    }

    @GetMapping("/coupons/edit/{id}")
    public String showEditCouponForm(@PathVariable("id") Integer id, Model model) {
        MaGiamGia coupon = maGiamGiaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại!"));
        model.addAttribute("coupon", coupon);
        return "admin/coupon_form";
    }

    @PostMapping("/coupons/edit/{id}")
    public String updateCoupon(@PathVariable("id") Integer id, @ModelAttribute("coupon") MaGiamGia coupon, RedirectAttributes redirectAttributes) {
        try {
            MaGiamGia existing = maGiamGiaService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại!"));
            
            if (!existing.getMaCode().equalsIgnoreCase(coupon.getMaCode())) {
                if (maGiamGiaService.findByCode(coupon.getMaCode()).isPresent()) {
                    throw new IllegalArgumentException("Mã giảm giá này đã tồn tại!");
                }
            }
            
            coupon.setId(id);
            maGiamGiaService.save(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
            return "redirect:/admin/coupons";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/coupons/edit/" + id;
        }
    }

    @GetMapping("/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            maGiamGiaService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa mã giảm giá này: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }
}
