package com.github.jutionck.controller;

import com.github.jutionck.entity.User;
import com.github.jutionck.service.ReferralService;
import com.github.jutionck.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
@Tag(name = "Referrals", description = "Referral code management endpoints")
public class ReferralController {
    private final ReferralService referralService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateReferralCode(@AuthenticationPrincipal User user) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                "Referral code generated successfully",
                referralService.generateReferralCode(user)
        );
    }

    @GetMapping("/my-code")
    public ResponseEntity<?> getMyReferralCode(@AuthenticationPrincipal User user) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Referral code retrieved successfully",
                referralService.getReferralCode(user)
        );
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateReferralCode(@PathVariable String code) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Referral code is valid",
                referralService.validateReferralCode(code)
        );
    }
}
