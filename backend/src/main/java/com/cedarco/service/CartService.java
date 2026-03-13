package com.cedarco.service;

import com.cedarco.dto.CartDto;
import com.cedarco.entity.*;
import com.cedarco.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartDto.Response getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        return toResponse(cart);
    }

    @Transactional
    public CartDto.Response addItem(Long userId, CartDto.AddItemRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if item with same product/size/color exists
        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(req.getProductId())
                        && equals(i.getSize(), req.getSize())
                        && equals(i.getColor(), req.getColor()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + (req.getQuantity() != null ? req.getQuantity() : 1)),
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .size(req.getSize())
                                    .color(req.getColor())
                                    .quantity(req.getQuantity() != null ? req.getQuantity() : 1)
                                    .build();
                            cart.getItems().add(item);
                        }
                );
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartDto.Response updateItem(Long userId, Long itemId, CartDto.UpdateItemRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (req.getQuantity() <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(req.getQuantity());
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartDto.Response removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = Cart.builder().user(user).build();
        return cartRepository.save(cart);
    }

    private CartDto.Response toResponse(Cart cart) {
        CartDto.Response r = new CartDto.Response();
        r.setId(cart.getId());
        List<CartDto.ItemResponse> items = cart.getItems().stream().map(i -> {
            CartDto.ItemResponse ir = new CartDto.ItemResponse();
            ir.setId(i.getId());
            ir.setProductId(i.getProduct().getId());
            ir.setProductName(i.getProduct().getName());
            ir.setProductSlug(i.getProduct().getSlug());
            ir.setImageUrl(!i.getProduct().getImages().isEmpty() ? i.getProduct().getImages().get(0).getUrl() : null);
            ir.setPrice(i.getProduct().getPrice());
            ir.setSalePrice(i.getProduct().getSalePrice());
            ir.setSize(i.getSize());
            ir.setColor(i.getColor());
            ir.setQuantity(i.getQuantity());
            BigDecimal unitPrice = i.getProduct().getSalePrice() != null
                    ? i.getProduct().getSalePrice() : i.getProduct().getPrice();
            ir.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(i.getQuantity())));
            return ir;
        }).collect(Collectors.toList());
        r.setItems(items);
        r.setTotal(items.stream().map(CartDto.ItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return r;
    }

    private boolean equals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
