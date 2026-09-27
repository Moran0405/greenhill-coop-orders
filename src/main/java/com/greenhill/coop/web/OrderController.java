package com.greenhill.coop.web;

import com.greenhill.coop.dto.OrderRequest;
import com.greenhill.coop.dto.OrderView;
import com.greenhill.coop.dto.RoundSummaryLine;
import com.greenhill.coop.service.OrderService;
import com.greenhill.coop.service.SummaryService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Ordering endpoints. Members only ever touch their own order;
 * the coordinator views every order of a round plus the summary.
 */
@RestController
public class OrderController {

    private final OrderService orderService;
    private final SummaryService summaryService;

    public OrderController(OrderService orderService, SummaryService summaryService) {
        this.orderService = orderService;
        this.summaryService = summaryService;
    }

    /** The signed-in member's own order for a round (or 404 if none yet). */
    @GetMapping("/api/rounds/{roundId}/my-order")
    public OrderView myOrder(@PathVariable Long roundId, HttpSession session) {
        Long memberId = SessionAuth.requireMember(session);
        return OrderView.from(orderService.getOwnOrder(memberId, roundId));
    }

    @GetMapping("/api/rounds/{roundId}/has-order")
    public java.util.Map<String, Object> hasOrder(@PathVariable Long roundId, HttpSession session) {
        Long memberId = SessionAuth.requireMember(session);
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("hasOrder", orderService.hasOrder(memberId, roundId));
        return result;
    }

    /** Create or update the member's own order. */
    @PutMapping("/api/rounds/{roundId}/my-order")
    public OrderView saveMyOrder(@PathVariable Long roundId,
                                 @RequestBody OrderRequest request,
                                 HttpSession session) {
        Long memberId = SessionAuth.requireMember(session);
        return OrderView.from(orderService.saveOwnOrder(memberId, roundId, request));
    }

    @DeleteMapping("/api/rounds/{roundId}/my-order")
    public java.util.Map<String, Object> cancelMyOrder(@PathVariable Long roundId, HttpSession session) {
        Long memberId = SessionAuth.requireMember(session);
        orderService.cancelOwnOrder(memberId, roundId);
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("cancelled", true);
        return result;
    }

    /** Coordinator: every order of the round. */
    @GetMapping("/api/rounds/{roundId}/orders")
    public List<OrderView> roundOrders(@PathVariable Long roundId, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return summaryService.roundOrders(roundId);
    }

    /** Coordinator: purchase totals per product for the wholesale order. */
    @GetMapping("/api/rounds/{roundId}/summary")
    public List<RoundSummaryLine> roundSummary(@PathVariable Long roundId, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return summaryService.roundSummary(roundId);
    }
}
