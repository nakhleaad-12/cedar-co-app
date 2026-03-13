package com.cedarco.controller;

import com.cedarco.entity.Category;
import com.cedarco.entity.Collection;
import com.cedarco.repository.CategoryRepository;
import com.cedarco.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findByActiveTrue());
    }

    @GetMapping("/collections")
    public ResponseEntity<List<Collection>> getCollections() {
        return ResponseEntity.ok(collectionRepository.findByActiveTrue());
    }

    @GetMapping("/collections/{slug}")
    public ResponseEntity<Collection> getCollection(@PathVariable("slug") String slug) {
        Collection c = collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Collection not found: " + slug));
        return ResponseEntity.ok(c);
    }
}
