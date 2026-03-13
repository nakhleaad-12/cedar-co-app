package com.cedarco.service;

import com.cedarco.dto.ProductDto;
import com.cedarco.entity.*;
import com.cedarco.entity.Collection;
import com.cedarco.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CollectionRepository collectionRepository;

    @InjectMocks private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Cedar Jacket")
                .slug("cedar-jacket")
                .description("A fine Lebanese jacket")
                .price(new BigDecimal("120.00"))
                .salePrice(new BigDecimal("99.00"))
                .sku("CJ-001")
                .gender(Product.Gender.UNISEX)
                .featured(true)
                .newArrival(true)
                .bestSeller(false)
                .active(true)
                .rating(4.5)
                .reviewCount(10)
                .build();
        
        sampleProduct.setImages(new ArrayList<>(List.of(ProductImage.builder().url("https://img.test/cedar-jacket.jpg").product(sampleProduct).build())));
        sampleProduct.setSizes(new ArrayList<>(List.of(
            ProductSize.builder().name("S").product(sampleProduct).build(),
            ProductSize.builder().name("M").product(sampleProduct).build(),
            ProductSize.builder().name("L").product(sampleProduct).build()
        )));
        sampleProduct.setColors(new ArrayList<>(List.of(
            ProductColor.builder().name("Black").product(sampleProduct).build(),
            ProductColor.builder().name("White").product(sampleProduct).build()
        )));
    }

    // ─── getAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll returns paginated products sorted by newest")
    void getAll_defaultSort_returnsPage() {
        Page<Product> mockPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(mockPage);

        Page<ProductDto.Response> result = productService.getAll(0, 10, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cedar Jacket");
        verify(productRepository).findByActiveTrue(any(Pageable.class));
    }

    @Test
    @DisplayName("getAll with price_asc sort uses ascending price sort")
    void getAll_priceAscSort_returnsPage() {
        Page<Product> mockPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(mockPage);

        Page<ProductDto.Response> result = productService.getAll(0, 5, "price_asc");

        assertThat(result).isNotNull();
        verify(productRepository).findByActiveTrue(any(Pageable.class));
    }

    @Test
    @DisplayName("getAll with price_desc sort succeeds")
    void getAll_priceDescSort_returnsPage() {
        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleProduct)));

        Page<ProductDto.Response> result = productService.getAll(0, 5, "price_desc");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getAll with rating sort succeeds")
    void getAll_ratingSortReturnsPage() {
        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleProduct)));

        Page<ProductDto.Response> result = productService.getAll(0, 5, "rating");
        assertThat(result).isNotNull();
    }

    // ─── getBySlug ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBySlug returns product when slug exists")
    void getBySlug_existingSlug_returnsDto() {
        when(productRepository.findBySlug("cedar-jacket")).thenReturn(Optional.of(sampleProduct));

        ProductDto.Response result = productService.getBySlug("cedar-jacket");

        assertThat(result.getSlug()).isEqualTo("cedar-jacket");
        assertThat(result.getPrice()).isEqualByComparingTo("120.00");
        assertThat(result.getSalePrice()).isEqualByComparingTo("99.00");
    }

    @Test
    @DisplayName("getBySlug throws RuntimeException when slug not found")
    void getBySlug_missingSlug_throwsException() {
        when(productRepository.findBySlug("missing-slug")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getBySlug("missing-slug"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found: missing-slug");
    }

    // ─── getFeatured ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getFeatured returns only featured active products")
    void getFeatured_returnsFeaturedProducts() {
        when(productRepository.findByFeaturedTrueAndActiveTrue()).thenReturn(List.of(sampleProduct));

        List<ProductDto.Response> result = productService.getFeatured();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isFeatured()).isTrue();
    }

    @Test
    @DisplayName("getFeatured returns empty list when no featured products")
    void getFeatured_noProducts_returnsEmpty() {
        when(productRepository.findByFeaturedTrueAndActiveTrue()).thenReturn(Collections.emptyList());

        assertThat(productService.getFeatured()).isEmpty();
    }

    // ─── getNewArrivals ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getNewArrivals returns list of new arrival products")
    void getNewArrivals_returnsNewArrivals() {
        when(productRepository.findByNewArrivalTrueAndActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleProduct));

        List<ProductDto.Response> result = productService.getNewArrivals();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isNewArrival()).isTrue();
    }

    // ─── getBestSellers ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getBestSellers returns empty when no best sellers exist")
    void getBestSellers_noProducts_returnsEmpty() {
        when(productRepository.findByBestSellerTrueAndActiveTrue()).thenReturn(Collections.emptyList());

        assertThat(productService.getBestSellers()).isEmpty();
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create saves product and returns mapped DTO")
    void create_validRequest_savesAndReturns() {
        ProductDto.CreateRequest req = new ProductDto.CreateRequest();
        req.setName("Cedar Jacket");
        req.setSlug("cedar-jacket");
        req.setPrice(new BigDecimal("120.00"));
        req.setSalePrice(new BigDecimal("99.00"));
        req.setSku("CJ-001");
        req.setFeatured(true);
        req.setNewArrival(true);

        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto.Response result = productService.create(req);

        assertThat(result.getName()).isEqualTo("Cedar Jacket");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("create with categoryId looks up category from repository")
    void create_withCategoryId_lookupCategory() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Outerwear");

        ProductDto.CreateRequest req = new ProductDto.CreateRequest();
        req.setName("Test");
        req.setSlug("test");
        req.setPrice(new BigDecimal("50.00"));
        req.setCategoryId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.create(req);

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("create with collectionId looks up collection from repository")
    void create_withCollectionId_lookupCollection() {
        Collection col = new Collection();
        col.setId(2L);
        col.setName("Summer");

        ProductDto.CreateRequest req = new ProductDto.CreateRequest();
        req.setName("Test");
        req.setSlug("test-2");
        req.setPrice(new BigDecimal("75.00"));
        req.setCollectionId(2L);

        when(collectionRepository.findById(2L)).thenReturn(Optional.of(col));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.create(req);

        verify(collectionRepository).findById(2L);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete soft-deletes product by setting active=false")
    void delete_existingProduct_setsActiveToFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.delete(1L);

        assertThat(sampleProduct.isActive()).isFalse();
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("delete throws RuntimeException when product not found")
    void delete_missingProduct_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found: 99");
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("toResponse maps all product fields correctly")
    void toResponse_mapsAllFields() {
        ProductDto.Response r = productService.toResponse(sampleProduct);

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getName()).isEqualTo("Cedar Jacket");
        assertThat(r.getSlug()).isEqualTo("cedar-jacket");
        assertThat(r.getPrice()).isEqualByComparingTo("120.00");
        assertThat(r.getSalePrice()).isEqualByComparingTo("99.00");
        assertThat(r.getSku()).isEqualTo("CJ-001");
        assertThat(r.getGender()).isEqualTo(Product.Gender.UNISEX);
        assertThat(r.isFeatured()).isTrue();
        assertThat(r.isNewArrival()).isTrue();
        assertThat(r.getRating()).isEqualTo(4.5);
        assertThat(r.getReviewCount()).isEqualTo(10);
        assertThat(r.getCategoryName()).isNull();
        assertThat(r.getCollectionName()).isNull();
    }

    @Test
    @DisplayName("toResponse includes category and collection names when present")
    void toResponse_withCategoryAndCollection_mapsNames() {
        Category cat = new Category();
        cat.setName("Outerwear");

        Collection col = new Collection();
        col.setName("Summer 2025");
        col.setSlug("summer-2025");

        sampleProduct.setCategory(cat);
        sampleProduct.setCollection(col);

        ProductDto.Response r = productService.toResponse(sampleProduct);

        assertThat(r.getCategoryName()).isEqualTo("Outerwear");
        assertThat(r.getCollectionName()).isEqualTo("Summer 2025");
        assertThat(r.getCollectionSlug()).isEqualTo("summer-2025");
    }
}
