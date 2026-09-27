package com.greenhill.coop.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** Payload for creating a new ordering round (coordinator only). */
public class RoundRequest {

    @NotBlank
    private String name;

    @NotNull
    private String opensAt;

    @NotNull
    private String closesAt;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** ISO-8601 date-time, e.g. 2026-09-21T08:00:00 */
    public String getOpensAt() { return opensAt; }
    public void setOpensAt(String opensAt) { this.opensAt = opensAt; }

    /** ISO-8601 date-time, e.g. 2026-09-27T20:00:00 */
    public String getClosesAt() { return closesAt; }
    public void setClosesAt(String closesAt) { this.closesAt = closesAt; }
}
