package com.cedarco.controller;

import com.cedarco.dto.ProductDto;
import com.cedarco.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDto.Response>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "collection", required = false) String collection,
            @RequestParam(name = "gender", required = false) String gender) {
        return ResponseEntity.ok(productService.getAll(page, size, sort));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDto.Response> getBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto.Response>> getFeatured() {
        return ResponseEntity.ok(productService.getFeatured());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductDto.Response>> getNewArrivals() {
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<List<ProductDto.Response>> getBestSellers() {
        return ResponseEntity.ok(productService.getBestSellers());
    }
}
