package com.greenhill.coop.repository;

import com.greenhill.coop.model.Order;
import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByMemberIdAndRoundId(Long memberId, Long roundId);

    List<Order> findByRoundIdOrderByMemberNameAsc(Long roundId);

    long countByRoundId(Long roundId);

    long countByRoundStatus(RoundStatus status);

    @Query("select distinct o.round from Order o where o.member.id = :memberId order by o.round.closesAt desc")
    List<OrderRound> findRoundsOrderedByMember(@Param("memberId") Long memberId);
}
