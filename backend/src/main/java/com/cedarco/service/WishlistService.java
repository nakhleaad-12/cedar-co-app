package com.cedarco.service;

import com.cedarco.entity.*;
import com.cedarco.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Wishlist getOrCreate(Long userId) {
        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return wishlistRepository.save(Wishlist.builder().user(user).build());
        });
    }

    public List<Product> getWishlistProducts(Long userId) {
        return getOrCreate(userId).getItems().stream()
                .map(WishlistItem::getProduct).toList();
    }

    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        Wishlist wishlist = getOrCreate(userId);
        boolean exists = wishlist.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(productId));
        if (!exists) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            WishlistItem item = WishlistItem.builder().wishlist(wishlist).product(product).build();
            wishlist.getItems().add(item);
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = getOrCreate(userId);
        wishlist.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        wishlistRepository.save(wishlist);
    }
}
