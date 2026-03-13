package com.cedarco.service;

import com.cedarco.dto.ProductDto;
import com.cedarco.entity.*;
import com.cedarco.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;

    public Page<ProductDto.Response> getAll(int page, int size, String sort) {
        Sort sortObj = switch (sort != null ? sort : "newest") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("rating").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return productRepository.findByActiveTrue(pageable).map(this::toResponse);
    }

    public ProductDto.Response getBySlug(String slug) {
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found: " + slug));
        return toResponse(p);
    }

    public List<ProductDto.Response> getFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductDto.Response> getNewArrivals() {
        return productRepository.findByNewArrivalTrueAndActiveTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductDto.Response> getBestSellers() {
        return productRepository.findByBestSellerTrueAndActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductDto.Response create(ProductDto.CreateRequest req) {
        Category category = req.getCategoryId() != null
                ? categoryRepository.findById(req.getCategoryId()).orElse(null) : null;
        Collection collection = req.getCollectionId() != null
                ? collectionRepository.findById(req.getCollectionId()).orElse(null) : null;

        Product product = Product.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .description(req.getDescription())
                .price(req.getPrice())
                .salePrice(req.getSalePrice())
                .sku(req.getSku())
                .category(category)
                .collection(collection)
                .gender(req.getGender() != null ? req.getGender() : Product.Gender.UNISEX)
                .featured(req.isFeatured())
                .newArrival(req.isNewArrival())
                .bestSeller(req.isBestSeller())
                .build();

        if (req.getImages() != null) {
            product.setImages(req.getImages().stream()
                .map(url -> ProductImage.builder().url(url).product(product).build())
                .collect(Collectors.toList()));
        }
        if (req.getSizes() != null) {
            product.setSizes(req.getSizes().stream()
                .map(s -> ProductSize.builder().name(s).product(product).build())
                .collect(Collectors.toList()));
        }
        if (req.getColors() != null) {
            product.setColors(req.getColors().stream()
                .map(c -> ProductColor.builder().name(c).product(product).build())
                .collect(Collectors.toList()));
        }
        if (req.getStockMap() != null) {
            product.setStock(req.getStockMap().entrySet().stream()
                .map(e -> ProductStock.builder().size(e.getKey()).quantity(e.getValue()).product(product).build())
                .collect(Collectors.toList()));
        }
        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        p.setActive(false);
        productRepository.save(p);
    }

    public ProductDto.Response toResponse(Product p) {
        ProductDto.Response r = new ProductDto.Response();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setSlug(p.getSlug());
        r.setDescription(p.getDescription());
        r.setPrice(p.getPrice());
        r.setSalePrice(p.getSalePrice());
        r.setSku(p.getSku());
        r.setCategoryName(p.getCategory() != null ? p.getCategory().getName() : null);
        r.setCollectionName(p.getCollection() != null ? p.getCollection().getName() : null);
        r.setCollectionSlug(p.getCollection() != null ? p.getCollection().getSlug() : null);
        r.setGender(p.getGender());
        r.setImages(p.getImages().stream().map(ProductImage::getUrl).collect(Collectors.toList()));
        r.setSizes(p.getSizes().stream().map(ProductSize::getName).collect(Collectors.toList()));
        r.setColors(p.getColors().stream().map(ProductColor::getName).collect(Collectors.toList()));
        r.setStockMap(p.getStock().stream().collect(Collectors.toMap(ProductStock::getSize, ProductStock::getQuantity)));
        r.setFeatured(p.isFeatured());
        r.setNewArrival(p.isNewArrival());
        r.setBestSeller(p.isBestSeller());
        r.setRating(p.getRating());
        r.setReviewCount(p.getReviewCount());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
