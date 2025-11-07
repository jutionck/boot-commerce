package com.github.jutionck.service;

import com.github.jutionck.dto.response.CustomerResponse;
import com.github.jutionck.entity.User;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.exceptions.UnauthorizedException;
import com.github.jutionck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    private final UserRepository userRepository;

    public Page<CustomerResponse> getSellerCustomers(User seller, Pageable pageable) {
        if (seller.getRole() != UserRole.SELLER && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only sellers can access customer data");
        }

        return userRepository.findCustomersBySellerId(seller.getId(), pageable)
                .map(this::mapToResponse);
    }

    private CustomerResponse mapToResponse(User user) {
        return CustomerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
