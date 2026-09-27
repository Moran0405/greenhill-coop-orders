package com.greenhill.coop.dto;

import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.model.RoundStatus;

import java.time.LocalDateTime;

/** An ordering round as exposed to the front end. */
public class RoundView {

    private Long id;
    private String name;
    private RoundStatus status;
    private boolean orderingOpen;
    private LocalDateTime opensAt;
    private LocalDateTime closesAt;

    public static RoundView from(OrderRound r) {
        RoundView v = new RoundView();
        v.id = r.getId();
        v.name = r.getName();
        v.status = r.getStatus();
        v.orderingOpen = r.isOrderingOpen();
        v.opensAt = r.getOpensAt();
        v.closesAt = r.getClosesAt();
        return v;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public RoundStatus getStatus() { return status; }
    public boolean isOrderingOpen() { return orderingOpen; }
    public LocalDateTime getOpensAt() { return opensAt; }
    public LocalDateTime getClosesAt() { return closesAt; }
}
