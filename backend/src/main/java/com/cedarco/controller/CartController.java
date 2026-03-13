package com.cedarco.controller;

import com.cedarco.dto.CartDto;
import com.cedarco.repository.UserRepository;
import com.cedarco.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @GetMapping
    public ResponseEntity<CartDto.Response> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(getUserId(userDetails)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto.Response> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                                     @RequestBody CartDto.AddItemRequest req) {
        return ResponseEntity.ok(cartService.addItem(getUserId(userDetails), req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto.Response> updateItem(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable("itemId") Long itemId,
                                                        @RequestBody CartDto.UpdateItemRequest req) {
        return ResponseEntity.ok(cartService.updateItem(getUserId(userDetails), itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto.Response> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable("itemId") Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(getUserId(userDetails), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(getUserId(userDetails));
        return ResponseEntity.noContent().build();
    }
}
