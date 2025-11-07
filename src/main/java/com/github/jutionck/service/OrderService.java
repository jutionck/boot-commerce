package com.github.jutionck.service;

import com.github.jutionck.dto.request.CreateOrderRequest;
import com.github.jutionck.dto.request.UpdateOrderStatusRequest;
import com.github.jutionck.dto.response.OrderResponse;
import com.github.jutionck.entity.*;
import com.github.jutionck.enums.OrderStatus;
import com.github.jutionck.enums.PaymentStatus;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.enums.VoucherType;
import com.github.jutionck.exceptions.ResourceNotFoundException;
import com.github.jutionck.exceptions.UnauthorizedException;
import com.github.jutionck.exceptions.ValidationException;
import com.github.jutionck.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final ReferralCodeRepository referralCodeRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User customer) {
        log.info("Creating order for customer: {}", customer.getEmail());

        // Validate and prepare order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Check stock
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new ValidationException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(itemPrice)
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(orderItem);

            // Reduce stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Calculate discount if voucher provided
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

            discount = calculateVoucherDiscount(voucher, subtotal);
        }

        // Calculate shipping, tax, and total
        BigDecimal shipping = calculateShipping(subtotal, discount);
        BigDecimal tax = calculateTax(subtotal.subtract(discount));
        BigDecimal total = subtotal.subtract(discount).add(shipping).add(tax);

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .discount(discount)
                .shipping(shipping)
                .tax(tax)
                .total(total)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .voucherCode(request.getVoucherCode())
                .referralCode(request.getReferralCode())
                .notes(request.getNotes())
                .build();

        order = orderRepository.save(order);

        // Save order items
        Order finalOrder = order;
        orderItems.forEach(item -> {
            item.setOrder(finalOrder);
            orderItemRepository.save(item);
        });

        // Update voucher usage if used
        if (request.getVoucherCode() != null) {
            voucherRepository.findByCode(request.getVoucherCode()).ifPresent(voucher -> {
                voucher.setUsageCount(voucher.getUsageCount() + 1);
                voucherRepository.save(voucher);
            });
        }

        // Update referral code if used
        if (request.getReferralCode() != null) {
            referralCodeRepository.findByCode(request.getReferralCode()).ifPresent(referralCode -> {
                referralCode.setUsageCount(referralCode.getUsageCount() + 1);
                referralCode.setTotalEarnings(
                        referralCode.getTotalEarnings().add(referralCode.getRewardAmount())
                );
                referralCodeRepository.save(referralCode);
            });
        }

        order.setItems(orderItems);
        log.info("Order created successfully: {}", order.getOrderNumber());
        return mapToResponse(order);
    }

    public OrderResponse getOrderById(UUID orderId, User user) {
        Order order = findOrderById(orderId);

        // Check permissions
        if (!order.getCustomer().getId().equals(user.getId()) &&
                user.getRole() != UserRole.ADMIN &&
                !orderContainsSellerProduct(orderId, user.getId())) {
            throw new UnauthorizedException("You don't have permission to view this order");
        }

        return mapToResponse(order);
    }

    public Page<OrderResponse> getCustomerOrders(User customer, Pageable pageable) {
        return orderRepository.findByCustomerId(customer.getId(), pageable)
                .map(this::mapToResponse);
    }

    public Page<OrderResponse> getSellerOrders(User seller, Pageable pageable) {
        return orderRepository.findOrdersBySellerId(seller.getId(), pageable)
                .map(this::mapToResponse);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, User user) {
        Order order = findOrderById(orderId);

        // Only seller or admin can update order status
        if (user.getRole() != UserRole.ADMIN && !orderContainsSellerProduct(orderId, user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this order");
        }

        order.setStatus(request.getStatus());

        if (request.getStatus() == OrderStatus.CANCELLED) {
            order.setCancelReason(request.getCancelReason());
            order.setCancelledAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.REFUNDED);

            // Restore stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        if (request.getStatus() == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order = orderRepository.save(order);
        log.info("Order status updated: {} to {}", order.getOrderNumber(), request.getStatus());
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String reason, User user) {
        Order order = findOrderById(orderId);

        // Customer can only cancel their own orders
        if (!order.getCustomer().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You don't have permission to cancel this order");
        }

        // Can only cancel if PENDING or PROCESSING
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new ValidationException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        order.setPaymentStatus(PaymentStatus.REFUNDED);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order = orderRepository.save(order);
        log.info("Order cancelled: {}", order.getOrderNumber());
        return mapToResponse(order);
    }

    // Helper methods
    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private boolean orderContainsSellerProduct(UUID orderId, UUID sellerId) {
        return orderRepository.orderContainsSellerProduct(orderId, sellerId);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal subtotal) {
        // Validate voucher
        if (!voucher.getIsActive()) {
            throw new ValidationException("Voucher is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new ValidationException("Voucher is expired or not yet valid");
        }

        if (voucher.getMinPurchase() != null && subtotal.compareTo(voucher.getMinPurchase()) < 0) {
            throw new ValidationException("Minimum purchase amount not met for this voucher");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            throw new ValidationException("Voucher usage limit reached");
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getType() == VoucherType.PERCENTAGE) {
            discount = subtotal.multiply(voucher.getValue()).divide(BigDecimal.valueOf(100));
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else if (voucher.getType() == VoucherType.FIXED_AMOUNT) {
            discount = voucher.getValue();
        }

        return discount;
    }

    private BigDecimal calculateShipping(BigDecimal subtotal, BigDecimal discount) {
        // Free shipping for orders over $100 after discount
        BigDecimal afterDiscount = subtotal.subtract(discount);
        if (afterDiscount.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(10.00);
    }

    private BigDecimal calculateTax(BigDecimal amount) {
        // 10% tax
        return amount.multiply(BigDecimal.valueOf(0.10));
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse.CustomerInfo customerInfo = OrderResponse.CustomerInfo.builder()
                .id(order.getCustomer().getId())
                .firstName(order.getCustomer().getFirstName())
                .lastName(order.getCustomer().getLastName())
                .email(order.getCustomer().getEmail())
                .build();

        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> {
                    OrderResponse.ProductInfo productInfo = OrderResponse.ProductInfo.builder()
                            .id(item.getProduct().getId())
                            .name(item.getProduct().getName())
                            .category(item.getProduct().getCategory())
                            .brand(item.getProduct().getBrand())
                            .build();

                    return OrderResponse.OrderItemResponse.builder()
                            .id(item.getId())
                            .product(productInfo)
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(item.getSubtotal())
                            .build();
                })
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customer(customerInfo)
                .status(order.getStatus())
                .items(items)
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .shipping(order.getShipping())
                .tax(order.getTax())
                .total(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .voucherCode(order.getVoucherCode())
                .referralCode(order.getReferralCode())
                .notes(order.getNotes())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
