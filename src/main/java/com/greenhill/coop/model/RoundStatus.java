package com.greenhill.coop.model;

/**
 * Lifecycle of an ordering round.
 * OPEN   - members may create, change and cancel orders.
 * CLOSED - the Sunday 8pm cut-off has passed; orders are read only.
 * PACKED - volunteers have packed the Thursday pick-up boxes.
 */
public enum RoundStatus {
    OPEN, CLOSED, PACKED
}
