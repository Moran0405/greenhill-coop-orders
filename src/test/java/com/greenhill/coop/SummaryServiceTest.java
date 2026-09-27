package com.greenhill.coop;

import com.greenhill.coop.dto.RoundSummaryLine;
import com.greenhill.coop.model.*;
import com.greenhill.coop.repository.OrderRepository;
import com.greenhill.coop.repository.ProductRepository;
import com.greenhill.coop.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Coordinator user story: view all orders of a round and aggregate the
 * total purchase quantity per product for the wholesale order.
 */
class SummaryServiceTest {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private SummaryService summaryService;

    private OrderRound round;
    private Member alice;
    private Member bob;
    private Product oats;
    private Product eggs;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        summaryService = new SummaryService(orderRepository, productRepository);

        round = new OrderRound("Week 39", null, null);
        round.setId(1L);
        round.setStatus(RoundStatus.CLOSED);

        alice = new Member("Alice", "alice@example.com", null);
        alice.setId(1L);
        bob = new Member("Bob", "bob@example.com", null);
        bob.setId(2L);

        oats = new Product("Rolled Oats", "Organic", PricingType.WEIGHT, new BigDecimal("4.20"));
        oats.setId(100L);
        eggs = new Product("Free-range Eggs", "Box of 12", PricingType.UNIT, new BigDecimal("7.50"));
        eggs.setId(101L);

        when(productRepository.findById(100L)).thenReturn(java.util.Optional.of(oats));
        when(productRepository.findById(101L)).thenReturn(java.util.Optional.of(eggs));
    }

    @Test
    void totalsAreAggregatedAcrossMembers() {
        Order orderA = order(alice, item(oats, "1.5"), item(eggs, "2"));
        Order orderB = order(bob, item(oats, "0.75"), item(eggs, "1"));
        when(orderRepository.findByRoundIdOrderByMemberNameAsc(1L)).thenReturn(Arrays.asList(orderA, orderB));

        List<RoundSummaryLine> lines = summaryService.roundSummary(1L);

        assertEquals(2, lines.size());

        RoundSummaryLine eggLine = lines.stream()
                .filter(l -> l.getProductName().equals("Free-range Eggs")).findFirst().get();
        assertEquals(new BigDecimal("3"), eggLine.getTotalQuantity());
        assertEquals(0, eggLine.getEstimatedCost().compareTo(new BigDecimal("22.50")));

        RoundSummaryLine oatLine = lines.stream()
                .filter(l -> l.getProductName().equals("Rolled Oats")).findFirst().get();
        assertEquals(0, oatLine.getTotalQuantity().compareTo(new BigDecimal("2.25")));
        assertEquals(0, oatLine.getEstimatedCost().compareTo(new BigDecimal("9.45")));
    }

    @Test
    void roundWithNoOrdersProducesEmptySummary() {
        when(orderRepository.findByRoundIdOrderByMemberNameAsc(1L)).thenReturn(java.util.Collections.emptyList());
        assertTrue(summaryService.roundSummary(1L).isEmpty());
    }

    private Order order(Member member, OrderItem... items) {
        Order order = new Order();
        order.setMember(member);
        order.setRound(round);
        for (OrderItem item : items) {
            order.addItem(item);
        }
        return order;
    }

    private OrderItem item(Product product, String quantity) {
        return new OrderItem(product, new BigDecimal(quantity));
    }
}
