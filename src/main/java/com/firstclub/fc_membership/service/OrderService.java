package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.PlaceOrderRequest;
import com.firstclub.fc_membership.dto.response.OrderResponse;
import com.firstclub.fc_membership.entity.Order;
import com.firstclub.fc_membership.entity.User;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.exception.ResourceNotFoundException;
import com.firstclub.fc_membership.repository.OrderRepository;
import com.firstclub.fc_membership.repository.UserMembershipRepository;
import com.firstclub.fc_membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TierEvaluationService tierEvaluationService;
    private final BenefitApplicationService benefitApplicationService;
    private final UserMembershipRepository membershipRepository;


    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Order order = Order.builder()
                .user(user)
                .totalAmount(request.getTotalAmount())
                .description(request.getDescription() != null
                        ? request.getDescription() : "Order placed")
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order {} placed: user={}, amount={}",
                saved.getId(), user.getId(), saved.getTotalAmount());

        // Apply tier benefits
        BenefitApplicationService.AppliedBenefits applied =
                benefitApplicationService.applyBenefits(user.getId(), request.getTotalAmount());

        // Check tier upgrade
        String updatedTier = null;
        if (membershipRepository.existsByUserIdAndStatus(
                user.getId(), MembershipStatus.ACTIVE)) {
            try {
                var membership = tierEvaluationService.evaluateAndUpdateTier(user.getId());
                updatedTier = membership.getTier().getTierType().name();
            } catch (Exception e) {
                log.debug("Tier evaluation failed for user {}: {}", user.getId(), e.getMessage());
            }
        }

        // Build response with benefit breakdown — NOT toResponse()
        return OrderResponse.builder()
                .id(saved.getId())
                .userId(user.getId())
                .userName(user.getName())
                .originalAmount(applied.getOriginalAmount())
                .discountApplied(applied.getDiscountApplied())
                .deliveryCharge(applied.getDeliveryCharge())
                .finalAmount(applied.getFinalAmount())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .appliedBenefits(applied.getAppliedBenefits())
                .updatedTier(updatedTier)
                .build();
    }


    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> toResponse(o, o.getUser(), null))
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order, User user, String updatedTier) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(user.getId())
                .userName(user.getName())
                .originalAmount(order.getTotalAmount())
                .description(order.getDescription())
                .createdAt(order.getCreatedAt())
                .updatedTier(updatedTier)
                .build();
    }
}