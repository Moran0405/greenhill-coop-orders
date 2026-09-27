package com.greenhill.coop.service;

import com.greenhill.coop.dto.OrderRequest;
import com.greenhill.coop.model.*;
import com.greenhill.coop.repository.OrderRepository;
import com.greenhill.coop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Core ordering flow. A member keeps a single editable order per round;
 * while the round is OPEN they can create, change or cancel it, and once
 * the round is CLOSED the order is read only.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RoundService roundService;
    private final MemberService memberService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        RoundService roundService,
                        MemberService memberService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.roundService = roundService;
        this.memberService = memberService;
    }

    @Transactional(readOnly = true)
    public Order getOwnOrder(Long memberId, Long roundId) {
        return orderRepository.findByMemberIdAndRoundId(memberId, roundId)
                .orElseThrow(() -> new IllegalArgumentException("No order placed for this round yet."));
    }

    @Transactional(readOnly = true)
    public boolean hasOrder(Long memberId, Long roundId) {
        return orderRepository.findByMemberIdAndRoundId(memberId, roundId).isPresent();
    }

    /** Create or replace the member's order for the given round. */
    public Order saveOwnOrder(Long memberId, Long roundId, OrderRequest request) {
        OrderRound round = roundService.get(roundId);
        if (!round.isOrderingOpen()) {
            throw new IllegalStateException("This round is closed - orders can no longer be changed.");
        }
        Member member = memberService.get(memberId);

        Map<Long, BigDecimal> quantities = parseAndValidateItems(request);

        Order order = orderRepository.findByMemberIdAndRoundId(memberId, roundId)
                .orElseGet(() -> {
                    Order o = new Order();
                    o.setMember(member);
                    o.setRound(round);
                    return o;
                });

        order.clearItems();
        for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + entry.getKey()));
            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            order.addItem(new OrderItem(product, entry.getValue()));
        }
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /** Cancel the member's own order, allowed only while the round is open. */
    public void cancelOwnOrder(Long memberId, Long roundId) {
        OrderRound round = roundService.get(roundId);
        if (!round.isOrderingOpen()) {
            throw new IllegalStateException("This round is closed - orders can no longer be cancelled.");
        }
        Order order = orderRepository.findByMemberIdAndRoundId(memberId, roundId)
                .orElseThrow(() -> new IllegalArgumentException("No order placed for this round yet."));
        orderRepository.delete(order);
    }

    /**
     * Validates every requested line against the product pricing rules:
     * UNIT products take whole numbers only, WEIGHT products allow decimals.
     */
    private Map<Long, BigDecimal> parseAndValidateItems(OrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item.");
        }
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (OrderRequest.OrderLine line : request.getItems()) {
            if (line.getProductId() == null) {
                throw new IllegalArgumentException("Every order line needs a product.");
            }
            BigDecimal quantity;
            try {
                quantity = new BigDecimal(line.getQuantity());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Quantity must be a number: " + line.getQuantity());
            }
            if (quantity.signum() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.getProductId()));
            if (product.getPricingType() == PricingType.UNIT
                    && quantity.stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException(
                        product.getName() + " is sold as whole units only, use a whole number.");
            }
            // Merge duplicate lines for the same product.
            result.merge(product.getId(), quantity, BigDecimal::add);
        }
        return result;
    }
}
