package com.greenhill.coop;

import com.greenhill.coop.model.Member;
import com.greenhill.coop.model.Order;
import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.Product;
import com.greenhill.coop.model.PricingType;
import com.greenhill.coop.model.RoundStatus;
import com.greenhill.coop.repository.OrderRepository;
import com.greenhill.coop.repository.ProductRepository;
import com.greenhill.coop.dto.OrderRequest;
import com.greenhill.coop.service.MemberService;
import com.greenhill.coop.service.OrderService;
import com.greenhill.coop.service.RoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Covers the core member ordering user stories:
 * save an order while the round is open, edit it, cancel it,
 * and the read-only behaviour after the round closes.
 */
class OrderServiceTest {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private RoundService roundService;
    private MemberService memberService;
    private OrderService orderService;

    private OrderRound openRound;
    private OrderRound closedRound;
    private Member member;
    private Product oats;
    private Product eggs;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        roundService = mock(RoundService.class);
        memberService = mock(MemberService.class);
        orderService = new OrderService(orderRepository, productRepository, roundService, memberService);

        openRound = new OrderRound("Week 39", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(3));
        openRound.setId(1L);
        openRound.setStatus(RoundStatus.OPEN);

        closedRound = new OrderRound("Week 38", LocalDateTime.now().minusDays(8), LocalDateTime.now().minusDays(2));
        closedRound.setId(2L);
        closedRound.setStatus(RoundStatus.CLOSED);

        member = new Member("Nguyen household", "nguyen@example.com", "0400 000 000");
        member.setId(10L);

        oats = new Product("Rolled Oats", "Organic", PricingType.WEIGHT, new BigDecimal("4.20"));
        oats.setId(100L);
        eggs = new Product("Free-range Eggs", "Box of 12", PricingType.UNIT, new BigDecimal("7.50"));
        eggs.setId(101L);

        when(productRepository.findById(100L)).thenReturn(Optional.of(oats));
        when(productRepository.findById(101L)).thenReturn(Optional.of(eggs));
        when(roundService.get(1L)).thenReturn(openRound);
        when(roundService.get(2L)).thenReturn(closedRound);
        when(memberService.get(10L)).thenReturn(member);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private OrderRequest.OrderLine line(Long productId, String quantity) {
        OrderRequest.OrderLine l = new OrderRequest.OrderLine();
        l.setProductId(productId);
        l.setQuantity(quantity);
        return l;
    }

    @Test
    void memberCanPlaceOrderWhileRoundIsOpen() {
        OrderRequest request = new OrderRequest();
        request.setItems(Arrays.asList(line(100L, "1.5"), line(101L, "2")));

        Order saved = orderService.saveOwnOrder(10L, 1L, request);

        assertEquals(2, saved.getItems().size());
        assertEquals(new BigDecimal("1.5"), saved.getItems().get(0).getQuantity());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void wholeUnitProductsRejectFractionalQuantities() {
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.singletonList(line(101L, "1.5")));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOwnOrder(10L, 1L, request));
    }

    @Test
    void weightProductsAcceptDecimalQuantities() {
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.singletonList(line(100L, "1.5")));

        Order saved = orderService.saveOwnOrder(10L, 1L, request);
        assertEquals(new BigDecimal("1.5"), saved.getItems().get(0).getQuantity());
    }

    @Test
    void duplicateProductLinesAreMerged() {
        OrderRequest request = new OrderRequest();
        request.setItems(Arrays.asList(line(101L, "1"), line(101L, "2")));

        Order saved = orderService.saveOwnOrder(10L, 1L, request);
        assertEquals(1, saved.getItems().size());
        assertEquals(new BigDecimal("3"), saved.getItems().get(0).getQuantity());
    }

    @Test
    void orderCannotBePlacedAfterRoundCloses() {
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.singletonList(line(101L, "1")));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderService.saveOwnOrder(10L, 2L, request));
        assertTrue(ex.getMessage().contains("closed"));
    }

    @Test
    void existingOrderIsReplacedOnSecondSave() {
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.singletonList(line(101L, "3")));

        Order existing = new Order();
        existing.setId(55L);
        existing.setMember(member);
        existing.setRound(openRound);
        when(orderRepository.findByMemberIdAndRoundId(10L, 1L)).thenReturn(Optional.of(existing));

        Order saved = orderService.saveOwnOrder(10L, 1L, request);

        assertEquals(55L, saved.getId());
        assertEquals(1, saved.getItems().size());
        assertEquals(new BigDecimal("3"), saved.getItems().get(0).getQuantity());
    }

    @Test
    void orderCannotBeCancelledAfterRoundCloses() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderService.cancelOwnOrder(10L, 2L));
        assertTrue(ex.getMessage().contains("closed"));
    }

    @Test
    void memberCanCancelOwnOrderWhileRoundIsOpen() {
        Order existing = new Order();
        existing.setId(55L);
        when(orderRepository.findByMemberIdAndRoundId(10L, 1L)).thenReturn(Optional.of(existing));

        orderService.cancelOwnOrder(10L, 1L);

        verify(orderRepository).delete(existing);
    }

    @Test
    void emptyOrderIsRejected() {
        OrderRequest request = new OrderRequest();
        assertThrows(IllegalArgumentException.class, () -> orderService.saveOwnOrder(10L, 1L, request));
    }

    @Test
    void nonPositiveQuantityIsRejected() {
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.singletonList(line(101L, "0")));
        assertThrows(IllegalArgumentException.class, () -> orderService.saveOwnOrder(10L, 1L, request));
    }
}
