package com.web.app.controller;

import com.web.app.model.KhachHang;
import com.web.app.model.TaiKhoan;
import com.web.app.service.KhachHangService;
import com.web.app.service.TaiKhoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                @RequestParam(value = "registerSuccess", required = false) String registerSuccess,
                                Model model, HttpSession session) {
        // If already logged in, redirect based on role
        if (session.getAttribute("admin") != null) {
            return "redirect:/admin/dashboard";
        }
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }

        if (error != null) {
            if (error.equals("invalid")) {
                model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            } else if (error.equals("login-required")) {
                model.addAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện chức năng này!");
            } else if (error.equals("admin-required")) {
                model.addAttribute("errorMessage", "Quyền quản trị viên yêu cầu đăng nhập!");
            }
        }
        if (registerSuccess != null) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam(value = "redirect", required = false) String redirect,
                               HttpSession session, Model model) {
        try {
            TaiKhoan tk = taiKhoanService.dangNhap(username, password)
                    .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác!"));
            if (tk.getVaiTro().equalsIgnoreCase("ADMIN")) {
                session.setAttribute("admin", tk);
                if (redirect != null && !redirect.isBlank() && redirect.startsWith("/admin")) {
                    return "redirect:" + redirect;
                }
                return "redirect:/admin/dashboard";
            } else {
                KhachHang kh = khachHangService.findByTaiKhoanId(tk.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng!"));
                session.setAttribute("user", kh);
                session.setAttribute("account", tk);
                if (redirect != null && !redirect.isBlank() && !redirect.contains("/admin")) {
                    return "redirect:" + redirect;
                }
                return "redirect:/";
            }
        } catch (IllegalArgumentException e) {
            return "redirect:/login?error=invalid" + (redirect != null ? "&redirect=" + redirect : "");
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("email") String email,
                                  @RequestParam("address") String address,
                                  Model model) {
        try {
            // Check if username is empty
            if (username.isBlank() || password.isBlank() || fullName.isBlank()) {
                throw new IllegalArgumentException("Vui lòng điền đầy đủ các thông tin bắt buộc!");
            }
            
            taiKhoanService.dangKy(username, password, fullName, email, phone, address);
            return "redirect:/login?registerSuccess=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
