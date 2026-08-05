package com.web.app.controller;

import com.web.app.model.KhachHang;
import com.web.app.service.GioHangService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private GioHangService gioHangService;

    @ModelAttribute("cartSize")
    public int getCartSize(HttpSession session) {
        KhachHang kh = (KhachHang) session.getAttribute("user");
        if (kh == null) {
            return 0;
        }
        try {
            return gioHangService.getCartDetails(kh.getId()).stream()
                    .mapToInt(item -> item.getSoLuong())
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }
}
