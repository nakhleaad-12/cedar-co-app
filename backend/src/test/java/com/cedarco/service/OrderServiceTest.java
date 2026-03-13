package com.cedarco.service;

import com.cedarco.dto.OrderDto;
import com.cedarco.entity.Cart;
import com.cedarco.entity.CartItem;
import com.cedarco.entity.Coupon;
import com.cedarco.entity.Order;
import com.cedarco.entity.OrderItem;
import com.cedarco.entity.Product;
import com.cedarco.entity.User;
import com.cedarco.repository.CartRepository;
import com.cedarco.repository.CouponRepository;
import com.cedarco.repository.OrderRepository;
import com.cedarco.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponRepository couponRepository;

    @InjectMocks private OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").role(User.Role.CUSTOMER).build();

        product = Product.builder()
                .id(10L).name("Cedar Jacket").slug("cedar-jacket")
                .price(new BigDecimal("100.00"))
                .build();

        cart = Cart.builder().id(100L).user(user).items(new ArrayList<>()).build();

        cartItem = CartItem.builder()
                .id(1L).cart(cart).product(product)
                .size("M").color("Black").quantity(2).build();
        cart.getItems().add(cartItem);
    }

    // ─── placeOrder ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder creates order with correct subtotal and empty discount")
    void placeOrder_noCoupon_correctTotals() {
        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setShippingStreet("Hamra St");
        req.setShippingCity("Beirut");
        req.setShippingRegion("Beirut");
        req.setShippingCountry("Lebanon");
        req.setPaymentMethod("CASH");

        Order savedOrder = buildSavedOrder(new BigDecimal("200.00"), BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderDto.Response result = orderService.placeOrder(1L, req);

        assertThat(result.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(result.getDiscount()).isEqualByComparingTo("0.00");
        assertThat(result.getTotal()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("placeOrder applies percent coupon discount correctly")
    void placeOrder_withPercentCoupon_appliesDiscount() {
        Coupon coupon = Coupon.builder()
                .id(1L).code("SAVE10").active(true)
                .discountType(Coupon.DiscountType.PERCENT)
                .discountValue(new BigDecimal("10"))
                .minOrderAmount(BigDecimal.ZERO)
                .usedCount(0)
                .build();

        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setShippingStreet("Hamra"); req.setShippingCity("Beirut");
        req.setShippingRegion("Beirut"); req.setShippingCountry("Lebanon");
        req.setPaymentMethod("CASH");
        req.setCouponCode("SAVE10");

        // subtotal = 100 * 2 = 200, 10% off = 20 discount, total = 180
        Order savedOrder = buildSavedOrder(new BigDecimal("200.00"), new BigDecimal("20.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderDto.Response result = orderService.placeOrder(1L, req);

        assertThat(result.getDiscount()).isEqualByComparingTo("20.00");
        assertThat(result.getTotal()).isEqualByComparingTo("180.00");
        verify(couponRepository).save(coupon); // usedCount incremented
    }

    @Test
    @DisplayName("placeOrder applies fixed coupon discount correctly")
    void placeOrder_withFixedCoupon_appliesDiscount() {
        Coupon coupon = Coupon.builder()
                .id(2L).code("FLAT15").active(true)
                .discountType(Coupon.DiscountType.FIXED)
                .discountValue(new BigDecimal("15.00"))
                .minOrderAmount(BigDecimal.ZERO)
                .usedCount(0)
                .build();

        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setShippingStreet("Hamra"); req.setShippingCity("Beirut");
        req.setShippingRegion("Beirut"); req.setShippingCountry("Lebanon");
        req.setPaymentMethod("CARD");
        req.setCouponCode("FLAT15");

        Order savedOrder = buildSavedOrder(new BigDecimal("200.00"), new BigDecimal("15.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCodeAndActiveTrue("FLAT15")).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderDto.Response result = orderService.placeOrder(1L, req);

        assertThat(result.getDiscount()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("placeOrder ignores coupon when minimum order amount is not met")
    void placeOrder_couponMinAmountNotMet_noDiscount() {
        Coupon coupon = Coupon.builder()
                .id(3L).code("BIG50").active(true)
                .discountType(Coupon.DiscountType.PERCENT)
                .discountValue(new BigDecimal("50"))
                .minOrderAmount(new BigDecimal("500.00")) // subtotal is 200, won't qualify
                .usedCount(0)
                .build();

        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setShippingStreet("Hamra"); req.setShippingCity("Beirut");
        req.setShippingRegion("Beirut"); req.setShippingCountry("Lebanon");
        req.setPaymentMethod("CASH");
        req.setCouponCode("BIG50");

        Order savedOrder = buildSavedOrder(new BigDecimal("200.00"), BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCodeAndActiveTrue("BIG50")).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderDto.Response result = orderService.placeOrder(1L, req);

        assertThat(result.getDiscount()).isEqualByComparingTo("0.00");
        verify(couponRepository, never()).save(any()); // should not increment count
    }

    @Test
    @DisplayName("placeOrder clears the cart after placing order")
    void placeOrder_success_clearsCart() {
        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setShippingStreet("Hamra"); req.setShippingCity("Beirut");
        req.setShippingRegion("Beirut"); req.setShippingCountry("Lebanon");
        req.setPaymentMethod("CASH");

        Order savedOrder = buildSavedOrder(new BigDecimal("200.00"), BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        orderService.placeOrder(1L, req);

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("placeOrder throws when cart is empty")
    void placeOrder_emptyCart_throwsException() {
        cart.getItems().clear();

        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setShippingStreet("Hamra"); req.setShippingCity("Beirut");
        req.setShippingRegion("Beirut"); req.setShippingCountry("Lebanon");
        req.setPaymentMethod("CASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot place order with empty cart");
    }

    @Test
    @DisplayName("placeOrder throws when cart not found")
    void placeOrder_noCart_throwsException() {
        OrderDto.CreateRequest req = new OrderDto.CreateRequest();
        req.setPaymentMethod("CASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("placeOrder throws when user not found")
    void placeOrder_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(99L, new OrderDto.CreateRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── getUserOrders ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserOrders returns list of orders for user")
    void getUserOrders_returnsOrders() {
        Order order = buildSavedOrder(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));

        List<OrderDto.Response> result = orderService.getUserOrders(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getUserOrders returns empty list when user has no orders")
    void getUserOrders_noOrders_returnsEmpty() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        assertThat(orderService.getUserOrders(1L)).isEmpty();
    }

    // ─── getOrderById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById returns DTO when order exists")
    void getOrderById_existingOrder_returnsDto() {
        Order order = buildSavedOrder(new BigDecimal("100.00"), BigDecimal.ZERO);
        order.setId(55L);
        when(orderRepository.findById(55L)).thenReturn(Optional.of(order));

        OrderDto.Response result = orderService.getOrderById(55L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getOrderById throws when order not found")
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found: 999");
    }

    // ─── getAllOrders ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders returns paginated order list")
    void getAllOrders_returnsPaginatedOrders() {
        Order order = buildSavedOrder(new BigDecimal("200.00"), BigDecimal.ZERO);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Page<OrderDto.Response> result = orderService.getAllOrders(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus changes order status to PROCESSING")
    void updateStatus_validStatus_updatesOrder() {
        Order order = buildSavedOrder(new BigDecimal("100.00"), BigDecimal.ZERO);
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateStatus(1L, "PROCESSING");

        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PROCESSING);
    }

    @Test
    @DisplayName("updateStatus throws when order not found")
    void updateStatus_orderNotFound_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(999L, "SHIPPED"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("updateStatus throws IllegalArgumentException for invalid status")
    void updateStatus_invalidStatus_throwsException() {
        Order order = buildSavedOrder(new BigDecimal("100.00"), BigDecimal.ZERO);
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Order buildSavedOrder(BigDecimal subtotal, BigDecimal discount) {
        Order o = new Order();
        o.setUser(user);
        o.setSubtotal(subtotal);
        o.setDiscount(discount);
        o.setTotal(subtotal.subtract(discount));
        o.setStatus(Order.OrderStatus.PENDING);
        o.setPaymentStatus(Order.PaymentStatus.PENDING);
        o.setPaymentMethod("CASH");
        o.setShippingCity("Beirut");
        o.setShippingCountry("Lebanon");
        o.setItems(new ArrayList<>());
        return o;
    }
}
