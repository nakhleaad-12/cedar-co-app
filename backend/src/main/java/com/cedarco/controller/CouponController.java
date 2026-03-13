package com.cedarco.controller;

import com.cedarco.dto.CouponDto;
import com.cedarco.entity.Coupon;
import com.cedarco.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<CouponDto.ValidateResponse> validate(@RequestBody CouponDto.ValidateRequest req) {
        return ResponseEntity.ok(couponService.validate(req));
    }
}
