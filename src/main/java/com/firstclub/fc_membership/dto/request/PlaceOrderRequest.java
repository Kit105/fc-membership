package com.firstclub.fc_membership.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Order amount must be greater than zero")
    private BigDecimal totalAmount;

    private String description;
}