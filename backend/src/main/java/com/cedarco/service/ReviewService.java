package com.cedarco.service;

import com.cedarco.dto.ReviewDto;
import com.cedarco.entity.*;
import com.cedarco.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<ReviewDto.Response> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto.Response addReview(Long productId, Long userId, ReviewDto.CreateRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(req.getRating())
                .title(req.getTitle())
                .body(req.getBody())
                .build();
        review = reviewRepository.save(review);
        recalculateProductRating(productId);
        return toResponse(review);
    }

    @Transactional
    public ReviewDto.Response updateReview(Long reviewId, Long userId, ReviewDto.CreateRequest req) {
        System.out.println("Updating review " + reviewId + " for user " + userId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        System.out.println("Review owner: " + review.getUser().getId());
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this review");
        }

        review.setRating(req.getRating());
        review.setTitle(req.getTitle());
        review.setBody(req.getBody());
        review = reviewRepository.save(review);
        System.out.println("Review saved, recalculating rating...");

        recalculateProductRating(review.getProduct().getId());

        return toResponse(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        System.out.println("Deleting review " + reviewId + " for user " + userId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this review");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        System.out.println("Review deleted, recalculating rating for product " + productId);
        recalculateProductRating(productId);
    }

    private void recalculateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        if (reviews.isEmpty()) {
            product.setRating(0.0);
            product.setReviewCount(0);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            product.setRating(Math.round(avg * 10.0) / 10.0);
            product.setReviewCount(reviews.size());
        }
        productRepository.save(product);
    }

    private ReviewDto.Response toResponse(Review r) {
        ReviewDto.Response res = new ReviewDto.Response();
        res.setId(r.getId());
        res.setUserId(r.getUser().getId());
        res.setUserName(r.getUser().getFirstName() + " " + r.getUser().getLastName());
        res.setRating(r.getRating());
        res.setTitle(r.getTitle());
        res.setBody(r.getBody());
        res.setVerified(r.isVerified());
        res.setCreatedAt(r.getCreatedAt().toString());
        return res;
    }
}
