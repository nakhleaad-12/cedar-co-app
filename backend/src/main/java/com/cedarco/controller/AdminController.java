package com.cedarco.controller;

import com.cedarco.dto.AdminDto;
import com.cedarco.dto.CouponDto;
import com.cedarco.dto.OrderDto;
import com.cedarco.dto.ProductDto;
import com.cedarco.entity.Banner;
import com.cedarco.entity.Coupon;
import com.cedarco.entity.Order;
import com.cedarco.repository.*;
import com.cedarco.service.CouponService;
import com.cedarco.service.OrderService;
import com.cedarco.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final BannerRepository bannerRepository;

    // ─── Dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDto.DashboardStats> getStats() {
        AdminDto.DashboardStats stats = new AdminDto.DashboardStats();
        stats.setTotalProducts(productRepository.count());
        stats.setTotalOrders(orderRepository.count());
        stats.setTotalCustomers(userRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(Order.OrderStatus.PENDING));
        stats.setTotalRevenue(orderRepository.findAll().stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return ResponseEntity.ok(stats);
    }

    // ─── Products ──────────────────────────────────────────────
    @PostMapping("/products")
    public ResponseEntity<ProductDto.Response> createProduct(@RequestBody ProductDto.CreateRequest req) {
        return ResponseEntity.ok(productService.create(req));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(name = "id") Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Orders ────────────────────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDto.Response>> getAllOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(@PathVariable(name = "id") Long id,
                                                                 @RequestBody OrderDto.UpdateStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(id, req.getStatus()));
    }

    @PutMapping("/orders/{id}/payment-status")
    public ResponseEntity<OrderDto.Response> updatePaymentStatus(@PathVariable(name = "id") Long id,
                                                                 @RequestBody OrderDto.UpdatePaymentStatusRequest req) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, req.getPaymentStatus()));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<com.cedarco.dto.UserDto.Response>> getCustomers() {
        List<com.cedarco.dto.UserDto.Response> responses = userRepository.findAll().stream().map(u -> {
            com.cedarco.dto.UserDto.Response r = new com.cedarco.dto.UserDto.Response();
            r.setId(u.getId());
            r.setFirstName(u.getFirstName());
            r.setLastName(u.getLastName());
            r.setEmail(u.getEmail());
            r.setPhone(u.getPhone());
            r.setRole(u.getRole().name());
            r.setActive(u.isActive());
            r.setCreatedAt(u.getCreatedAt());
            return r;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ─── Coupons ───────────────────────────────────────────────
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponService.getAll());
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody CouponDto.CreateRequest req) {
        return ResponseEntity.ok(couponService.create(req));
    }

    // ─── Banners ───────────────────────────────────────────────
    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getBanners() {
        return ResponseEntity.ok(bannerRepository.findAll());
    }

    @PostMapping("/banners")
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        return ResponseEntity.ok(bannerRepository.save(banner));
    }
}
