package com.cedarco.dto;

import com.cedarco.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProductDto {

    public static class Response {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private BigDecimal price;
        private BigDecimal salePrice;
        private String sku;
        private String categoryName;
        private String collectionName;
        private String collectionSlug;
        private Product.Gender gender;
        private List<String> images;
        private List<String> sizes;
        private List<String> colors;
        private Map<String, Integer> stockMap;
        private boolean featured;
        private boolean newArrival;
        private boolean bestSeller;
        private Double rating;
        private Integer reviewCount;
        private LocalDateTime createdAt;

        // Explicit Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getSalePrice() { return salePrice; }
        public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getCollectionName() { return collectionName; }
        public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
        public String getCollectionSlug() { return collectionSlug; }
        public void setCollectionSlug(String collectionSlug) { this.collectionSlug = collectionSlug; }
        public Product.Gender getGender() { return gender; }
        public void setGender(Product.Gender gender) { this.gender = gender; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        public List<String> getSizes() { return sizes; }
        public void setSizes(List<String> sizes) { this.sizes = sizes; }
        public List<String> getColors() { return colors; }
        public void setColors(List<String> colors) { this.colors = colors; }
        public Map<String, Integer> getStockMap() { return stockMap; }
        public void setStockMap(Map<String, Integer> stockMap) { this.stockMap = stockMap; }
        public boolean isFeatured() { return featured; }
        public void setFeatured(boolean featured) { this.featured = featured; }
        public boolean isNewArrival() { return newArrival; }
        public void setNewArrival(boolean newArrival) { this.newArrival = newArrival; }
        public boolean isBestSeller() { return bestSeller; }
        public void setBestSeller(boolean bestSeller) { this.bestSeller = bestSeller; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CreateRequest {
        private String name;
        private String slug;
        private String description;
        private BigDecimal price;
        private BigDecimal salePrice;
        private String sku;
        private Long categoryId;
        private Long collectionId;
        private Product.Gender gender;
        private List<String> images;
        private List<String> sizes;
        private List<String> colors;
        private Map<String, Integer> stockMap;
        private boolean featured;
        private boolean newArrival;
        private boolean bestSeller;

        // Explicit Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getSalePrice() { return salePrice; }
        public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getCollectionId() { return collectionId; }
        public void setCollectionId(Long collectionId) { this.collectionId = collectionId; }
        public Product.Gender getGender() { return gender; }
        public void setGender(Product.Gender gender) { this.gender = gender; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        public List<String> getSizes() { return sizes; }
        public void setSizes(List<String> sizes) { this.sizes = sizes; }
        public List<String> getColors() { return colors; }
        public void setColors(List<String> colors) { this.colors = colors; }
        public Map<String, Integer> getStockMap() { return stockMap; }
        public void setStockMap(Map<String, Integer> stockMap) { this.stockMap = stockMap; }
        public boolean isFeatured() { return featured; }
        public void setFeatured(boolean featured) { this.featured = featured; }
        public boolean isNewArrival() { return newArrival; }
        public void setNewArrival(boolean newArrival) { this.newArrival = newArrival; }
        public boolean isBestSeller() { return bestSeller; }
        public void setBestSeller(boolean bestSeller) { this.bestSeller = bestSeller; }
    }

    @Data
    public static class FilterRequest {
        private Long categoryId;
        private Long collectionId;
        private String gender;
        private String size;
        private String color;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String sort; // price_asc, price_desc, newest, rating
        private int page = 0;
        private int size2 = 12;
    }
}
