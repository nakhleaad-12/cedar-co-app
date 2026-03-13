package com.cedarco.service;

import com.cedarco.dto.OrderDto;
import com.cedarco.entity.*;
import com.cedarco.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public OrderDto.Response placeOrder(Long userId, OrderDto.CreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingStreet(req.getShippingStreet());
        order.setShippingCity(req.getShippingCity());
        order.setShippingRegion(req.getShippingRegion());
        order.setShippingCountry(req.getShippingCountry());
        order.setPaymentMethod(req.getPaymentMethod());

        // Create order items from cart
        List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
            BigDecimal unitPrice = ci.getProduct().getSalePrice() != null
                    ? ci.getProduct().getSalePrice() : ci.getProduct().getPrice();
            return OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .productName(ci.getProduct().getName())
                    .size(ci.getSize())
                    .color(ci.getColor())
                    .quantity(ci.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
        }).collect(Collectors.toList());
        order.setItems(orderItems);

        BigDecimal subtotal = orderItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);

        // Apply coupon
        BigDecimal discount = BigDecimal.ZERO;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCodeAndActiveTrue(req.getCouponCode()).orElse(null);
            if (coupon != null && subtotal.compareTo(coupon.getMinOrderAmount()) >= 0) {
                if (coupon.getDiscountType() == Coupon.DiscountType.PERCENT) {
                    discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                } else {
                    discount = coupon.getDiscountValue();
                }
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                couponRepository.save(coupon);
                order.setCouponCode(req.getCouponCode());
            }
        }
        order.setDiscount(discount);
        order.setTotal(subtotal.subtract(discount));

        Order saved = orderRepository.save(order);

        // Clear cart after order
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(saved);
    }

    public List<OrderDto.Response> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderDto.Response getOrderById(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return toResponse(o);
    }

    public Page<OrderDto.Response> getAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional
    public OrderDto.Response updateStatus(Long orderId, String status) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        o.setStatus(Order.OrderStatus.valueOf(status));
        return toResponse(orderRepository.save(o));
    }

    @Transactional
    public OrderDto.Response updatePaymentStatus(Long orderId, String paymentStatus) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        o.setPaymentStatus(Order.PaymentStatus.valueOf(paymentStatus));
        return toResponse(orderRepository.save(o));
    }

    private OrderDto.Response toResponse(Order o) {
        OrderDto.Response r = new OrderDto.Response();
        r.setId(o.getId());
        r.setStatus(o.getStatus().name());
        r.setPaymentStatus(o.getPaymentStatus().name());
        r.setSubtotal(o.getSubtotal());
        r.setDiscount(o.getDiscount());
        r.setTotal(o.getTotal());
        r.setCouponCode(o.getCouponCode());
        r.setPaymentMethod(o.getPaymentMethod());
        r.setShippingCity(o.getShippingCity());
        r.setShippingCountry(o.getShippingCountry());
        r.setCreatedAt(o.getCreatedAt());
        r.setItems(o.getItems().stream().map(i -> {
            OrderDto.ItemResponse ir = new OrderDto.ItemResponse();
            ir.setProductId(i.getProduct() != null ? i.getProduct().getId() : null);
            ir.setProductName(i.getProductName());
            ir.setSize(i.getSize());
            ir.setColor(i.getColor());
            ir.setQuantity(i.getQuantity());
            ir.setUnitPrice(i.getUnitPrice());
            return ir;
        }).collect(Collectors.toList()));
        return r;
    }
}
