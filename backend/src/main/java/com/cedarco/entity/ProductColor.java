package com.cedarco.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_colors")
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
