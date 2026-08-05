package com.web.app.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String uri = request.getRequestURI();

        // 1. Admin path protection
        if (uri.startsWith("/admin")) {
            // Allow access to login endpoint
            if (uri.equals("/admin/login") || uri.equals("/login")) {
                return true;
            }
            if (session.getAttribute("admin") == null) {
                response.sendRedirect("/login?error=admin-required&redirect=" + uri);
                return false;
            }
            return true;
        }

        // 2. Customer protected paths (cart, checkout, profile, orders)
        if (uri.startsWith("/cart") || uri.startsWith("/checkout") || uri.startsWith("/profile") || uri.startsWith("/orders")) {
            if (session.getAttribute("user") == null) {
                response.sendRedirect("/login?error=login-required&redirect=" + uri);
                return false;
            }
            return true;
        }

        return true;
    }
}
