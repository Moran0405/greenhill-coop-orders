package com.greenhill.coop.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/** One weekly ordering round. */
@Entity
@Table(name = "order_rounds")
public class OrderRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoundStatus status = RoundStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime opensAt;

    @Column(nullable = false)
    private LocalDateTime closesAt;

    public OrderRound() {
    }

    public OrderRound(String name, LocalDateTime opensAt, LocalDateTime closesAt) {
        this.name = name;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }

    /** True while members are allowed to submit or change orders. */
    public boolean isOrderingOpen() {
        return status == RoundStatus.OPEN;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoundStatus getStatus() { return status; }
    public void setStatus(RoundStatus status) { this.status = status; }

    public LocalDateTime getOpensAt() { return opensAt; }
    public void setOpensAt(LocalDateTime opensAt) { this.opensAt = opensAt; }

    public LocalDateTime getClosesAt() { return closesAt; }
    public void setClosesAt(LocalDateTime closesAt) { this.closesAt = closesAt; }
}
