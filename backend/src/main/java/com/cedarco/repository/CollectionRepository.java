package com.cedarco.repository;

import com.cedarco.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByActiveTrue();
    Optional<Collection> findBySlug(String slug);
}
