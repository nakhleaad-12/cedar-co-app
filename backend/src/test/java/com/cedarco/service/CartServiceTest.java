package com.cedarco.service;

import com.cedarco.dto.CartDto;
import com.cedarco.entity.*;
import com.cedarco.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").role(User.Role.CUSTOMER).build();

        product = Product.builder()
                .id(10L)
                .name("Cedar Jacket")
                .slug("cedar-jacket")
                .price(new BigDecimal("120.00"))
                .build();
        product.setImages(new ArrayList<>(List.of(ProductImage.builder().url("img.jpg").product(product).build())));

        cart = Cart.builder().id(100L).user(user).items(new ArrayList<>()).build();
    }

    // ─── getCart ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCart returns existing cart for user")
    void getCart_existingCart_returnsDto() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartDto.Response result = cartService.getCart(1L);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getCart creates a new cart when user has none")
    void getCart_noExistingCart_createsNewCart() {
        Cart newCart = Cart.builder().id(200L).user(user).items(new ArrayList<>()).build();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartDto.Response result = cartService.getCart(1L);

        assertThat(result.getId()).isEqualTo(200L);
        verify(userRepository).findById(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("getCart throws when user does not exist during cart creation")
    void getCart_userNotFound_throwsException() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── addItem ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addItem adds new item to cart when product/size/color combo is new")
    void addItem_newItem_addsToCart() {
        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(10L);
        req.setSize("M");
        req.setColor("Black");
        req.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto.Response result = cartService.addItem(1L, req);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("addItem accumulates quantity for existing item with same product/size/color")
    void addItem_existingItemSameCombo_accumulatesQuantity() {
        // Existing item already in cart
        CartItem existing = CartItem.builder()
                .id(1L).cart(cart).product(product)
                .size("M").color("Black").quantity(2).build();
        cart.getItems().add(existing);

        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(10L);
        req.setSize("M");
        req.setColor("Black");
        req.setQuantity(3);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItem(1L, req);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
    }

    @Test
    @DisplayName("addItem defaults quantity to 1 when not specified")
    void addItem_noQuantitySpecified_defaultsToOne() {
        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(10L);
        // quantity is null

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItem(1L, req);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("addItem throws when product is not found")
    void addItem_productNotFound_throwsException() {
        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(999L);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ─── updateItem ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateItem updates quantity when quantity > 0")
    void updateItem_positiveQuantity_updatesQuantity() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .size("M").quantity(2).build();
        cart.getItems().add(item);

        CartDto.UpdateItemRequest req = new CartDto.UpdateItemRequest();
        req.setQuantity(5);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateItem(1L, 1L, req);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("updateItem removes item when quantity is 0")
    void updateItem_zeroQuantity_removesItem() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .size("M").quantity(2).build();
        cart.getItems().add(item);

        CartDto.UpdateItemRequest req = new CartDto.UpdateItemRequest();
        req.setQuantity(0);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateItem(1L, 1L, req);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("updateItem removes item when quantity is negative")
    void updateItem_negativeQuantity_removesItem() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .size("M").quantity(3).build();
        cart.getItems().add(item);

        CartDto.UpdateItemRequest req = new CartDto.UpdateItemRequest();
        req.setQuantity(-1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateItem(1L, 1L, req);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("updateItem throws when item not in cart")
    void updateItem_itemNotInCart_throwsException() {
        CartDto.UpdateItemRequest req = new CartDto.UpdateItemRequest();
        req.setQuantity(3);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItem(1L, 999L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found in cart");
    }

    @Test
    @DisplayName("updateItem throws when cart not found")
    void updateItem_cartNotFound_throwsException() {
        CartDto.UpdateItemRequest req = new CartDto.UpdateItemRequest();
        req.setQuantity(1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItem(1L, 1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
    }

    // ─── removeItem ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeItem removes specific item from cart")
    void removeItem_existingItem_removesIt() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItem(1L, 1L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("removeItem does nothing when item id does not match")
    void removeItem_nonExistentItemId_noChange() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItem(1L, 999L);

        assertThat(cart.getItems()).hasSize(1);
    }

    // ─── clearCart ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart clears all items from the cart")
    void clearCart_withItems_clearsAll() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(1L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("clearCart is a no-op when user has no cart")
    void clearCart_noCart_doesNothing() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatCode(() -> cartService.clearCart(1L)).doesNotThrowAnyException();
        verify(cartRepository, never()).save(any());
    }

    // ─── Cart total calculation ───────────────────────────────────────────────

    @Test
    @DisplayName("getCart total uses salePrice when available")
    void getCart_salePriceUsed_totalReflectsSalePrice() {
        product.setSalePrice(new BigDecimal("80.00"));
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .size("M").quantity(2).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartDto.Response result = cartService.getCart(1L);

        // 80.00 * 2 = 160.00
        assertThat(result.getTotal()).isEqualByComparingTo("160.00");
    }

    @Test
    @DisplayName("getCart total uses regular price when no salePrice")
    void getCart_noSalePrice_totalUsesRegularPrice() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .size("L").quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartDto.Response result = cartService.getCart(1L);

        assertThat(result.getTotal()).isEqualByComparingTo("120.00");
    }
}
