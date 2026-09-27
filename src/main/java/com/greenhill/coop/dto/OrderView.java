package com.greenhill.coop.dto;

import com.greenhill.coop.model.Order;
import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.RoundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** A member order as exposed to the front end. */
public class OrderView {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long roundId;
    private String roundName;
    private RoundStatus roundStatus;
    private boolean editable;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
    private List<OrderItemView> items;
    private BigDecimal total;

    public static OrderView from(Order order) {
        OrderView v = new OrderView();
        v.id = order.getId();
        v.memberId = order.getMember().getId();
        v.memberName = order.getMember().getName();
        v.roundId = order.getRound().getId();
        OrderRound round = order.getRound();
        v.roundId = round.getId();
        v.roundName = round.getName();
        v.roundStatus = round.getStatus();
        v.editable = round.isOrderingOpen();
        v.placedAt = order.getPlacedAt();
        v.updatedAt = order.getUpdatedAt();
        v.items = order.getItems().stream().map(OrderItemView::from).collect(Collectors.toList());
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItemView i : v.items) {
            sum = sum.add(i.getLineTotal());
        }
        v.total = sum;
        return v;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Long getRoundId() { return roundId; }
    public String getRoundName() { return roundName; }
    public RoundStatus getRoundStatus() { return roundStatus; }
    public boolean isEditable() { return editable; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItemView> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
}
