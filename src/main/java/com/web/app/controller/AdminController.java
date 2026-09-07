package com.web.app.controller;

import com.web.app.dto.DoanhThuDTO;
import com.web.app.model.*;
import com.web.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Stream;
import com.web.app.dto.KhachHangAdminDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.web.app.repository.BienTheSanPhamRepository;
import com.web.app.repository.ChiTietDonHangRepository;
import com.web.app.repository.ChiTietGioHangRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private ChiTietGioHangRepository chiTietGioHangRepository;

    // 1. Dashboard & Statistics
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        List<DoanhThuDTO> dsNgay = thongKeService.getDoanhThuTheoNgay();
        List<DoanhThuDTO> dsThang = thongKeService.getDoanhThuTheoThang();
        List<DoanhThuDTO> dsNam = thongKeService.getDoanhThuTheoNam();

        // Calculate summaries
        double tongDoanhThu = dsNgay.stream().mapToDouble(DoanhThuDTO::getRevenue).sum();
        long tongDonHang = dsNgay.stream().mapToLong(DoanhThuDTO::getOrderCount).sum();

        double doanhThuHienTai = dsNgay.isEmpty() ? 0 : dsNgay.get(0).getRevenue();
        double doanhThuKyTruoc = dsNgay.size() < 2 ? 0 : dsNgay.get(1).getRevenue();
        long donHangHienTai = dsNgay.isEmpty() ? 0 : dsNgay.get(0).getOrderCount();
        long donHangKyTruoc = dsNgay.size() < 2 ? 0 : dsNgay.get(1).getOrderCount();

        double tangTruongDoanhThu = tinhPhanTramThayDoi(doanhThuHienTai, doanhThuKyTruoc);
        double tangTruongDonHang = tinhPhanTramThayDoi(donHangHienTai, donHangKyTruoc);

        model.addAttribute("dsNgay", dsNgay);
        model.addAttribute("dsThang", dsThang);
        model.addAttribute("dsNam", dsNam);
        model.addAttribute("tongDoanhThu", tongDoanhThu);
        model.addAttribute("tongDonHang", tongDonHang);
        model.addAttribute("tangTruongDoanhThu", tangTruongDoanhThu);
        model.addAttribute("tangTruongDonHang", tangTruongDonHang);

        return "admin/dashboard";
    }

    private double tinhPhanTramThayDoi(double hienTai, double kyTruoc) {
        if (kyTruoc == 0) {
            return hienTai > 0 ? 100 : 0;
        }
        return ((hienTai - kyTruoc) / kyTruoc) * 100;
    }

    // 2. Product Management (CRUD)
    @GetMapping("/products")
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", required = false) Integer categoryId,
                               @RequestParam(value = "brandId", required = false) Integer brandId,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        Page<SanPham> prodPage = sanPhamService.getFilteredProducts(keyword, categoryId, brandId, null, null, page, size);
        model.addAttribute("products", prodPage.getContent());
        Map<Integer, Long> shippingQuantities = new HashMap<>();
        prodPage.getContent().forEach(product -> shippingQuantities.put(product.getId(),
                chiTietDonHangRepository.sumShippingQuantityByProductId(product.getId())));
        model.addAttribute("shippingQuantities", shippingQuantities);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prodPage.getTotalPages());
        model.addAttribute("totalItems", prodPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("categories", danhMucService.findAll());
        model.addAttribute("brands", thuongHieuService.findAll());
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
                                @RequestParam(value = "variantId", required = false) List<Integer> variantIds,
                                @RequestParam(value = "variantColor", required = false) List<String> variantColors,
                                @RequestParam(value = "variantSize", required = false) List<String> variantSizes,
                                @RequestParam(value = "variantSku", required = false) List<String> variantSkus,
                                @RequestParam(value = "variantPrice", required = false) List<Double> variantPrices,
                                @RequestParam(value = "variantStock", required = false) List<Integer> variantStocks,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                sp.setAnhUrl(fileName);
            }
            sanPhamService.save(sp);
            saveVariants(sp, variantIds, variantColors, variantSizes, variantSkus, variantPrices, variantStocks);
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
                              @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
                              @RequestParam(value = "variantId", required = false) List<Integer> variantIds,
                              @RequestParam(value = "variantColor", required = false) List<String> variantColors,
                              @RequestParam(value = "variantSize", required = false) List<String> variantSizes,
                              @RequestParam(value = "variantSku", required = false) List<String> variantSkus,
                              @RequestParam(value = "variantPrice", required = false) List<Double> variantPrices,
                              @RequestParam(value = "variantStock", required = false) List<Integer> variantStocks,
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

            if (removeImage) {
                existing.setAnhUrl(null);
            } else if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                existing.setAnhUrl(fileName);
            }

            sanPhamService.save(existing);
            saveVariants(existing, variantIds, variantColors, variantSizes, variantSkus, variantPrices, variantStocks);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi lưu ảnh sản phẩm!");
        }
        return "redirect:/admin/products";
    }

    private void saveVariants(SanPham product, List<Integer> ids, List<String> colors, List<String> sizes,
                              List<String> skus, List<Double> prices, List<Integer> stocks) {
        if (colors == null) return;
        List<BienTheSanPham> existing = bienTheSanPhamRepository.findBySanPhamIdOrderByMauSacAscKichCoAsc(product.getId());
        Set<Integer> keptIds = new HashSet<>();
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i) == null || colors.get(i).isBlank()) continue;
            Integer variantId = ids != null && ids.size() > i ? ids.get(i) : null;
            BienTheSanPham variant = variantId == null
                    ? BienTheSanPham.builder().sanPham(product).build()
                    : bienTheSanPhamRepository.findById(variantId).orElseThrow(() -> new IllegalArgumentException("Biến thể không tồn tại!"));
            if (variantId != null && !variant.getSanPham().getId().equals(product.getId()))
                throw new IllegalArgumentException("Biến thể không thuộc sản phẩm này!");
            variant.setMauSac(colors.get(i).trim());
            variant.setKichCo(sizes.get(i).trim());
            variant.setSku(skus.get(i).isBlank() ? null : skus.get(i).trim());
            variant.setGia(prices.get(i));
            variant.setSoLuong(stocks.get(i));
            bienTheSanPhamRepository.save(variant);
            keptIds.add(variant.getId());
        }
        for (BienTheSanPham variant : existing) {
            if (!keptIds.contains(variant.getId())) {
                if (chiTietDonHangRepository.existsByBienTheId(variant.getId()) || chiTietGioHangRepository.existsByBienTheId(variant.getId()))
                    throw new IllegalArgumentException("Không thể xóa biến thể đã có trong đơn hàng hoặc giỏ hàng.");
                bienTheSanPhamRepository.delete(variant);
            }
        }
        int totalStock = bienTheSanPhamRepository.findBySanPhamIdOrderByMauSacAscKichCoAsc(product.getId()).stream()
                .mapToInt(BienTheSanPham::getSoLuong).sum();
        product.setSoLuong(totalStock);
        sanPhamService.save(product);
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
        String name = dm.getTenDanhMuc() == null ? "" : dm.getTenDanhMuc().trim();
        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("categoryError", "Tên danh mục không được để trống.");
            return "redirect:/admin/categories";
        }
        if (danhMucService.nameExists(name)) {
            redirectAttributes.addFlashAttribute("categoryError", "Danh mục ‘" + name + "’ đã tồn tại.");
            redirectAttributes.addFlashAttribute("newCategoryName", name);
            return "redirect:/admin/categories";
        }
        dm.setTenDanhMuc(name);
        danhMucService.save(dm);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Integer id, @RequestParam("tenDanhMuc") String name, RedirectAttributes redirectAttributes) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên danh mục không được để trống.");
            return "redirect:/admin/categories";
        }
        if (danhMucService.nameExistsForOtherCategory(name, id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên danh mục này đã được sử dụng.");
            return "redirect:/admin/categories";
        }
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
        String name = th.getTenThuongHieu() == null ? "" : th.getTenThuongHieu().trim();
        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("brandError", "Tên thương hiệu không được để trống.");
            return "redirect:/admin/brands";
        }
        if (thuongHieuService.nameExists(name)) {
            redirectAttributes.addFlashAttribute("brandError", "Thương hiệu ‘" + name + "’ đã tồn tại.");
            redirectAttributes.addFlashAttribute("newBrandName", name);
            return "redirect:/admin/brands";
        }
        th.setTenThuongHieu(name);
        thuongHieuService.save(th);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm thương hiệu thành công!");
        return "redirect:/admin/brands";
    }

    @PostMapping("/brands/edit/{id}")
    public String editBrand(@PathVariable("id") Integer id, @RequestParam("tenThuongHieu") String name, RedirectAttributes redirectAttributes) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên thương hiệu không được để trống.");
            return "redirect:/admin/brands";
        }
        if (thuongHieuService.nameExistsForOtherBrand(name, id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên thương hiệu này đã được sử dụng.");
            return "redirect:/admin/brands";
        }
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
    public String listCustomers(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "sort", defaultValue = "newest") String sort,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        Stream<KhachHangAdminDTO> stream = khachHangService.getAdminCustomers().stream();
        if (keyword != null && !keyword.isBlank()) {
            String term = keyword.trim().toLowerCase();
            stream = stream.filter(row -> (row.getCustomer().getHoTen() != null && row.getCustomer().getHoTen().toLowerCase().contains(term))
                    || (row.getCustomer().getEmail() != null && row.getCustomer().getEmail().toLowerCase().contains(term))
                    || (row.getCustomer().getSoDienThoai() != null && row.getCustomer().getSoDienThoai().contains(term)));
        }
        if (status != null && !status.isBlank()) {
            stream = stream.filter(row -> row.getCustomer().getTaiKhoan() != null
                    && status.equals(row.getCustomer().getTaiKhoan().getTrangThai()));
        }
        Comparator<KhachHangAdminDTO> comparator = switch (sort) {
            case "spending" -> Comparator.comparingDouble(KhachHangAdminDTO::getTotalSpent).reversed();
            case "orders" -> Comparator.comparingLong(KhachHangAdminDTO::getOrderCount).reversed();
            default -> Comparator.comparing(row -> row.getCustomer().getId(), Comparator.reverseOrder());
        };
        List<KhachHangAdminDTO> filtered = stream.sorted(comparator).toList();
        int safePage = Math.min(Math.max(page, 0), Math.max(0, (filtered.size() - 1) / size));
        int start = Math.min(safePage * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        Page<KhachHangAdminDTO> customerPage = new PageImpl<>(filtered.subList(start, end), PageRequest.of(safePage, size), filtered.size());
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalItems", customerPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        return "admin/customers";
    }

    @GetMapping("/customers/{id}")
    public String viewCustomer(@PathVariable Integer id, Model model) {
        KhachHang customer = khachHangService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại."));
        List<DonHang> orders = donHangService.getOrderHistory(id);
        double totalSpent = orders.stream().filter(order -> "DELIVERED".equals(order.getTrangThai()))
                .mapToDouble(order -> order.getTongTien() == null ? 0 : order.getTongTien()).sum();
        model.addAttribute("customer", customer);
        model.addAttribute("orders", orders);
        model.addAttribute("totalSpent", totalSpent);
        return "admin/customer_detail";
    }

    @PostMapping("/customers/{id}/toggle-status")
    public String toggleCustomerStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            khachHangService.toggleAccountStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái tài khoản.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    // 6. Order Management
    @GetMapping("/orders")
    public String listOrders(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                             @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        LocalDateTime fromDateTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        Page<DonHang> orderPage = donHangService.getFilteredOrders(keyword, status, fromDateTime, toDateTime, page, size);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
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
    public String listCoupons(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                              @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size, Model model) {
        LocalDateTime now = LocalDateTime.now();
        Stream<MaGiamGia> stream = maGiamGiaService.findAll().stream();
        if (keyword != null && !keyword.isBlank()) { String term = keyword.trim().toLowerCase(); stream = stream.filter(c -> c.getMaCode() != null && c.getMaCode().toLowerCase().contains(term)); }
        if (status != null && !status.isBlank()) stream = stream.filter(c -> status.equals(getCouponStatus(c, now)));
        if (fromDate != null) { LocalDateTime startDate = fromDate.atStartOfDay(); stream = stream.filter(c -> c.getNgayKetThuc() == null || !c.getNgayKetThuc().isBefore(startDate)); }
        if (toDate != null) { LocalDateTime endDate = toDate.plusDays(1).atStartOfDay(); stream = stream.filter(c -> c.getNgayBatDau() == null || c.getNgayBatDau().isBefore(endDate)); }
        List<MaGiamGia> filtered = stream.sorted(Comparator.comparing(MaGiamGia::getId).reversed()).toList();
        int safePage = Math.min(Math.max(page, 0), Math.max(0, (filtered.size() - 1) / size));
        int start = Math.min(safePage * size, filtered.size()), end = Math.min(start + size, filtered.size());
        Page<MaGiamGia> couponPage = new PageImpl<>(filtered.subList(start, end), PageRequest.of(safePage, size), filtered.size());
        model.addAttribute("coupons", couponPage.getContent()); model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", couponPage.getTotalPages()); model.addAttribute("totalItems", couponPage.getTotalElements());
        model.addAttribute("keyword", keyword); model.addAttribute("status", status); model.addAttribute("fromDate", fromDate); model.addAttribute("toDate", toDate);
        model.addAttribute("now", now); model.addAttribute("warningDate", now.plusDays(3));
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

    private String getCouponStatus(MaGiamGia coupon, LocalDateTime now) {
        if (coupon.getSoLuong() != null && coupon.getSoLuongDaDung() != null && coupon.getSoLuongDaDung() >= coupon.getSoLuong()) return "EXHAUSTED";
        if (coupon.getNgayKetThuc() != null && now.isAfter(coupon.getNgayKetThuc())) return "EXPIRED";
        if (coupon.getNgayBatDau() != null && now.isBefore(coupon.getNgayBatDau())) return "UPCOMING";
        return Boolean.TRUE.equals(coupon.getTrangThai()) ? "ACTIVE" : "DISABLED";
    }
}
