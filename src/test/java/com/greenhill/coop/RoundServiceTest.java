package com.greenhill.coop;

import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.RoundStatus;
import com.greenhill.coop.repository.OrderRepository;
import com.greenhill.coop.repository.OrderRoundRepository;
import com.greenhill.coop.service.RoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Round lifecycle: open -> closed -> packed. */
class RoundServiceTest {

    private OrderRoundRepository roundRepository;
    private OrderRepository orderRepository;
    private RoundService roundService;

    @BeforeEach
    void setUp() {
        roundRepository = mock(OrderRoundRepository.class);
        orderRepository = mock(OrderRepository.class);
        roundService = new RoundService(roundRepository, orderRepository);
    }

    @Test
    void createRoundWithValidWindow() {
        when(roundRepository.save(any(OrderRound.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderRound round = roundService.create("Week 40",
                LocalDateTime.of(2026, 9, 21, 8, 0),
                LocalDateTime.of(2026, 9, 27, 20, 0));

        assertEquals(RoundStatus.OPEN, round.getStatus());
        assertTrue(round.isOrderingOpen());
    }

    @Test
    void createRoundRejectsCloseBeforeOpen() {
        assertThrows(IllegalArgumentException.class, () -> roundService.create("Bad round",
                LocalDateTime.of(2026, 9, 27, 20, 0),
                LocalDateTime.of(2026, 9, 21, 8, 0)));
    }

    @Test
    void closeRoundStopsOrdering() {
        OrderRound round = new OrderRound("Week 39",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        round.setId(1L);
        round.setStatus(RoundStatus.OPEN);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.save(any(OrderRound.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderRound closed = roundService.close(1L);

        assertEquals(RoundStatus.CLOSED, closed.getStatus());
        assertFalse(closed.isOrderingOpen());
    }

    @Test
    void onlyClosedRoundsCanBePacked() {
        OrderRound round = new OrderRound("Week 39",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        round.setId(1L);
        round.setStatus(RoundStatus.OPEN);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        assertThrows(IllegalStateException.class, () -> roundService.pack(1L));
    }

    @Test
    void closedRoundCanBeMarkedPacked() {
        OrderRound round = new OrderRound("Week 38",
                LocalDateTime.now().minusDays(8), LocalDateTime.now().minusDays(2));
        round.setId(2L);
        round.setStatus(RoundStatus.CLOSED);
        when(roundRepository.findById(2L)).thenReturn(Optional.of(round));
        when(roundRepository.save(any(OrderRound.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderRound packed = roundService.pack(2L);

        assertEquals(RoundStatus.PACKED, packed.getStatus());
    }
}
