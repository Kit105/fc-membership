package com.firstclub.fc_membership.controller;

import com.firstclub.fc_membership.dto.request.PlaceOrderRequest;
import com.firstclub.fc_membership.dto.response.ApiResponse;
import com.firstclub.fc_membership.dto.response.OrderResponse;
import com.firstclub.fc_membership.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Place orders and view history — order placement auto-evaluates tier")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @Operation(summary = "Place an order — automatically triggers tier evaluation")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.placeOrder(request), "Order placed successfully"));
    }

    @GetMapping("/users/{userId}/orders")
    @Operation(summary = "Get all orders for a user, newest first")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getUserOrders(userId)));
    }
}