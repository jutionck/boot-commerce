package com.github.jutionck.dto.request;

import com.github.jutionck.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String cancelReason;
}
