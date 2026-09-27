package com.greenhill.coop.repository;

import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRoundRepository extends JpaRepository<OrderRound, Long> {
    Optional<OrderRound> findFirstByStatusOrderByClosesAtAsc(RoundStatus status);
    List<OrderRound> findAllByOrderByOpensAtDesc();
}
