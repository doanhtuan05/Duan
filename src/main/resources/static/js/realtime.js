(function () {
    'use strict';
    var reloadTimer, currentUserId = window.HATS_REALTIME_USER_ID;
    function belongsToCurrentUser(event) { return !event.customerId || (currentUserId && String(event.customerId) === String(currentUserId)); }
    function pageNeedsRefresh(event) {
        var body = document.body;
        if (!belongsToCurrentUser(event)) return false;
        if (event.type === 'CATALOG') return body.classList.contains('storefront') || body.classList.contains('admin-dashboard');
        if (event.type === 'COUPONS') return body.classList.contains('admin-coupons') || body.classList.contains('admin-coupon-form');
        if (event.type === 'CART') return body.classList.contains('cart-page');
        if (event.type === 'ORDERS') return body.classList.contains('orders-page') || body.classList.contains('order-detail-page');
        if (event.type === 'PROFILE') return body.classList.contains('profile-page');
        if (event.type === 'ADMIN_ORDERS') return body.classList.contains('admin-orders') || body.classList.contains('admin-dashboard');
        return false;
    }
    function showRefreshNotice() {
        if (document.getElementById('realtimeRefreshNotice')) return;
        var notice = document.createElement('button');
        notice.id = 'realtimeRefreshNotice'; notice.type = 'button';
        notice.innerHTML = '<i class="fa-solid fa-rotate"></i> Dữ liệu vừa thay đổi. Tải lại';
        notice.addEventListener('click', function () { window.location.reload(); });
        document.body.appendChild(notice);
    }
    function refresh(event) {
        if (!pageNeedsRefresh(event)) return;
        var focused = document.activeElement;
        if (focused && /INPUT|TEXTAREA|SELECT/.test(focused.tagName)) { showRefreshNotice(); return; }
        clearTimeout(reloadTimer); reloadTimer = setTimeout(function () { window.location.reload(); }, 350);
    }
    function connect() {
        if (!window.SockJS || !window.Stomp) return;
        var client = Stomp.over(new SockJS('/ws')); client.debug = null;
        client.connect({}, function () { client.subscribe('/topic/updates', function (message) { try { refresh(JSON.parse(message.body)); } catch (e) { console.warn('Realtime message is invalid', e); } }); }, function () { setTimeout(connect, 5000); });
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', connect); else connect();
}());
