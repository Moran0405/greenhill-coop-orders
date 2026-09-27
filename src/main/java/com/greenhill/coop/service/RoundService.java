package com.greenhill.coop.service;

import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.RoundStatus;
import com.greenhill.coop.repository.OrderRepository;
import com.greenhill.coop.repository.OrderRoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the weekly ordering rounds. Each round opens on Monday and
 * closes on Sunday 8pm; after the cut-off the round becomes read only
 * and volunteers mark it packed once the Thursday pick-up is done.
 */
@Service
@Transactional
public class RoundService {

    private final OrderRoundRepository roundRepository;
    private final OrderRepository orderRepository;

    public RoundService(OrderRoundRepository roundRepository, OrderRepository orderRepository) {
        this.roundRepository = roundRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderRound> listAll() {
        return roundRepository.findAllByOrderByOpensAtDesc();
    }

    @Transactional(readOnly = true)
    public OrderRound get(Long id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Round not found: " + id));
    }

    @Transactional(readOnly = true)
    public OrderRound getCurrentOpenRound() {
        return roundRepository.findFirstByStatusOrderByClosesAtAsc(RoundStatus.OPEN).orElse(null);
    }

    public OrderRound create(String name, LocalDateTime opensAt, LocalDateTime closesAt) {
        if (opensAt == null || closesAt == null || !closesAt.isAfter(opensAt)) {
            throw new IllegalArgumentException("Close time must be after open time.");
        }
        OrderRound round = new OrderRound(name, opensAt, closesAt);
        round.setStatus(RoundStatus.OPEN);
        return roundRepository.save(round);
    }

    /** Pass the cut-off: orders become read only. */
    public OrderRound close(Long id) {
        OrderRound round = get(id);
        round.setStatus(RoundStatus.CLOSED);
        return roundRepository.save(round);
    }

    /** Thursday pick-up packing is complete. */
    public OrderRound pack(Long id) {
        OrderRound round = get(id);
        if (round.getStatus() != RoundStatus.CLOSED) {
            throw new IllegalStateException("Only a closed round can be marked as packed.");
        }
        round.setStatus(RoundStatus.PACKED);
        return roundRepository.save(round);
    }

    @Transactional(readOnly = true)
    public long orderCount(Long roundId) {
        return orderRepository.countByRoundId(roundId);
    }
}
