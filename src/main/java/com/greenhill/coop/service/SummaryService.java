package com.greenhill.coop.service;

import com.greenhill.coop.dto.OrderItemView;
import com.greenhill.coop.dto.OrderView;
import com.greenhill.coop.dto.RoundSummaryLine;
import com.greenhill.coop.model.Order;
import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.PricingType;
import com.greenhill.coop.model.Product;
import com.greenhill.coop.repository.OrderRepository;
import com.greenhill.coop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinator side aggregation: every order of one round rolled up into
 * total purchase quantities per product, ready for the weekly
 * wholesale order and the Thursday packing lists.
 */
@Service
@Transactional(readOnly = true)
public class SummaryService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public SummaryService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderView> roundOrders(Long roundId) {
        return orderRepository.findByRoundIdOrderByMemberNameAsc(roundId).stream()
                .map(OrderView::from)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Total quantity and estimated cost per product across the whole round. */
    public List<RoundSummaryLine> roundSummary(Long roundId) {
        List<Order> orders = orderRepository.findByRoundIdOrderByMemberNameAsc(roundId);

        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        for (Order order : orders) {
            for (OrderItemView item : OrderView.from(order).getItems()) {
                totals.merge(item.getProductId(), item.getQuantity(), BigDecimal::add);
            }
        }

        List<RoundSummaryLine> lines = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : totals.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product == null) {
                continue;
            }
            lines.add(new RoundSummaryLine(product.getId(), product.getName(),
                    product.getPricingType(), product.getPrice(), entry.getValue()));
        }
        lines.sort(Comparator.comparing(RoundSummaryLine::getProductName));
        return lines;
    }

    /** Packing helper: quantity display, e.g. "2 units" vs "1.500 kg". */
    public static String quantityLabel(PricingType type, BigDecimal quantity) {
        if (type == PricingType.WEIGHT) {
            return quantity.stripTrailingZeros().toPlainString() + " kg";
        }
        return quantity.stripTrailingZeros().toPlainString() + " units";
    }
}
