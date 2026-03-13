package com.cedarco.controller;

import com.cedarco.dto.ReviewDto;
import com.cedarco.repository.UserRepository;
import com.cedarco.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ReviewDto.Response>> getReviews(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto.Response> addReview(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReviewDto.CreateRequest req) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(reviewService.addReview(productId, userId, req));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReviewDto.CreateRequest req) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, req));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
