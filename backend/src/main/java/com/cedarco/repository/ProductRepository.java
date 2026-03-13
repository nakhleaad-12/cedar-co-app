package com.cedarco.repository;

import com.cedarco.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    List<Product> findByFeaturedTrueAndActiveTrue();
    List<Product> findByNewArrivalTrueAndActiveTrueOrderByCreatedAtDesc();
    List<Product> findByBestSellerTrueAndActiveTrue();
    Page<Product> findByActiveTrue(Pageable pageable);
}
