package com.cedarco.controller;

import com.cedarco.dto.ProductDto;
import com.cedarco.entity.Product;
import com.cedarco.repository.UserRepository;
import com.cedarco.service.ProductService;
import com.cedarco.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;
    private final ProductService productService;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> getWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        List<Product> products = wishlistService.getWishlistProducts(getUserId(userDetails));
        return ResponseEntity.ok(products.stream().map(productService::toResponse).toList());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable("productId") Long productId) {
        wishlistService.addToWishlist(getUserId(userDetails), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable("productId") Long productId) {
        wishlistService.removeFromWishlist(getUserId(userDetails), productId);
        return ResponseEntity.noContent().build();
    }
}
