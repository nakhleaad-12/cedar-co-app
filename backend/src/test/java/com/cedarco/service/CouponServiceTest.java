package com.cedarco.service;

import com.cedarco.dto.CouponDto;
import com.cedarco.entity.Coupon;
import com.cedarco.repository.CouponRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;

    @InjectMocks private CouponService couponService;

    private Coupon percentCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        percentCoupon = Coupon.builder()
                .id(1L).code("SAVE20").active(true)
                .discountType(Coupon.DiscountType.PERCENT)
                .discountValue(new BigDecimal("20"))
                .minOrderAmount(BigDecimal.ZERO)
                .usageLimit(100).usedCount(5)
                .expiryDate(null)
                .build();

        fixedCoupon = Coupon.builder()
                .id(2L).code("FLAT30").active(true)
                .discountType(Coupon.DiscountType.FIXED)
                .discountValue(new BigDecimal("30.00"))
                .minOrderAmount(BigDecimal.ZERO)
                .usageLimit(0).usedCount(0)
                .expiryDate(null)
                .build();
    }

    @Test
    @DisplayName("validate returns invalid when coupon code does not exist")
    void validate_nonExistentCode_returnsInvalid() {
        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("FAKECODE");
        req.setOrderAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndActiveTrue("FAKECODE")).thenReturn(Optional.empty());

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Invalid or expired");
    }

    @Test
    @DisplayName("validate returns invalid when coupon is expired")
    void validate_expiredCoupon_returnsInvalid() {
        percentCoupon.setExpiryDate(LocalDateTime.now().minusDays(1));

        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("SAVE20");
        req.setOrderAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndActiveTrue("SAVE20")).thenReturn(Optional.of(percentCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("expired");
    }

    @Test
    @DisplayName("validate returns invalid when usage limit is reached")
    void validate_usageLimitReached_returnsInvalid() {
        percentCoupon.setUsageLimit(5);
        percentCoupon.setUsedCount(5); // fully used up

        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("SAVE20");
        req.setOrderAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndActiveTrue("SAVE20")).thenReturn(Optional.of(percentCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("limit");
    }

    @Test
    @DisplayName("validate returns invalid when order amount is below minimum")
    void validate_orderBelowMinimum_returnsInvalid() {
        percentCoupon.setMinOrderAmount(new BigDecimal("200.00"));

        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("SAVE20");
        req.setOrderAmount(new BigDecimal("50.00"));

        when(couponRepository.findByCodeAndActiveTrue("SAVE20")).thenReturn(Optional.of(percentCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("minimum");
    }

    @Test
    @DisplayName("validate applies percent discount correctly")
    void validate_percentCoupon_computesCorrectDiscount() {
        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("SAVE20");
        req.setOrderAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndActiveTrue("SAVE20")).thenReturn(Optional.of(percentCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("20.00"); // 20% of 100
        assertThat(result.getDiscountType()).isEqualTo("PERCENT");
    }

    @Test
    @DisplayName("validate applies fixed discount correctly")
    void validate_fixedCoupon_returnsFixedDiscountAmount() {
        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("FLAT30");
        req.setOrderAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndActiveTrue("FLAT30")).thenReturn(Optional.of(fixedCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getDiscountType()).isEqualTo("FIXED");
    }

    @Test
    @DisplayName("validate returns valid when coupon has no expiry and unlimited uses")
    void validate_noExpiryNoMaxUses_returnsValid() {
        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("FLAT30");
        req.setOrderAmount(new BigDecimal("50.00"));

        when(couponRepository.findByCodeAndActiveTrue("FLAT30")).thenReturn(Optional.of(fixedCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("validate is valid when coupon is not yet expired")
    void validate_notYetExpired_returnsValid() {
        percentCoupon.setExpiryDate(LocalDateTime.now().plusDays(7));

        CouponDto.ValidateRequest req = new CouponDto.ValidateRequest();
        req.setCode("SAVE20");
        req.setOrderAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndActiveTrue("SAVE20")).thenReturn(Optional.of(percentCoupon));

        CouponDto.ValidateResponse result = couponService.validate(req);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("getAll returns all coupons from repository")
    void getAll_returnsCoupons() {
        when(couponRepository.findAll()).thenReturn(List.of(percentCoupon, fixedCoupon));

        List<Coupon> result = couponService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("create saves coupon with uppercased code")
    void create_validRequest_savesWithUppercaseCode() {
        CouponDto.CreateRequest req = new CouponDto.CreateRequest();
        req.setCode("summer10");
        req.setDiscountType("PERCENT");
        req.setDiscountValue(new BigDecimal("10"));
        req.setUsageLimit(100);

        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Coupon result = couponService.create(req);

        assertThat(result.getCode()).isEqualTo("SUMMER10");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUsedCount()).isEqualTo(0);
        assertThat(result.getUsageLimit()).isEqualTo(100);
    }
}
