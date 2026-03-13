package com.cedarco.service;

import com.cedarco.dto.CouponDto;
import com.cedarco.entity.Coupon;
import com.cedarco.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponDto.ValidateResponse validate(CouponDto.ValidateRequest req) {
        CouponDto.ValidateResponse res = new CouponDto.ValidateResponse();
        Coupon coupon = couponRepository.findByCodeAndActiveTrue(req.getCode()).orElse(null);

        if (coupon == null) {
            res.setValid(false);
            res.setMessage("Invalid or expired coupon code");
            return res;
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            res.setValid(false);
            res.setMessage("Coupon has expired");
            return res;
        }
        if (coupon.getUsageLimit() > 0 && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            res.setValid(false);
            res.setMessage("Coupon usage limit reached");
            return res;
        }
        if (req.getOrderAmount() != null && req.getOrderAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            res.setValid(false);
            res.setMessage("Minimum order amount not met ($" + coupon.getMinOrderAmount() + ")");
            return res;
        }

        BigDecimal discountAmount = coupon.getDiscountType() == Coupon.DiscountType.PERCENT
                ? req.getOrderAmount().multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100))
                : coupon.getDiscountValue();

        res.setValid(true);
        res.setDiscountType(coupon.getDiscountType().name());
        res.setDiscountValue(coupon.getDiscountValue());
        res.setDiscountAmount(discountAmount);
        res.setMessage("Coupon applied! Saving $" + discountAmount);
        return res;
    }

    public List<Coupon> getAll() {
        return couponRepository.findAll();
    }

    public Coupon create(CouponDto.CreateRequest req) {
        Coupon coupon = Coupon.builder()
                .code(req.getCode().toUpperCase())
                .discountType(Coupon.DiscountType.valueOf(req.getDiscountType()))
                .discountValue(req.getDiscountValue())
                .minOrderAmount(req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO)
                .usageLimit(req.getUsageLimit() != null ? req.getUsageLimit() : 0)
                .usedCount(0)
                .active(true)
                .expiryDate(req.getExpiryDate() != null ? LocalDateTime.parse(req.getExpiryDate()) : null)
                .build();
        return couponRepository.save(coupon);
    }
}
