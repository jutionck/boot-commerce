package com.github.jutionck.service;

import com.github.jutionck.dto.request.VoucherRequest;
import com.github.jutionck.dto.response.VoucherResponse;
import com.github.jutionck.entity.User;
import com.github.jutionck.entity.Voucher;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.exceptions.ResourceDuplicateException;
import com.github.jutionck.exceptions.ResourceNotFoundException;
import com.github.jutionck.exceptions.UnauthorizedException;
import com.github.jutionck.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherService {
    private final VoucherRepository voucherRepository;

    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request, User seller) {
        log.info("Creating voucher: {} by seller: {}", request.getCode(), seller.getEmail());

        if (seller.getRole() != UserRole.SELLER && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only sellers can create vouchers");
        }

        if (voucherRepository.existsByCode(request.getCode())) {
            throw new ResourceDuplicateException("Voucher code already exists");
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .value(request.getValue())
                .minPurchase(request.getMinPurchase())
                .maxDiscount(request.getMaxDiscount())
                .usageLimit(request.getUsageLimit())
                .userUsageLimit(request.getUserUsageLimit())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .seller(seller)
                .build();

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    public VoucherResponse getVoucherById(UUID voucherId) {
        Voucher voucher = findVoucherById(voucherId);
        return mapToResponse(voucher);
    }

    public VoucherResponse validateVoucherCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        LocalDateTime now = LocalDateTime.now();
        if (!voucher.getIsActive() ||
            now.isBefore(voucher.getStartDate()) ||
            now.isAfter(voucher.getEndDate())) {
            throw new ResourceNotFoundException("Voucher is not valid");
        }

        return mapToResponse(voucher);
    }

    public Page<VoucherResponse> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<VoucherResponse> getActiveVouchers(Pageable pageable) {
        return voucherRepository.findActiveVouchers(LocalDateTime.now(), pageable)
                .map(this::mapToResponse);
    }

    public Page<VoucherResponse> getSellerVouchers(User seller, Pageable pageable) {
        return voucherRepository.findBySellerId(seller.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public VoucherResponse updateVoucher(UUID voucherId, VoucherRequest request, User seller) {
        Voucher voucher = findVoucherById(voucherId);

        if (!voucher.getSeller().getId().equals(seller.getId()) && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You don't have permission to update this voucher");
        }

        if (!voucher.getCode().equals(request.getCode()) &&
            voucherRepository.existsByCodeAndIdNot(request.getCode(), voucherId)) {
            throw new ResourceDuplicateException("Voucher code already exists");
        }

        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setType(request.getType());
        voucher.setValue(request.getValue());
        voucher.setMinPurchase(request.getMinPurchase());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUserUsageLimit(request.getUserUsageLimit());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setIsActive(request.getIsActive());

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Transactional
    public void deleteVoucher(UUID voucherId, User seller) {
        Voucher voucher = findVoucherById(voucherId);

        if (!voucher.getSeller().getId().equals(seller.getId()) && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You don't have permission to delete this voucher");
        }

        voucherRepository.delete(voucher);
        log.info("Voucher deleted: {}", voucher.getCode());
    }

    private Voucher findVoucherById(UUID voucherId) {
        return voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + voucherId));
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        VoucherResponse.SellerInfo sellerInfo = VoucherResponse.SellerInfo.builder()
                .id(voucher.getSeller().getId())
                .firstName(voucher.getSeller().getFirstName())
                .lastName(voucher.getSeller().getLastName())
                .email(voucher.getSeller().getEmail())
                .build();

        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .type(voucher.getType())
                .value(voucher.getValue())
                .minPurchase(voucher.getMinPurchase())
                .maxDiscount(voucher.getMaxDiscount())
                .usageLimit(voucher.getUsageLimit())
                .usageCount(voucher.getUsageCount())
                .userUsageLimit(voucher.getUserUsageLimit())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .isActive(voucher.getIsActive())
                .seller(sellerInfo)
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }
}
